package org.leveloneproject.central.kms.persistance.postgres

import java.time.Instant
import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheck, HealthCheckStore}
import org.leveloneproject.central.kms.persistance.{DbProvider, HealthChecksTable}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostgresHealthCheckStore @Inject()(val dbProvider: DbProvider) extends PostgresDbProfile with HealthChecksTable with HealthCheckStore {

  import profile.api._

  private val db = dbProvider.db

  def create(healthCheck: HealthCheck): Future[HealthCheck] = db.run(healthChecks += healthCheck).map(_ ⇒ healthCheck)

  def complete(healthCheckId: UUID, response: String, timestamp: Instant): Future[Option[HealthCheck]] = {
    val filter = healthChecks.filter(_.id === healthCheckId)

    val update = filter.map(h ⇒ (h.responded, h.response))
      .update((Some(timestamp), Some(response)))

    db.run(update).flatMap(_ ⇒ db.run(filter.result.headOption))
  }
}
