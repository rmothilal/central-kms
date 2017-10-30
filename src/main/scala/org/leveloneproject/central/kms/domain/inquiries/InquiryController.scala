package org.leveloneproject.central.kms.domain.inquiries

import java.util.UUID

import com.google.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class InquiryController @Inject()(inquiries: InquiriesStore, inquiryResponses: InquiryResponsesStore) {

  def getInquirySummaryById(id: UUID): Future[Option[InquirySummary]] = {
    inquiries.findById(id).flatMap {
      case None ⇒ Future(None)
      case Some(i) ⇒ inquiryResponses.findByInquiryId(i.id).flatMap(r ⇒ verifyInquiryStatus(i, r))
    }
  }

  private def verifyInquiryStatus(i: Inquiry, r: Seq[InquiryResponse]): Future[Option[InquirySummary]] = {
    i.status match {
      case InquiryStatus.Pending if i.total <= r.length ⇒
        for {
          inquiry <- inquiries.updateStats(i.copy(status = InquiryStatus.Complete))
        } yield inquiry.map(i ⇒ InquirySummary(i, r))
      case _ ⇒ Future(Some(InquirySummary(i, r)))
    }
  }
}

case class InquiryResponseSummary(id: UUID, body: String, verified: Boolean)

object InquiryResponseSummary {
  def apply(response: InquiryResponse): InquiryResponseSummary = InquiryResponseSummary(response.batchId, response.body, response.verified)
}

case class InquirySummary(status: InquiryStatus, total: Int, completed: Int, results: Seq[InquiryResponseSummary])

object InquirySummary {
  def apply(inquiry: Inquiry, responses: Seq[InquiryResponse]): InquirySummary =
    InquirySummary(inquiry.status, inquiry.total, responses.length, responses.map(i ⇒ InquiryResponseSummary(i)))
}

