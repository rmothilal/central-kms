package org.leveloneproject.central.kms.domain.batches

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.util.InstantProvider
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class BatchServiceSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult {

  trait Setup {
    val store: BatchStore = mock[BatchStore]
    val currentInstant: Instant = Instant.now()

    val service = new BatchCreatorImpl(store) with InstantProvider {
      override def now(): Instant = currentInstant
    }
    val sidecarId: UUID = UUID.randomUUID()
    val batchId: UUID = UUID.randomUUID()
  }

  "create" should "save batch to repo" in new Setup {
    private val batch = Batch(batchId, sidecarId, "signature", currentInstant)

    when(store.create(any())).thenAnswer(i ⇒ Future(Right(i.getArgument[Batch](0))))

    private val result = await(service.create(CreateBatchRequest(sidecarId, batchId, "signature")))
    result shouldBe Right(batch)

    verify(store, times(1)).create(batch)
  }

}
