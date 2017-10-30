package org.leveloneproject.central.kms.crypto

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.util.Bytes._
import org.scalatest.{FlatSpec, Matchers}

class TweetNaClKeysSpec extends FlatSpec with Matchers with AwaitResult {

  trait Setup {
    val generator = new TweetNaClKeys
  }

  trait VerifierSetup extends Setup {
    val key: PublicPrivateKeyPair = await(generator.generate())
    val publicKey: String = key.publicKey
    val privateKey: String = key.privateKey

    val message = "Test Message"
    val signature: String = TweetNaCl.crypto_sign(message.fromUtf8, privateKey.fromHex).toHex
  }

  "generate" should "create right sized keys" in new Setup {
    private val result = await(generator.generate())
    result.publicKey.length shouldBe 64
    result.privateKey.length shouldBe 128
  }

  "verify" should "return true if signature is generated from privateKey" in new VerifierSetup {
    generator.verify(publicKey, signature, message) shouldBe VerificationResult.Success
  }

  it should "return false if message differs from signature" in new VerifierSetup {
    generator.verify(publicKey, signature, message + " ") shouldBe VerificationResult.InvalidSignature
  }

  it should "return false if signature differs from message" in new VerifierSetup {
    generator.verify(publicKey, signature.replace('A', 'B'), message) shouldBe VerificationResult.InvalidSignature
  }

  it should "be able to verify many times" in new VerifierSetup {
    generator.verify(publicKey, signature, message) shouldBe VerificationResult.Success
    generator.verify(publicKey, signature, message + " ") shouldBe VerificationResult.InvalidSignature
    generator.verify(publicKey, signature.replace('A', 'B'), message) shouldBe VerificationResult.InvalidSignature
    generator.verify(publicKey, signature, message) shouldBe VerificationResult.Success
  }

  it should "verify known signatures" in new Setup {

    // Data from http://ed25519.cr.yp.to/python/sign.input
    //noinspection SpellCheckingInspection
    val testData = "ab6f7aee6a0837b334ba5eb1b2ad7fcecfab7e323cab187fe2e0a95d80eff1325b96dca497875bf9664c5e75facf3f9bc54bae913d66ca15ee85f1491ca24d2c:5b96dca497875bf9664c5e75facf3f9bc54bae913d66ca15ee85f1491ca24d2c:8171456f8b907189b1d779e26bc5afbb08c67a:73bca64e9dd0db88138eedfafcea8f5436cfb74bfb0e7733cf349baa0c49775c56d5934e1d38e36f39b7c5beb0a836510c45126f8ec4b6810519905b0ca07c098171456f8b907189b1d779e26bc5afbb08c67a:"

    val splits: Array[String] = testData.split(':')
    val privateKey: Array[Byte] = splits(0).fromHex
    val publicKey: Array[Byte] = splits(1).fromHex
    val message: Array[Byte] = splits(2).fromHex
    val sig: Array[Byte] = splits(3).fromHex

    generator.verify(publicKey, sig, message) shouldBe VerificationResult.Success
  }

  it should "verify up to 64 bytes of the signature" in new Setup {

    val testData = "0ddcdc872c7b748d40efe96c2881ae189d87f56148ed8af3ebbbc80324e38bdd588ddadcbcedf40df0e9697d8bb277c7bb1498fa1d26ce0a835a760b92ca7c85:588ddadcbcedf40df0e9697d8bb277c7bb1498fa1d26ce0a835a760b92ca7c85:65641cd402add8bf3d1d67dbeb6d41debfbef67e4317c35b0a6d5bbbae0e034de7d670ba1413d056f2d6f1de12:c179c09456e235fe24105afa6e8ec04637f8f943817cd098ba95387f9653b2add181a31447d92d1a1ddf1ceb0db62118de9dffb7dcd2424057cbdff5d41d040365641cd402add8bf3d1d67dbeb6d41debfbef67e4317c35b0a6d5bbbae0e034de7d670ba1413d056f2d6f1de12:"

    val splits: Array[String] = testData.split(':')
    val privateKey: Array[Byte] = splits(0).fromHex
    val publicKey: Array[Byte] = splits(1).fromHex
    val message: Array[Byte] = splits(2).fromHex
    val sig: Array[Byte] = splits(3).fromHex.take(64)

    generator.verify(publicKey, sig, message) shouldBe VerificationResult.Success
  }

}
