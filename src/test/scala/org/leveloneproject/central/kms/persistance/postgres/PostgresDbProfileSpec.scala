package org.leveloneproject.central.kms.persistance.postgres

import org.scalatest.{FlatSpec, Matchers}

class PostgresDbProfileSpec extends FlatSpec with Matchers with PostgresDbProfile {

  "profile" should "be postgres profile" in {
    assert(profile.isInstanceOf[slick.jdbc.PostgresProfile])
  }
}
