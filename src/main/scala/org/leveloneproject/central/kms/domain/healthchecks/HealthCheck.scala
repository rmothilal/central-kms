package org.leveloneproject.central.kms.domain.healthchecks

import java.time.Instant
import java.util.UUID

case class HealthCheck(id: UUID, sidecarId: UUID, level: HealthCheckLevel, created: Instant, status: HealthCheckStatus, responded: Option[Instant] = None, result: Option[String] = None)
