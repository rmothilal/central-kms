package org.leveloneproject.central.kms.persistance.postgres

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.inquiries.{InquiryResponse, InquiryResponsesStore}
import org.leveloneproject.central.kms.persistance.{DbProvider, InquiryResponsesTable}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostgresInquiryResponsesStore @Inject()(dbProvider: DbProvider) extends PostgresDbProfile with InquiryResponsesTable with InquiryResponsesStore {
  import profile.api._

  private val db = dbProvider.db

  def create(response: InquiryResponse): Future[InquiryResponse] = db.run(inquiryResponses += response).map(_ ⇒ response)

  def findByInquiryId(id: UUID): Future[Seq[InquiryResponse]] = db.run(inquiryResponses.filter(_.inquiryId === id).result)
}
