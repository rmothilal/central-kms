package org.leveloneproject.central.kms.domain.sidecars

import com.google.inject.Inject
import org.leveloneproject.central.kms.crypto.{AsymmetricVerifier, SymmetricVerifier, VerificationResult}
import org.leveloneproject.central.kms.domain.KmsError

class ChallengeVerifier @Inject()(asymmetricVerifier: AsymmetricVerifier, symmetricVerifier: SymmetricVerifier) {
  def verify(challenge: String, keys: ChallengeKeys, answer: ChallengeAnswer): Either[KmsError, ChallengeResult] = {
    for {
      _ ← verifyBatchSignature(challenge, keys, answer)
      _ ← verifyRowSignature(challenge, keys, answer)
    } yield ChallengeResult.success
  }

  private def verifyBatchSignature(challenge: String, variables: ChallengeKeys, answer: ChallengeAnswer): Either[KmsError, VerificationResult] = {
    asymmetricVerifier.verify(variables.publicKey, answer.batchSignature, challenge) match {
      case r@VerificationResult(true, _) ⇒ Right(r)
      case VerificationResult(false, _) ⇒ Left(KmsError.invalidBatchSignature)
    }
  }

  private def verifyRowSignature(challenge: String, variables: ChallengeKeys, answer: ChallengeAnswer): Either[KmsError, VerificationResult] = {
    symmetricVerifier.verify(variables.symmetricKey, answer.rowSignature, challenge) match {
      case r@VerificationResult(true, _) ⇒ Right(r)
      case VerificationResult(false, _) ⇒ Left(KmsError.invalidRowSignature)
    }
  }
}

case class ChallengeResult(status: String)

object ChallengeResult {
  val success = ChallengeResult("OK")
}

case class ChallengeKeys(publicKey: String, symmetricKey: String)

case class ChallengeAnswer(batchSignature: String, rowSignature: String)

