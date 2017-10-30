package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.crypto._
import org.leveloneproject.central.kms.domain._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class KeyCreatorImplSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult {

  trait Setup {
    val asymmetricKeyGenerator: AsymmetricKeyGenerator = mock[AsymmetricKeyGenerator]
    val symmetricKeyGenerator: SymmetricKeyGenerator = mock[SymmetricKeyGenerator]
    val store: KeyStore = mock[KeyStore]
    val creator = new KeyCreator(asymmetricKeyGenerator, symmetricKeyGenerator, store)
    val keyId: UUID = UUID.randomUUID()
    val serviceName: String = UUID.randomUUID().toString

    final val publicKey = "some public key"
    final val privateKey = "some private key"
    final val symmetricKey = "some symmetric key"

    def setupKeys(): Unit = {
      when(asymmetricKeyGenerator.generate()).thenReturn(Future(PublicPrivateKeyPair(publicKey, privateKey)))
      when(symmetricKeyGenerator.generate()).thenReturn(Future(symmetricKey))
    }
  }

  "create" should "generate and return key" in new Setup {
    val key = Key(keyId, "some public key")
    when(store.create(any[Key])).thenReturn(Future(Right(key)))
    setupKeys()

    await(creator.create(CreateKeyRequest(keyId))) shouldBe Right(CreateKeyResponse(keyId, publicKey, privateKey, symmetricKey))
  }

  it should "save key to store" in new Setup {
    val key = Key(keyId, publicKey)
    when(store.create(key)).thenReturn(Future(Right(key)))

    setupKeys()

    await(creator.create(CreateKeyRequest(keyId)))

    verify(store).create(key)
  }

  it should "return createError from keystore" in new Setup {
    private val error = KmsError(500, "any message")
    setupKeys()
    when(store.create(any())).thenReturn(Future(Left(error)))

    await(creator.create(CreateKeyRequest(UUID.randomUUID()))) shouldBe Left(error)
  }

}
