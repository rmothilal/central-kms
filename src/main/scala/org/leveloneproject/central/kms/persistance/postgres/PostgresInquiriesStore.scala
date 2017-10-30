package org.leveloneproject.central.kms.persistance.postgres

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.inquiries.{InquiriesStore, Inquiry}
import org.leveloneproject.central.kms.persistance.{DbProvider, InquiriesTable}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PostgresInquiriesStore @Inject()(val dbProvider: DbProvider) extends PostgresDbProfile with InquiriesTable with InquiriesStore {

  import profile.api._

  private val db = dbProvider.db

  def findById(id: UUID): Future[Option[Inquiry]] = db.run {
    inquiries.filter(_.id === id).result.headOption
  }

  def updateStats(inquiry: Inquiry): Future[Option[Inquiry]] =
    db.run(inquiries
      .filter(_.id === inquiry.id)
      .map(i ⇒ (i.status, i.total))
      .update(inquiry.status, inquiry.total)
      .map {
        case 0 ⇒ None
        case _ ⇒ Some(inquiry)
      }
    )

  def insert(inquiry: Inquiry): Future[Inquiry] = db.run(inquiries += inquiry) map (_ ⇒ inquiry)
}




