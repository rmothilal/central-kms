package org.leveloneproject.central.kms.persistance.postgres

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.sidecars.{SidecarLog, SidecarLogsStore}
import org.leveloneproject.central.kms.persistance.{DbProvider, SidecarLogsTable}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostgresSidecarLogsStore @Inject()(val dbProvider: DbProvider) extends PostgresDbProfile with SidecarLogsTable with SidecarLogsStore {

  import profile.api._

  private val db = dbProvider.db

  def create(log: SidecarLog): Future[SidecarLog] = db.run(sidecarLogs += log).map(_ ⇒ log)
}
