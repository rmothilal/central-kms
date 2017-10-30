package org.leveloneproject.central.kms.crypto

import scala.concurrent.Future

trait SymmetricKeyGenerator {

  def generate(): Future[String]
}
