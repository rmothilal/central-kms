package org.leveloneproject.central.kms.domain.batches

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.KmsError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait BatchFinder {
  def findById(id: UUID): Future[Either[KmsError, Batch]]
}

class BatchFinderImpl @Inject()(store: BatchStore) extends BatchFinder {
  def findById(id: UUID): Future[Either[KmsError, Batch]] = store.getById(id) map {
    case Some(batch) ⇒ Right(batch)
    case None ⇒ Left(KmsError.notFound("Batch", id))
  }
}
