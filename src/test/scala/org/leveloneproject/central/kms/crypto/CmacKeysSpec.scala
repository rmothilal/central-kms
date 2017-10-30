package org.leveloneproject.central.kms.crypto

import java.security.Security

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.util.Bytes._
import org.scalatest.{FlatSpec, Matchers}

class CmacKeysSpec extends FlatSpec with Matchers with AwaitResult {

  Security.addProvider(new BouncyCastleProvider)

  trait Setup {
    val keys = new CmacKeys
  }

  trait VerifySetup extends Setup {
    val key: String = "Sixteen byte keySixteen byte key".fromUtf8.toHex
    val message = "Hello World!"
    val signature = "36d70f0e3f0ff84e8cfbcfafbbb4ca4d"
  }

  "generate" should "generate 32 byte key" in new Setup {
    await(keys.generate()).length shouldBe 64
  }

  "verify" should "verify valid signature" in new VerifySetup {
    keys.verify(key, signature, message) shouldBe VerificationResult.Success
  }

  it should "return left if signature is not right length" in new VerifySetup {
    keys.verify(key, signature + signature, message) shouldBe VerificationResult.InvalidSignature
  }

  it should "return InvalidSignature if key is not hex" in new VerifySetup {
    keys.verify("01234567890ABCDEFGHIJKLMNOPQRSTUVWXYZ", signature, message) shouldBe VerificationResult.InvalidKey
  }
}
