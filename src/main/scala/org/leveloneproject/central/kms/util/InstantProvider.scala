package org.leveloneproject.central.kms.util

import java.time.Instant

trait InstantProvider {
  def now(): Instant = Instant.now()
}
