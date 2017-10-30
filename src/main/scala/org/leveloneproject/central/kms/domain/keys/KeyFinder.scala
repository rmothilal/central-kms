package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.KmsError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait KeyFinder {
  def findById(id: UUID): Future[Either[KmsError, Key]]
}

class KeyFinderImpl @Inject()(keyStore: KeyStore) extends KeyFinder {
  def findById(id: UUID): Future[Either[KmsError, Key]] = keyStore.getById(id) map {
    case Some(key) ⇒ Right(key)
    case None ⇒ Left(KmsError.notFound("Key", id))
  }
}
