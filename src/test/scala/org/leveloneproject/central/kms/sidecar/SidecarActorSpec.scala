package org.leveloneproject.central.kms.sidecar

import java.time.Instant
import java.util.UUID

import akka.testkit.{TestActorRef, TestProbe}
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.batches.Batch
import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheck, HealthCheckLevel, HealthCheckStatus}
import org.leveloneproject.central.kms.domain.inquiries.{Inquiry, InquiryStatus}
import org.leveloneproject.central.kms.domain.keys._
import org.leveloneproject.central.kms.domain.sidecars._
import org.leveloneproject.central.kms.socket.JsonResponse
import org.leveloneproject.central.kms.utils.AkkaSpec
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class SidecarActorSpec extends FlatSpec with AkkaSpec with Matchers with MockitoSugar {

  trait Setup {
    val out = TestProbe()
    val sidecarActions: SidecarActions = mock[SidecarActions]
    val sidecarActor = TestActorRef(SidecarActor.props(sidecarActions))
    val defaultTimeout: FiniteDuration = 100.milliseconds

    final val serviceName = "service name"
    final val challengeString: String = UUID.randomUUID().toString
    final val sidecarId: UUID = UUID.randomUUID()
    final val commandId = "commandId"
    final val publicKey = "some public key"
    final val privateKey = "some private key"
    final val symmetricKey = "symmetric key"

  }

  trait ConnectedSidecar {
    this: Setup ⇒

    sidecarActor ! Connected(out.ref)
    out.expectNoMsg(defaultTimeout)
  }

  trait ChallengedSidecar extends ConnectedSidecar {
    this: Setup ⇒

    val keyResponse = CreateKeyResponse(sidecarId, publicKey, privateKey, symmetricKey)
    when(sidecarActions.registerSidecar(RegisterParameters(sidecarId, serviceName)))
      .thenReturn(Future(Right(RegisterResponse(Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString), keyResponse))))
    val registerCommand = Register(commandId, RegisterParameters(sidecarId, serviceName))
    sidecarActor ! registerCommand
    out.expectMsg(Responses.sidecarRegistered(commandId, RegisteredResult(sidecarId, privateKey, symmetricKey, challengeString)))
    val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challengeString)
  }

  trait RegisteredSidecar extends ChallengedSidecar {
    this: Setup ⇒

    when(sidecarActions.challenge(any(), any(), any())).thenReturn(Future(Right(SidecarAndActor(sidecar, sidecarActor))))
    val challengeCommandId: String = UUID.randomUUID().toString
    sidecarActor ! Challenge(challengeCommandId, ChallengeAnswer("", ""))

    out.expectMsg(Responses.challengeAccepted(challengeCommandId, ChallengeResult.success))
  }

  "initial" should "not respond to register command when not connected" in new Setup {
    sidecarActor ! Register(commandId, RegisterParameters(UUID.randomUUID(), ""))

    out.expectNoMsg(defaultTimeout)
  }

  it should "accept Connected message" in new Setup with ConnectedSidecar {
  }

  "when connected" should "stop self on Disconnect command" in new Setup with ConnectedSidecar {

    sidecarActor ! Disconnect

    sidecarActor.underlying.isTerminated shouldBe true
  }

  it should "send responses to out" in new Setup with ConnectedSidecar{
    private val response = Responses.commandError(commandId, KmsError.parseError)
    sidecarActor ! response

    out.expectMsg(response)
  }

  it should "send error to out when registration fails" in new Setup with ConnectedSidecar {
    val error = KmsError(500, "some message")
    when(sidecarActions.registerSidecar(RegisterParameters(sidecarId, serviceName))).thenReturn(Future(Left(error)))
    sidecarActor ! Register(commandId, RegisterParameters(sidecarId, serviceName))
    out.expectMsg(Responses.commandError(commandId, error))
  }

  it should "send registered to out when registering" in new Setup with ChallengedSidecar {

  }

  it should "send method not allowed to out when registering twice" in new Setup with ChallengedSidecar {
    sidecarActor ! registerCommand
    out.expectMsg(JsonResponse("2.0", None, Some(KmsError.methodNotAllowed("register")), commandId))
  }

  it should "send duplicate sidecar registered error to out" in new Setup with ConnectedSidecar {
    private val exists = KmsError.sidecarExistsError(sidecarId)
    when(sidecarActions.registerSidecar(any())).thenReturn(Future(Left(exists)))
    sidecarActor ! Register(commandId, RegisterParameters(sidecarId, serviceName))
    out.expectMsg(Responses.commandError(commandId, exists))
  }

  "when challenged" should "not respond to batch commands" in new Setup with ChallengedSidecar {
    private val command = SaveBatch(commandId, SaveBatchParameters(UUID.randomUUID(), "signature"))
    sidecarActor ! command

    out.expectMsg(Responses.methodNotAllowed(command))
  }

  it should "send responses to out" in new Setup with ConnectedSidecar {
    private val response = Responses.commandError(commandId, KmsError.parseError)
    sidecarActor ! response

    out.expectMsg(response)
  }

  it should "send error to socket if challenge fails" in new Setup with ChallengedSidecar {
    private val challengeError = KmsError.invalidRowSignature
    when(sidecarActions.challenge(any(), any(), any())).thenReturn(Future(Left(challengeError)))

    sidecarActor ! Challenge(commandId, ChallengeAnswer("", ""))
    out.expectMsg(Responses.commandError(commandId, challengeError))
  }

  it should "respond ok to challenge command" in new Setup with ChallengedSidecar {
    when(sidecarActions.challenge(any(), any(), any())).thenReturn(Future(Right(SidecarAndActor(sidecar, sidecarActor))))

    sidecarActor ! Challenge(commandId, ChallengeAnswer("batchSignature", "rowSignature"))

    out.expectMsg(Responses.challengeAccepted(commandId, ChallengeResult.success))
  }

  "when registered" should "save batch when registered and send batch to out" in new Setup with RegisteredSidecar {
    private val batchId = UUID.randomUUID()
    private val signature = "signature"

    private val batch = Batch(batchId, sidecarId, signature, Instant.now())
    private val parameters = SaveBatchParameters(batchId, signature)
    when(sidecarActions.createBatch(sidecar, parameters))
      .thenReturn(Future(Right(batch)))

    sidecarActor ! SaveBatch(commandId, parameters)
    out.expectMsg(Responses.batchCreated(commandId, batch))
  }

  it should "send health check request to web socket" in new Setup with RegisteredSidecar {
    private val healthCheckId = UUID.randomUUID()
    private val healthCheckLevel = HealthCheckLevel.Ping
    private val healthCheck = HealthCheck(healthCheckId, sidecarId, healthCheckLevel, Instant.now(), HealthCheckStatus.Pending)

    sidecarActor ! healthCheck

    out.expectMsg(Responses.healthCheckRequest(healthCheck))
  }

  it should "terminate sidecar and stop self when disconnected" in new Setup with RegisteredSidecar {
    when(sidecarActions.terminateSidecar(sidecar)).thenReturn(Future(Right(sidecar)))

    sidecarActor ! Disconnect
    sidecarActor.underlying.isTerminated shouldBe true
  }

  it should "send inquiry command to socket" in new Setup with RegisteredSidecar {
    private val now = Instant.now()

    private val inquiry = Inquiry(UUID.randomUUID(), serviceName, now, now, now, InquiryStatus.Created, sidecarId)
    sidecarActor ! inquiry
    out.expectMsg(Responses.inquiryCommand(inquiry))
  }

  "InquiryReply" should "be returned as invalid command when connected" in new Setup with ConnectedSidecar {
    sidecarActor ! InquiryReply(commandId, mock[InquiryReplyParameters])
    out.expectMsg(Responses.commandError(commandId, KmsError.methodNotAllowed("inquiry-response")))
  }

  it should "be returned as method not allowed when challenged" in new Setup with ChallengedSidecar {
    sidecarActor ! InquiryReply(commandId, mock[InquiryReplyParameters])
    out.expectMsg(Responses.commandError(commandId, KmsError.methodNotAllowed("inquiry-response")))
  }

  it should "not send anything to socket when complete" in new Setup with RegisteredSidecar {
    private val params = mock[InquiryReplyParameters]
    when(sidecarActions.inquiryResponse(sidecar,params)).thenReturn(Future((): Unit))
    sidecarActor ! InquiryReply(commandId, params)
    out.expectNoMsg(defaultTimeout)
    verify(sidecarActions, times(1)).inquiryResponse(sidecar, params)
  }
}
