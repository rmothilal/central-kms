package org.leveloneproject.central.kms.persistance

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.domain.sidecars.{SidecarLog, SidecarStatus}
import slick.lifted.ProvenShape

trait SidecarLogsTable extends DataMappers {
  this: DbProfile ⇒

  import profile.api._

  class SidecarLogsTable(tag: Tag) extends Table[SidecarLog](tag, "sidecarlogs") {
    def id: Rep[UUID] = column[UUID]("id")
    def sidecarId: Rep[UUID] = column[UUID]("sidecar_id")
    def timestamp: Rep[Instant] = column[Instant]("timestamp")
    def status: Rep[SidecarStatus] = column[SidecarStatus]("status")
    def message: Rep[Option[String]] = column[Option[String]]("message")
    def * : ProvenShape[SidecarLog] = (id, sidecarId, timestamp, status, message) <> (SidecarLog.tupled, SidecarLog.unapply)
  }

  val sidecarLogs: TableQuery[SidecarLogsTable] = TableQuery[SidecarLogsTable]
}





