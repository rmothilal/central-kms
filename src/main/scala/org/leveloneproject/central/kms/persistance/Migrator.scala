package org.leveloneproject.central.kms.persistance

import com.google.inject.Inject
import com.typesafe.config.Config
import org.flywaydb.core.Flyway

class Migrator @Inject()(config: Config, flyway: Flyway) {

  def migrate(): Unit = {
    flyway.setDataSource(config.getString("db.url"), config.getString("db.user"), config.getString("db.password"))

    try {
      flyway.migrate()
    }
    catch {
      case _: Exception ⇒
        flyway.repair()
        flyway.migrate()
    }
  }
}
