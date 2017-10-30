package org.leveloneproject.central.kms.persistance.postgres

import java.sql.SQLException
import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.batches.{Batch, BatchStore}
import org.leveloneproject.central.kms.persistance.{BatchesTable, DatabaseHelper, DbProvider}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostgresBatchStore @Inject()(val dbProvider: DbProvider) extends PostgresDbProfile with BatchesTable with BatchStore with DatabaseHelper {

  import profile.api._

  private val db = dbProvider.db

  def create(batch: Batch): Future[Either[KmsError, Batch]] = db.run(batches += batch).map(_ ⇒ Right(batch)).recover {
    case ex: SQLException if isPrimaryKeyViolation(ex) ⇒ Left(KmsError.batchExistsError(batch.id))
    case _ ⇒ Left(KmsError.internalError)
  }

  def getById(id: UUID): Future[Option[Batch]] = db.run(batches.filter(_.id === id).result.headOption)
}
