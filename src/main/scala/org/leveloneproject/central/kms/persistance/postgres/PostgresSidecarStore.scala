package org.leveloneproject.central.kms.persistance.postgres

import java.sql.SQLException
import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.sidecars.{Sidecar, SidecarStatus, SidecarStore}
import org.leveloneproject.central.kms.persistance.{DatabaseHelper, DbProvider, SidecarsTable}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PostgresSidecarStore @Inject()(val dbProvider: DbProvider) extends PostgresDbProfile with SidecarsTable with SidecarStore with DatabaseHelper {

  import profile.api._

  private val db = dbProvider.db

  def create(sidecar: Sidecar): Future[Either[KmsError, Sidecar]] =
    db.run(sidecars += sidecar)
      .map(_ ⇒ Right(sidecar)).recover {
      case ex: SQLException if isPrimaryKeyViolation(ex) ⇒ Left(KmsError.sidecarExistsError(sidecar.id))
      case _: Throwable ⇒ Left(KmsError.internalError)
    }

  def updateStatus(id: UUID, sidecarStatus: SidecarStatus): Future[Int] =
    db.run(sidecars.filter(_.id === id).map(s ⇒ s.status).update(sidecarStatus))
}
