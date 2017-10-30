package org.leveloneproject.central.kms.domain.healthchecks

import java.time.Instant
import java.util.UUID

import scala.concurrent.Future

trait HealthCheckStore {
  def create(healthCheck: HealthCheck): Future[HealthCheck]

  def complete(healthCheckId: UUID, response: String, timestamp: Instant): Future[Option[HealthCheck]]
}
