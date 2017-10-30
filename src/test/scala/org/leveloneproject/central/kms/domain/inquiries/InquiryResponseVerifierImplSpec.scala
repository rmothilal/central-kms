package org.leveloneproject.central.kms.domain.inquiries

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.crypto.VerificationResult
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.batches.{Batch, BatchFinder}
import org.leveloneproject.central.kms.domain.keys.{Key, KeyVerifier}
import org.leveloneproject.central.kms.util.{IdGenerator, InstantProvider}
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class InquiryResponseVerifierImplSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult {

  trait Setup {
    val inquiriesStore: InquiriesStore = mock[InquiriesStore]
    val batchFinder: BatchFinder = mock[BatchFinder]
    val keyVerifier: KeyVerifier = mock[KeyVerifier]
    val inquiryResponseStore: InquiryResponsesStore = mock[InquiryResponsesStore]
    when(inquiryResponseStore.create(any[InquiryResponse])).thenAnswer(i ⇒ Future(i.getArgument[InquiryResponse](0)))
    val inquiryResponseId: UUID = UUID.randomUUID()
    val created: Instant = Instant.now()

    val verifier = new InquiryResponseVerifierImpl(inquiriesStore, batchFinder, keyVerifier, inquiryResponseStore) with IdGenerator with InstantProvider {
      override def newId(): UUID = inquiryResponseId

      override def now(): Instant = created
    }

    val inquiryId: UUID = UUID.randomUUID()
    val batchId: UUID = UUID.randomUUID()
    val keyId: UUID = UUID.randomUUID()
    val signature = "signature"
    val publicKey = "public key"
    val body = "body"
    val responseCount = 0
    val total = 100
    val item = 50
    val request = InquiryResponseRequest(inquiryId, batchId, body, total, item, keyId)
    val emptyRequest = EmptyInquiryResponse(inquiryId)

    val inquiry = Inquiry(inquiryId, "service", Instant.now(), Instant.now(), Instant.now(), InquiryStatus.Created, keyId)
    val batch = Batch(batchId, keyId, signature, Instant.now())
    val key = Key(keyId, publicKey)
    val response = InquiryResponse(inquiryResponseId, inquiryId, batchId, body, item, created, keyId)

    def verifyResponseSaved(response: InquiryResponse): Future[InquiryResponse] = {
      verify(inquiryResponseStore, times(1)).create(response)
    }
  }

  trait GoodBatch {
    this: Setup ⇒
    when(batchFinder.findById(batchId)).thenReturn(Future(Right(batch)))
  }

  trait GoodKeyVerification {
    this: Setup ⇒
    when(keyVerifier.verify(keyId, signature, body)).thenReturn(Future(Right(VerificationResult.Success)))
  }

  trait GoodInquiry {
    this: Setup ⇒
    when(inquiriesStore.findById(inquiryId)).thenReturn(Future(Some(inquiry)))
    when(inquiriesStore.updateStats(any[Inquiry]())).thenAnswer(i ⇒ Future(Some(i.getArgument[Inquiry](0))))
  }

  "verify" should "return unverified response if inquiry does not exist" in new Setup {
    private val error = KmsError.notFound("Inquiry", inquiryId)
    when(inquiriesStore.findById(inquiryId)).thenReturn(Future(None))

    private val result = await(verifier.verify(request))

    result shouldBe response.copy(verified = false, errorMessage = Some(error.message))
    verifyResponseSaved(result)
  }


  it should "return unverified response if batch does not exist" in new Setup with GoodInquiry {
    private val error = KmsError.notFound("Batch", batchId)
    when(inquiriesStore.findById(inquiryId)).thenReturn(Future(Some(inquiry)))
    when(batchFinder.findById(batchId)).thenReturn(Future(Left(error)))

    private val result = await(verifier.verify(request))

    result shouldBe response.copy(verified = false, errorMessage = Some(error.message))
    verifyResponseSaved(result)
  }

  it should "return unverified response if key does not exist fails" in new Setup with GoodInquiry with GoodBatch {
    private val error = KmsError.notFound("Key", keyId)
    when(keyVerifier.verify(keyId, signature, body)).thenReturn(Future(Left(error)))

    private val result = await(verifier.verify(request))
    result shouldBe response.copy(verified = false, errorMessage = Some(error.message))
    verifyResponseSaved(result)
  }

  it should "return verified inquiry response" in new Setup with GoodInquiry with GoodBatch with GoodKeyVerification {
    private val result = await(verifier.verify(request))
    result shouldBe response.copy(verified = true)
    verifyResponseSaved(result)
  }

  it should "update inquiry stats" in new Setup with GoodInquiry with GoodBatch with GoodKeyVerification {
    private val result = await(verifier.verify(request))
    result shouldBe response.copy(verified = true)
    verify(inquiriesStore, times(1)).updateStats(inquiry.copy(total = total, status = InquiryStatus.Pending))
    verifyResponseSaved(result)
  }

  it should "update inquiry to complete and return nothing when inquiry result is empty" in new Setup with GoodInquiry {
    await(verifier.verify(emptyRequest))
    verify(inquiriesStore, times(1)).updateStats(inquiry.copy(total = 0, status = InquiryStatus.Complete))
    verify(inquiryResponseStore, times(0)).create(any())
  }
}
