package org.leveloneproject.central.kms.persistance.postgres

import java.sql.SQLException
import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.keys.{Key, KeyStore}
import org.leveloneproject.central.kms.persistance.{DatabaseHelper, DbProvider, KeysTable}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class PostgresKeyStore @Inject()(val dbProvider: DbProvider) extends PostgresDbProfile with KeyStore with KeysTable with DatabaseHelper {
  import profile.api._

  private val db = dbProvider.db


  def create(key: Key): Future[Either[KmsError, Key]] =
    db.run(keys += key).map(_ ⇒ Right(key)).recover {
      case ex: SQLException if isPrimaryKeyViolation(ex) ⇒ Left(KmsError.sidecarExistsError(key.id))
      case _ ⇒ Left(KmsError.internalError)
    }

  def getById(id: UUID): Future[Option[Key]] = db.run(keys.filter(_.id === id).result.headOption)
}
