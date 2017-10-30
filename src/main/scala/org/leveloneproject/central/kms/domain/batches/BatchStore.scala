package org.leveloneproject.central.kms.domain.batches

import java.util.UUID

import org.leveloneproject.central.kms.domain.KmsError

import scala.concurrent.Future

trait BatchStore {
  def create(batch: Batch): Future[Either[KmsError, Batch]]

  def getById(id: UUID): Future[Option[Batch]]
}
