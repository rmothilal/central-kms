package org.leveloneproject.central.kms.socket

import java.time.Instant
import java.util.UUID

import akka.http.scaladsl.testkit.{ScalatestRouteTest, WSProbe}
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.batches.Batch
import org.leveloneproject.central.kms.domain.keys.CreateKeyResponse
import org.leveloneproject.central.kms.domain.sidecars._
import org.leveloneproject.central.kms.sidecar.{SaveBatchParameters, SidecarActions}
import org.leveloneproject.central.kms.utils.MessageBuilder
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class SocketRouteSpec extends FlatSpec with Matchers with MockitoSugar with ScalatestRouteTest with MessageBuilder {

  trait Setup {
    final val sidecarActions: SidecarActions = mock[SidecarActions]
    final val webSocketService: WebSocketService = new WebSocketService(sidecarActions)
    final val sidecarId: UUID = UUID.randomUUID()
    final val serviceName: String = "some service"
    final val challenge: String = UUID.randomUUID().toString
    final val publicKey: String = "public key"
    final val privateKey: String = "batch key"
    final val rowKey: String = "row key"

    def setupRegistration(): Sidecar = {
      val keyResponse = CreateKeyResponse(sidecarId, publicKey, privateKey, rowKey)
      val sidecar = Sidecar(sidecarId, serviceName, SidecarStatus.Challenged, challenge)
      when(sidecarActions.registerSidecar(any())).thenReturn(Future(Right(RegisterResponse(sidecar, keyResponse))))
      when(sidecarActions.challenge(any(), any(), any())).thenAnswer(i ⇒ Future(Right(i.getArgument[SidecarAndActor](0))))
      sidecar
    }
  }

  "socket router" should "be able to connect to web socket route" in new Setup {
    private val socketRouter = new SocketRouter(webSocketService)
    private val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      isWebSocketUpgrade shouldBe true
    }
  }

  it should "return error for invalid command" in new Setup {
    private val parseError = KmsError.parseError
    private val socketRouter = new SocketRouter(webSocketService)
    private val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage("test")
      wsClient.expectMessage(s"""{"jsonrpc":"2.0","error":{"code":${parseError.code},"message":"${parseError.message}"},"id":null}""")
    }
  }

  it should "return registered for register command" in new Setup {
    setupRegistration()
    private val requestId = "test"
    private val socketRouter = new SocketRouter(webSocketService)
    private val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage(registerRequest(requestId, sidecarId, serviceName))
      wsClient.expectMessage(registerResponse(requestId, sidecarId, privateKey, rowKey, challenge))
    }
  }

  it should "return method not allowed error when already registered" in new Setup {
    setupRegistration()
    private val socketRouter = new SocketRouter(webSocketService)
    private val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage(registerRequest("test", sidecarId, serviceName))
      wsClient.expectMessage(registerResponse("test", sidecarId, privateKey, rowKey, challenge))
      wsClient.sendMessage(registerRequest("test2"))
      wsClient.expectMessage(s"""{"jsonrpc":"2.0","error":{"code":-32601,"message":"'register' method not allowed in current state"},"id":"test2"}""")
    }
  }

  it should "return batch id when registered sidecar issues batch" in new Setup {
    private val sidecar = setupRegistration()
    private val batchId = UUID.randomUUID()
    private val signature = "some signature"
    val socketRouter = new SocketRouter(webSocketService)
    when(sidecarActions.createBatch(sidecar, SaveBatchParameters(batchId, signature))).thenReturn(Future(Right(Batch(batchId, sidecarId, signature, Instant.now()))))
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage(registerRequest("register1", sidecarId, serviceName))
      wsClient.expectMessage(registerResponse("register1", sidecarId, privateKey, rowKey, challenge))
      wsClient.sendMessage(challengeRequest("challenge1"))
      wsClient.expectMessage(challengeResponse("challenge1"))
      wsClient.sendMessage(batchRequest("batch1", batchId, signature))
      wsClient.expectMessage(batchResponse("batch1", batchId))
    }
  }

  it should "terminate client on challenge failure" in new Setup {
    private val challengeRequestId = UUID.randomUUID.toString
    setupRegistration()
    private val invalidRowSignature = KmsError.invalidRowSignature
    when(sidecarActions.challenge(any(), any(), any())).thenReturn(Future(Left(invalidRowSignature)))
    val socketRouter = new SocketRouter(webSocketService)
    val wsClient = WSProbe()
    WS("/sidecar", wsClient.flow) ~> socketRouter.route ~> check {
      wsClient.sendMessage(registerRequest("register1", sidecarId, serviceName))
      wsClient.expectMessage(registerResponse("register1", sidecarId, privateKey, rowKey, challenge))
      wsClient.sendMessage(challengeRequest(challengeRequestId))
      wsClient.expectMessage(s"""{"jsonrpc":"2.0","error":{"code":${invalidRowSignature.code},"message":"${invalidRowSignature.message}"},"id":"$challengeRequestId"}""")
      wsClient.expectCompletion()
    }
  }
}
