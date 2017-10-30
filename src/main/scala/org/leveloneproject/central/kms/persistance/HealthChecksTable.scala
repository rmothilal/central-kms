package org.leveloneproject.central.kms.persistance

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheck, HealthCheckLevel, HealthCheckStatus}
import slick.lifted.ProvenShape

trait HealthChecksTable extends DataMappers {
  this: DbProfile ⇒

  import profile.api._

  class HealthChecksTable(tag: Tag) extends Table[HealthCheck](tag, "healthchecks") {
    def id: Rep[UUID] = column[UUID]("id")

    def sidecarId: Rep[UUID] = column[UUID]("sidecar_id")

    def level: Rep[HealthCheckLevel] = column[HealthCheckLevel]("level")

    def created: Rep[Instant] = column[Instant]("created")

    def status: Rep[HealthCheckStatus] = column[HealthCheckStatus]("status")

    def responded: Rep[Option[Instant]] = column[Option[Instant]]("responded")

    def response: Rep[Option[String]] = column[Option[String]]("response")

    def * : ProvenShape[HealthCheck] = (id, sidecarId, level, created, status, responded, response) <> (HealthCheck.tupled, HealthCheck.unapply)
  }

  val healthChecks: TableQuery[HealthChecksTable] = TableQuery[HealthChecksTable]
}

