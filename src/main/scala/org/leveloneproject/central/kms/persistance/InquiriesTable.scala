package org.leveloneproject.central.kms.persistance

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.domain.inquiries.{Inquiry, InquiryStatus}
import slick.lifted.ProvenShape

trait InquiriesTable extends DataMappers {
  this: DbProfile ⇒

  import profile.api._

  class InquiresTable(tag: Tag) extends Table[Inquiry](tag, "inquiries") {
    def id: Rep[UUID] = column[UUID]("id")

    def service: Rep[String] = column[String]("service")

    def startTime: Rep[Instant] = column[Instant]("start_time")

    def endTime: Rep[Instant] = column[Instant]("end_time")

    def created: Rep[Instant] = column[Instant]("created")

    def status: Rep[InquiryStatus] = column[InquiryStatus]("status")

    def issuedTo: Rep[UUID] = column[UUID]("issued_to")

    def total: Rep[Int] = column[Int]("total")

    def * : ProvenShape[Inquiry] = (id, service, startTime, endTime, created, status, issuedTo, total) <> (Inquiry.tupled, Inquiry.unapply)
  }

  val inquiries: TableQuery[InquiresTable] = TableQuery[InquiresTable]
}


