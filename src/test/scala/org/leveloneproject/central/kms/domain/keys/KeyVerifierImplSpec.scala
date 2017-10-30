package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.crypto.{AsymmetricVerifier, VerificationResult}
import org.leveloneproject.central.kms.domain.KmsError
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class KeyVerifierImplSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult {

  trait Setup {
    val keyFinder: KeyFinder = mock[KeyFinder]
    val asymmetricVerifier: AsymmetricVerifier = mock[AsymmetricVerifier]
    val verifier = new KeyVerifierImpl(keyFinder, asymmetricVerifier)

    val keyId: UUID = UUID.randomUUID()
    val publicKey = "publicKey"
    val signature = "signature"
    val message = "message"
    val key = Key(keyId, publicKey)
  }

  "verify" should "return NotFound error if key does not exist" in new Setup {
    private val error = KmsError.notFound("Key", keyId)
    when(keyFinder.findById(keyId)).thenReturn(Future(Left(error)))

    await(verifier.verify(keyId, signature, message)) shouldBe Left(error)
  }

  it should "return verificationError if verification fails" in new Setup {
    when(keyFinder.findById(keyId)).thenReturn(Future(Right(key)))
    when(asymmetricVerifier.verify(publicKey, signature, message)).thenReturn(VerificationResult.InvalidSignature)

    await(verifier.verify(keyId, signature, message)) shouldBe Right(VerificationResult.InvalidSignature)
  }

  it should "return verification result if verification passes" in new Setup {
    when(keyFinder.findById(keyId)).thenReturn(Future(Right(key)))
    when(asymmetricVerifier.verify(publicKey, signature, message)).thenReturn(VerificationResult.Success)

    await(verifier.verify(keyId, signature, message)) shouldBe Right(VerificationResult.Success)
  }
}
