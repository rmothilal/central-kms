package org.leveloneproject.central.kms.domain.inquiries

import java.time.Instant
import java.util.UUID

case class Inquiry(
                    id: UUID,
                    service: String,
                    startTime: Instant,
                    endTime: Instant,
                    created: Instant,
                    status: InquiryStatus,
                    issuedTo: UUID,
                    total: Int = 0
                  )
