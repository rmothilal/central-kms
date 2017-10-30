package org.leveloneproject.central.kms.domain.sidecars

import java.util.UUID

case class Sidecar(id: UUID, serviceName: String, status: SidecarStatus, challenge: String)
