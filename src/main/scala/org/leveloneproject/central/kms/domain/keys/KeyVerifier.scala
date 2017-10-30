package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.crypto.{AsymmetricVerifier, VerificationResult}
import org.leveloneproject.central.kms.domain.KmsError

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait KeyVerifier {
  def verify(id: UUID, signature: String, message: String): Future[Either[KmsError, VerificationResult]]
}

class KeyVerifierImpl @Inject()(keyFinder: KeyFinder, asymmetricVerifier: AsymmetricVerifier) extends KeyVerifier {
  def verify(id: UUID, signature: String, message: String): Future[Either[KmsError, VerificationResult]] = {
    keyFinder.findById(id) map {
      case Right(k) ⇒ Right(asymmetricVerifier.verify(k.publicKey, signature, message))
      case Left(_) ⇒ Left(KmsError.notFound("Key", id))
    }
  }
}
