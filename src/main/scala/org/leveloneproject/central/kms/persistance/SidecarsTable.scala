package org.leveloneproject.central.kms.persistance

import java.util.UUID

import org.leveloneproject.central.kms.domain.sidecars.{Sidecar, SidecarStatus}
import slick.lifted.ProvenShape

trait SidecarsTable extends DataMappers {
  this: DbProfile ⇒

  import profile.api._

  class SidecarsTable(tag: Tag) extends Table[Sidecar](tag, "sidecars") {
    def id: Rep[UUID] = column[UUID]("id")
    def serviceName: Rep[String] = column[String]("service_name")
    def status: Rep[SidecarStatus] = column[SidecarStatus]("status")
    def challenge: Rep[String] = column[String]("challenge")
    def * : ProvenShape[Sidecar] = (id, serviceName, status, challenge) <> (Sidecar.tupled, Sidecar.unapply)
  }

  val sidecars: TableQuery[SidecarsTable] = TableQuery[SidecarsTable]
}
