package org.leveloneproject.central.kms.crypto

trait AsymmetricVerifier {
  def verify(publicKey: String, signature: String, message: String): VerificationResult
}
