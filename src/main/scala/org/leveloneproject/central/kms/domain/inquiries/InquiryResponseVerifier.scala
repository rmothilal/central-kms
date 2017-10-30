package org.leveloneproject.central.kms.domain.inquiries

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.crypto.VerificationResult
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.batches.BatchFinder
import org.leveloneproject.central.kms.domain.keys.KeyVerifier
import org.leveloneproject.central.kms.util.{FutureEither, IdGenerator, InstantProvider}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


case class InquiryResponseRequest(inquiryId: UUID, batchId: UUID, body: String, total: Int, item: Int, sidecarId: UUID)

case class EmptyInquiryResponse(inquiryId: UUID)

trait InquiryResponseVerifier {
  def verify(response: InquiryResponseRequest): Future[InquiryResponse]

  def verify(response: EmptyInquiryResponse): Future[Unit]
}

class InquiryResponseVerifierImpl @Inject()(
                                             inquiriesStore: InquiriesStore,
                                             batchFinder: BatchFinder,
                                             keyVerifier: KeyVerifier,
                                             responseStore: InquiryResponsesStore)
  extends InquiryResponseVerifier with IdGenerator with InstantProvider {

  def verify(request: InquiryResponseRequest): Future[InquiryResponse] = {
    val response = InquiryResponse(newId(), request.inquiryId, request.batchId, request.body, request.item, now(), request.sidecarId)

    val result: Future[Either[KmsError, VerificationResult]] = for {
      _ ← findAndUpdateInquiry(request.inquiryId, request.total, InquiryStatus.Pending)
      batch ← FutureEither(batchFinder.findById(request.batchId))
      verificationResult ← FutureEither(keyVerifier.verify(batch.sidecarId, batch.signature, request.body))
    } yield verificationResult

    result
      .flatMap(e ⇒
        responseStore.create(e.fold(k ⇒ response.copy(verified = false, errorMessage = Some(k.message)), r ⇒ response.copy(verified = r.success, errorMessage = r.message))))
  }

  def verify(response: EmptyInquiryResponse): Future[Unit] = {
    val result: Future[Either[KmsError, Inquiry]] = findAndUpdateInquiry(response.inquiryId, 0, InquiryStatus.Complete)
    result.map(_ ⇒ Nil)
  }


  private def findAndUpdateInquiry(id: UUID, total: Int, newStatus: InquiryStatus): FutureEither[KmsError, Inquiry] =
    for {
      inquiry ← inquiriesStore.findById(id)
      updatedInquiry ← updateInquiryStats(inquiry, total, newStatus)
    } yield updatedInquiry.toRight(KmsError.notFound("Inquiry", id))

  private def updateInquiryStats(i: Option[Inquiry], total: Int, newStatus: InquiryStatus): Future[Option[Inquiry]] = {
    i match {
      case Some(inquiry) ⇒
        val newTotal = math.max(inquiry.total, total)
        val status = newStatus
        inquiriesStore.updateStats(inquiry.copy(status = status, total = newTotal))
      case None ⇒ Future(None)
    }
  }

}
