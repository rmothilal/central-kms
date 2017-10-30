package org.leveloneproject.central.kms.util

import java.util.UUID

trait IdGenerator {
  def newId(): UUID = UUID.randomUUID()
}



