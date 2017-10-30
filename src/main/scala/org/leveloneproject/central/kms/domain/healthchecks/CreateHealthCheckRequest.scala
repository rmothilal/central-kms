package org.leveloneproject.central.kms.domain.healthchecks

import java.util.UUID

case class CreateHealthCheckRequest(sidecarId: UUID, level: HealthCheckLevel)
