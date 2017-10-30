package org.leveloneproject.central.kms.crypto

import org.leveloneproject.central.kms.util.Bytes._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Try}

class TweetNaClKeys extends AsymmetricKeyGenerator with AsymmetricVerifier {
  private val CryptoSignatureSize = 64

  def generate(): Future[PublicPrivateKeyPair] = Future {
    val publicKey = Array.ofDim[Byte](TweetNaCl.SIGN_PUBLIC_KEY_BYTES)
    val privateKey = Array.ofDim[Byte](TweetNaCl.SIGN_SECRET_KEY_BYTES)

    TweetNaCl.crypto_sign_keypair(publicKey, privateKey, false)
    PublicPrivateKeyPair(publicKey.toHex, privateKey.toHex)
  }

  def verify(publicKey: Array[Byte], signature: Array[Byte], message: Array[Byte]): VerificationResult = {
    val sm = getSignature(signature, message)
    Try(TweetNaCl.crypto_sign_open(sm, publicKey)) match {
      case Success(unsigned) if unsigned.deep == message.deep ⇒ VerificationResult.Success
      case _ ⇒ VerificationResult.InvalidSignature
    }
  }

  def verify(publicKey: String, signature: String, message: String): VerificationResult = verify(publicKey.fromHex, signature.fromHex, message.fromUtf8)

  private def getSignature(original: Array[Byte], message: Array[Byte]): Array[Byte] = {
    if (original.length != CryptoSignatureSize) {
      original
    } else {
      val newSignature = Array.ofDim[Byte](64 + message.length)
      Array.copy(original, 0, newSignature, 0, CryptoSignatureSize)
      Array.copy(message, 0, newSignature, CryptoSignatureSize, message.length)
      newSignature
    }

  }
}
