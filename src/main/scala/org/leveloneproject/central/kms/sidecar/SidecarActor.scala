package org.leveloneproject.central.kms.sidecar

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill, Props}
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheck
import org.leveloneproject.central.kms.domain.inquiries.Inquiry
import org.leveloneproject.central.kms.domain.sidecars._
import org.leveloneproject.central.kms.socket.{JsonRequest, JsonResponse}
import org.leveloneproject.central.kms.util.JsonSerializer

import scala.language.implicitConversions

class SidecarActor(sidecarActions: SidecarActions) extends Actor with JsonSerializer with ActorLogging {

  import Responses._
  import context._

  val requests = collection.mutable.HashMap.empty[String, (String, AnyRef) ⇒ Unit]

  def connected(out: ActorRef): Receive = {
    case Register(id, registerParameters) ⇒
      log.info("Registering sidecar {}", registerParameters.id)
      sidecarActions.registerSidecar(registerParameters) map {
        case Right(response) ⇒
          become(challenged(SidecarAndOutSocket(response.sidecar, out), ChallengeKeys(response.keyResponse.publicKey, response.keyResponse.symmetricKey)))
          out ! sidecarRegistered(id, response)
        case Left(error) ⇒ out ! commandError(id, error)
      }
    case command: Command ⇒ out ! methodNotAllowed(command)
    case Disconnect ⇒ terminate()
    case x ⇒ out ! x
  }

  def challenged(sidecarAndOutSocket: SidecarAndOutSocket, keys: ChallengeKeys): Receive = {
    case Challenge(id, answer) ⇒ sidecarActions.challenge(SidecarAndActor(sidecarAndOutSocket, self), keys, answer) map {
      case Right(sWithActor) ⇒
        become(registered(SidecarAndOutSocket(sWithActor, sidecarAndOutSocket)))
        sidecarAndOutSocket.out ! challengeAccepted(id, ChallengeResult.success)
      case Left(error) ⇒ disconnectClientWithMessage(sidecarAndOutSocket.out, commandError(id, error))
    }

    case command: Command ⇒ sidecarAndOutSocket.out ! methodNotAllowed(command)
    case Disconnect ⇒ terminate()
    case x ⇒ sidecarAndOutSocket.out ! x
  }

  def registered(sidecarAndOutSocket: SidecarAndOutSocket): Receive = {
    case CompleteRequest(jsonResponse) ⇒ handleRequest(jsonResponse)
    case SaveBatch(id, params) ⇒
      sidecarActions.createBatch(sidecarAndOutSocket, params).map {
        _.fold(e ⇒ commandError(id, e), batch ⇒ {
          log.info("Batch {} created", batch.id)
          batchCreated(id, batch)
        })
      }.map { result ⇒ sidecarAndOutSocket.out ! result }

    case InquiryReply(_, params) ⇒
      log.info("Handling inquiry reply for inquiry={}, item={}", params.inquiry, params.item)
      sidecarActions.inquiryResponse(sidecarAndOutSocket, params)

    case healthCheck: HealthCheck ⇒ request(sidecarAndOutSocket, healthCheckRequest(healthCheck), completeHealthCheck)
    case inquiry: Inquiry ⇒ sidecarAndOutSocket.out ! inquiryCommand(inquiry)
    case command: Command ⇒ sidecarAndOutSocket.out ! methodNotAllowed(command)
    case Disconnect ⇒ sidecarActions.terminateSidecar(sidecarAndOutSocket) map { _ ⇒ terminate() }
    case x ⇒ sidecarAndOutSocket.out ! x
  }

  def receive: Receive = {
    case Connected(socket) ⇒ become(connected(socket))
  }

  private def request(out: ActorRef, request: JsonRequest, handler: (String, AnyRef) ⇒ Unit) = {
    requests += request.id → handler
    out ! request
  }

  private def disconnectClientWithMessage[T](client: ActorRef, message: T) = {
    client ! message
    client ! PoisonPill
    terminate()
  }

  private def terminate(): Unit = {
    requests.clear()
    stop(self)
  }

  private def handleRequest(request: JsonResponse): Unit = {
    requests.remove(request.id).foreach((req: (String, AnyRef) ⇒ Unit) ⇒ {
      (request.result, request.error) match {
        case (Some(result), _) ⇒ req(request.id, result)
        case _ ⇒ //ignore
      }
    })
  }

  private def completeHealthCheck(healthCheckId: String, result: AnyRef): Unit = {
    sidecarActions.completeHealthCheck(UUID.fromString(healthCheckId), serialize(result))
  }

  private implicit def toRegisteredResult(response: RegisterResponse): RegisteredResult = RegisteredResult(response.sidecar.id, response.keyResponse.privateKey, response.keyResponse.symmetricKey, response.sidecar.challenge)

  private case class SidecarAndOutSocket(sidecar: Sidecar, out: ActorRef)

  private object SidecarAndOutSocket {
    implicit def toSidecar(sidecarAndOutSocket: SidecarAndOutSocket): Sidecar = sidecarAndOutSocket.sidecar

    implicit def toOutSocket(sidecarAndOutSocket: SidecarAndOutSocket): ActorRef = sidecarAndOutSocket.out
  }

}

object SidecarActor {
  def props(sidecarActions: SidecarActions) = Props(new SidecarActor(sidecarActions))

}
