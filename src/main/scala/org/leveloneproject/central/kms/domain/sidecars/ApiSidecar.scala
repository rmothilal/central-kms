package org.leveloneproject.central.kms.domain.sidecars

import java.util.UUID

case class ApiSidecar(id: UUID, serviceName: String, status: SidecarStatus)
