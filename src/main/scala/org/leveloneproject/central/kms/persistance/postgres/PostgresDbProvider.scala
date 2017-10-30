package org.leveloneproject.central.kms.persistance.postgres

import com.google.inject.Inject
import com.typesafe.config.Config
import org.leveloneproject.central.kms.persistance.DbProvider

class PostgresDbProvider @Inject()(config: Config) extends DbProvider with PostgresDbProfile {

  import profile.api._

  lazy val db: profile.api.Database = Database.forConfig("db", config)
}
