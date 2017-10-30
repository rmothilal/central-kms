package org.leveloneproject.central.kms.domain.inquiries

import java.time.Instant
import java.util.UUID

case class InquiryResponse(id: UUID, inquiryId: UUID, batchId: UUID, body: String, item: Int, created: Instant, fulfillingSidecar: UUID, verified: Boolean = false, errorMessage: Option[String] = None)
