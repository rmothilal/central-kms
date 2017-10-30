package org.leveloneproject.central.kms.domain.inquiries

import java.util.UUID

import scala.concurrent.Future

trait InquiriesStore {
  def insert(inquiry: Inquiry): Future[Inquiry]

  def findById(id: UUID): Future[Option[Inquiry]]

  def updateStats(inquiry: Inquiry): Future[Option[Inquiry]]
}
