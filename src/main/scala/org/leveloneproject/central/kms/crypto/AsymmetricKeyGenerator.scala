package org.leveloneproject.central.kms.crypto

import scala.concurrent.Future

trait AsymmetricKeyGenerator {

  def generate(): Future[PublicPrivateKeyPair]
}
