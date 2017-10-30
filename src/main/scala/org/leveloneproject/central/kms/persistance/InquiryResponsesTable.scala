package org.leveloneproject.central.kms.persistance

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.domain.inquiries.InquiryResponse
import slick.lifted.ProvenShape

trait InquiryResponsesTable extends DataMappers {
  this: DbProfile ⇒

  import profile.api._

  class InquiryResponsesTable(tag: Tag) extends Table[InquiryResponse](tag, "inquiry_responses") {
    def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)

    def inquiryId: Rep[UUID] = column[UUID]("inquiry_id")

    def batchId: Rep[UUID] = column[UUID]("batch_id")

    def body: Rep[String] = column[String]("body")

    def item: Rep[Int] = column[Int]("item")

    def created: Rep[Instant] = column[Instant]("created")

    def sidecarId: Rep[UUID] = column[UUID]("fulfilling_sidecar_id")

    def verified: Rep[Boolean] = column[Boolean]("verified")

    def errorMessage: Rep[Option[String]] = column[Option[String]]("error_message")


    def * : ProvenShape[InquiryResponse] = (id, inquiryId, batchId, body, item, created, sidecarId, verified, errorMessage) <> (InquiryResponse.tupled, InquiryResponse.unapply)
  }

  val inquiryResponses: TableQuery[InquiryResponsesTable] = TableQuery[InquiryResponsesTable]
}
