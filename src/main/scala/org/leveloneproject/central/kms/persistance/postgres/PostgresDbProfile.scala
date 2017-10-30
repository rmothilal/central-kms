package org.leveloneproject.central.kms.persistance.postgres

import org.leveloneproject.central.kms.persistance.DbProfile
import slick.jdbc.JdbcProfile

trait PostgresDbProfile extends DbProfile {
  val profile: JdbcProfile = slick.jdbc.PostgresProfile
}
