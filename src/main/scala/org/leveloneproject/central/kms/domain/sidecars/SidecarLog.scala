package org.leveloneproject.central.kms.domain.sidecars

import java.time.Instant
import java.util.UUID

case class SidecarLog(id: UUID, sidecarId: UUID, timestamp: Instant, status: SidecarStatus, message: Option[String] = None)
