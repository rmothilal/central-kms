package org.leveloneproject.central.kms.domain.inquiries

import java.util.UUID

import scala.concurrent.Future

trait InquiryResponsesStore {
  def findByInquiryId(id: UUID): Future[Seq[InquiryResponse]]

  def create(response: InquiryResponse): Future[InquiryResponse]
}
