package org.leveloneproject.central.kms.domain.keys

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.crypto._
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.util.FutureEither

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

sealed case class CreateKeyRequest(id: UUID)

sealed case class CreateKeyResponse(id: UUID, publicKey: String, privateKey: String, symmetricKey: String)

class KeyCreator @Inject()(
                            asymmetricKeyGenerator: AsymmetricKeyGenerator,
                            symmetricKeyGenerator: SymmetricKeyGenerator,
                            keyStore: KeyStore) {
  def create(keyRequest: CreateKeyRequest): Future[Either[KmsError, CreateKeyResponse]] = {

    for {
      keyPair ← asymmetricKeyGenerator.generate()
      symmetricKey ← symmetricKeyGenerator.generate()
      key ← FutureEither(keyStore.create(Key(keyRequest.id, keyPair.publicKey)))
    } yield CreateKeyResponse(key.id, keyPair.publicKey, keyPair.privateKey, symmetricKey)
  }
}


