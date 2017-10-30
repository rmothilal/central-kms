package org.leveloneproject.central.kms.domain.batches

import java.time.Instant
import java.util.UUID

case class Batch(id: UUID, sidecarId: UUID, signature: String, timestamp: Instant)
