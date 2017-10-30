package org.leveloneproject.central.kms.util

import java.util.UUID

trait ChallengeGenerator {
  def newChallenge(): String = UUID.randomUUID().toString
}
