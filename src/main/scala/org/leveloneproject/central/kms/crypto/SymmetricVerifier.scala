package org.leveloneproject.central.kms.crypto

trait SymmetricVerifier {
  def verify(key: String, signature: String, message: String): VerificationResult
}
