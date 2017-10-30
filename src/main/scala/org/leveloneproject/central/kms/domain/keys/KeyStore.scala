package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import org.leveloneproject.central.kms.domain._
import scala.concurrent.Future

trait KeyStore {

  def create(key: Key): Future[Either[KmsError, Key]]

  def getById(id: UUID): Future[Option[Key]]
}
