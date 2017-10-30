package org.leveloneproject.central.kms.crypto

case class VerificationResult(success: Boolean, message: Option[String] = None)

object VerificationResult {
  final val Success = VerificationResult(true)
  final val InvalidSignature = VerificationResult(false, Some("Invalid signature"))
  final val InvalidKey = VerificationResult(false, Some("Key is not in valid format"))
}
