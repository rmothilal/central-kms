package org.leveloneproject.central.kms.domain.sidecars

import java.util.UUID

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.keys.{CreateKeyRequest, KeyCreator}
import org.leveloneproject.central.kms.util.{ChallengeGenerator, FutureEither, IdGenerator, InstantProvider}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SidecarService @Inject()(
                                sidecarStore: SidecarStore,
                                keyService: KeyCreator,
                                sidecarList: SidecarList,
                                sidecarLogsStore: SidecarLogsStore
                              ) extends ChallengeGenerator with IdGenerator with InstantProvider {

  def register(request: RegisterRequest): Future[Either[KmsError, RegisterResponse]] = {
    val status = SidecarStatus.Challenged
    val sidecar = Sidecar(request.id, request.serviceName, status, newChallenge())

    for {
      s ← FutureEither(sidecarStore.create(sidecar))
      k ← FutureEither(keyService.create(CreateKeyRequest(s.id)))
      _ ← logStatusChange(sidecar.id, status)
    } yield RegisterResponse(s, k)
  }

  def challengeAccepted(sidecarWithActor: SidecarAndActor): Future[Either[KmsError, SidecarAndActor]] = {
    FutureEither(updateStatus(sidecarWithActor.sidecar, SidecarStatus.Registered)).map { sidecar ⇒
      val n = sidecarWithActor.copy(sidecar = sidecar)
      sidecarList.register(n)
      n
    }
  }

  def suspend(sidecar: Sidecar, reason: String): Future[Either[KmsError, Sidecar]] = {
    for {
      _ ← logStatusChange(sidecar.id, SidecarStatus.Suspended, Some(reason))
      terminated ← terminate(sidecar)
    } yield terminated
  }

  def terminate(sidecar: Sidecar): FutureEither[KmsError, Sidecar] = {
    for {
      updated ← updateStatus(sidecar, SidecarStatus.Terminated)
      _ ← Future(sidecarList.unregister(sidecar.id))
    } yield Right(updated)
  }

  def active(): Future[Seq[ApiSidecar]] = Future(sidecarList.registered().map(s ⇒ ApiSidecar(s.id, s.serviceName, s.status)))

  private def updateStatus(sidecar: Sidecar, newStatus: SidecarStatus, message: Option[String] = None): FutureEither[KmsError, Sidecar] = {
    val updated = sidecar.copy(status = newStatus)
    for {
      _ ← logStatusChange(sidecar.id, newStatus, message)
      _ ← sidecarStore.updateStatus(sidecar.id, newStatus)
    } yield Right(updated)
  }

  private def logStatusChange(sidecarId: UUID, sidecarStatus: SidecarStatus, message: Option[String] = None): FutureEither[KmsError, SidecarLog] = {
    sidecarLogsStore.create(SidecarLog(newId(), sidecarId, now(), sidecarStatus, message)).map(Right(_)).recover {
      case _: Throwable ⇒ Left(KmsError.internalError)
    }

  }
}
