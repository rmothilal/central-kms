package org.leveloneproject.central.kms.domain.batches

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.persistance.DatabaseHelper
import org.leveloneproject.central.kms.util.InstantProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

sealed case class CreateBatchRequest(sidecarId: UUID, batchId: UUID, signature: String)

sealed trait BatchCreator {
  def create(request: CreateBatchRequest): Future[Either[KmsError, Batch]]
}

class BatchCreatorImpl @Inject()(store: BatchStore) extends BatchCreator with DatabaseHelper with InstantProvider {
  def create(request: CreateBatchRequest): Future[Either[KmsError, Batch]] = {
    val batch = Batch(request.batchId, request.sidecarId, request.signature, now())
    store.create(batch)
  }
}
