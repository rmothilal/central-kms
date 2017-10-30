package org.leveloneproject.central.kms.persistance

import slick.jdbc.JdbcProfile

trait DbProfile {
  val profile: JdbcProfile
}

