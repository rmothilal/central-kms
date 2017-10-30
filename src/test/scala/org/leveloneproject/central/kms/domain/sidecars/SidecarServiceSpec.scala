package org.leveloneproject.central.kms.domain.sidecars

import java.time.Instant
import java.util.UUID

import akka.actor.ActorRef
import akka.testkit.TestProbe
import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.keys.{CreateKeyRequest, CreateKeyResponse, KeyCreator}
import org.leveloneproject.central.kms.util.{ChallengeGenerator, IdGenerator, InstantProvider}
import org.leveloneproject.central.kms.utils.AkkaSpec
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class SidecarServiceSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult with AkkaSpec {

  trait Setup {
    final val sidecarStore: SidecarStore = mock[SidecarStore]
    final val sidecarLogsStore: SidecarLogsStore = mock[SidecarLogsStore]
    final val keyService: KeyCreator = mock[KeyCreator]
    final val sidecarList: SidecarList = mock[SidecarList]
    final val currentInstant: Instant = Instant.now()

    final val sidecarId: UUID = UUID.randomUUID()
    final val logId: UUID = UUID.randomUUID()
    final val serviceName: String = "service name"
    final val sidecarActor: ActorRef = TestProbe().ref

    final val registerRequest = RegisterRequest(sidecarId, serviceName)

    final val challengeString: String = UUID.randomUUID().toString
    final val sidecarService = new SidecarService(sidecarStore, keyService, sidecarList, sidecarLogsStore) with ChallengeGenerator with IdGenerator with InstantProvider {
      override def newChallenge(): String = challengeString

      override def newId(): UUID = logId

      override def now(): Instant = currentInstant
    }
  }

  "register" should "return error if SideCar Repository returns error" in new Setup {

    private val internalError = KmsError.internalError
    when(sidecarStore.create(any())).thenReturn(Future(Left(internalError)))
    await(sidecarService.register(registerRequest)) shouldBe Left(internalError)
  }

  it should "return error if keyService returns error" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)
    when(sidecarStore.create(sidecar)).thenReturn(Future(Right(sidecar)))
    when(keyService.create(CreateKeyRequest(sidecarId))).thenReturn(Future(Left(KmsError.invalidRequest)))

    await(sidecarService.register(registerRequest)) shouldBe Left(KmsError.invalidRequest)
  }

  it should "return register result if sidecar and keys are saved" in new Setup {
    private val initialized = SidecarStatus.Challenged
    private val sidecar = Sidecar(sidecarId, serviceName, initialized, challengeString)
    when(sidecarStore.create(sidecar)).thenReturn(Future(Right(sidecar)))
    private val keyResponse = CreateKeyResponse(UUID.randomUUID(), "public key", "private key", "symmetric key")
    when(keyService.create(CreateKeyRequest(sidecarId))).thenReturn(Future(Right(keyResponse)))
    when(sidecarLogsStore.create(any())).thenReturn(Future(SidecarLog(logId, sidecarId, currentInstant, initialized)))

    await(sidecarService.register(registerRequest)) shouldBe Right(RegisterResponse(sidecar, keyResponse))
  }

  it should "add initialized to logs" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)
    private val log = SidecarLog(logId, sidecarId, currentInstant, SidecarStatus.Challenged, None)
    private val keyResponse = CreateKeyResponse(UUID.randomUUID(), "public key", "private key", "symmetric key")

    when(sidecarStore.create(sidecar)).thenReturn(Future(Right(sidecar)))
    when(sidecarLogsStore.create(log)).thenReturn(Future(log))
    when(keyService.create(CreateKeyRequest(sidecarId))).thenReturn(Future(Right(keyResponse)))

    await(sidecarService.register(registerRequest))

    verify(sidecarLogsStore, times(1)).create(log)
  }

  "challengeAccepted" should "add sidecar to sidecarList" in new Setup {
    when(sidecarStore.updateStatus(sidecarId, SidecarStatus.Registered)).thenReturn(Future(1))
    when(sidecarLogsStore.create(any())).thenReturn(Future(SidecarLog(logId, sidecarId, currentInstant, SidecarStatus.Registered)))
    private val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)

    private val sidecarWithActor = SidecarAndActor(sidecar, sidecarActor)

    await(sidecarService.challengeAccepted(sidecarWithActor))

    verify(sidecarList, times(1)).register(SidecarAndActor(sidecar.copy(status = SidecarStatus.Registered), sidecarActor))
  }

  "suspend" should "insert suspended and terminated logs in repo" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)

    private val reason = "reason"
    when(sidecarLogsStore.create(any())).thenAnswer(i ⇒ Future(i.getArgument[SidecarLog](0)))
    when(sidecarStore.updateStatus(sidecarId, SidecarStatus.Terminated)).thenReturn(Future(1))
    await(sidecarService.suspend(sidecar, reason))
    verify(sidecarLogsStore, times(1)).create(SidecarLog(logId, sidecarId, currentInstant, SidecarStatus.Suspended, Some(reason)))
    verify(sidecarLogsStore, times(1)).create(SidecarLog(logId, sidecarId, currentInstant, SidecarStatus.Terminated, None))
  }

  "terminate" should "insert terminated log in repo" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)

    private val log = SidecarLog(logId, sidecarId, currentInstant, SidecarStatus.Terminated, None)
    when(sidecarLogsStore.create(log)).thenReturn(Future(log))
    when(sidecarStore.updateStatus(sidecarId, SidecarStatus.Terminated)).thenReturn(Future(1))

    await(sidecarService.terminate(sidecar)) shouldBe Right(Sidecar(sidecarId, serviceName, SidecarStatus.Terminated, challengeString))

    verify(sidecarLogsStore, times(1)).create(log)
    verify(sidecarStore, times(1)).updateStatus(sidecarId, SidecarStatus.Terminated)
  }

  it should "remove sidecar from SidecarList" in new Setup {
    private val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)
    when(sidecarLogsStore.create(any())).thenReturn(Future(SidecarLog(logId, sidecarId, currentInstant, SidecarStatus.Terminated)))
    when(sidecarStore.updateStatus(sidecarId, SidecarStatus.Terminated)).thenReturn(Future(1))

    await(sidecarService.terminate(sidecar))

    verify(sidecarList, times(1)).unregister(sidecarId)
  }
}
