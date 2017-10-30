package org.leveloneproject.central.kms.sidecar

import java.util.UUID

import akka.actor.ActorRef
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.sidecars.ChallengeAnswer
import org.leveloneproject.central.kms.socket.{JsonMessage, JsonRequest, JsonResponse}
import org.leveloneproject.central.kms.util.JsonDeserializer

trait SidecarMessage

trait SidecarMessageParser extends JsonDeserializer {

  def parse(message: JsonMessage): Either[JsonResponse, SidecarMessage] = {
    message match {
      case request: JsonRequest ⇒ parseRequest(request)
      case response: JsonResponse ⇒ Right(CompleteRequest(response))
    }
  }

  private def parseRequest(request: JsonRequest): Either[JsonResponse, SidecarMessage] = {
    request.method match {
      case "register" ⇒ register(request)
      case "batch" ⇒ saveBatch(request)
      case "challenge" ⇒ challenge(request)
      case "inquiry-response" ⇒ inquiryReply(request)
      case _ ⇒ Left(Responses.commandError(request.id, KmsError.methodNotFound))
    }
  }

  private def challenge(request: JsonRequest): Either[JsonResponse, Challenge] = {
    extractParameters[ChallengeAnswer](request).map(Challenge(request.id, _))
  }

  private def inquiryReply(request: JsonRequest): Either[JsonResponse, InquiryReply] = {
    extractParameters[InquiryReplyParameters](request).map(InquiryReply(request.id, _))
  }

  private def saveBatch(request: JsonRequest): Either[JsonResponse, SaveBatch] = {
    extractParameters[SaveBatchParameters](request).map(SaveBatch(request.id, _))
  }

  private def register(request: JsonRequest): Either[JsonResponse, Register] = {
    extractParameters[RegisterParameters](request).map(Register(request.id, _))
  }

  private def extractParameters[T](request: JsonRequest)(implicit mf: Manifest[T]): Either[JsonResponse, T] = {
    request.params.flatMap(extractSafe[T]) match {
      case Some(x) ⇒ Right(x)
      case None ⇒ Left(Responses.commandError(request.id, KmsError.invalidParams))
    }
  }
}

abstract class Command(val method: String) extends SidecarMessage {
  val id: String
}

case class RegisterParameters(id: UUID, serviceName: String)

case class Register(id: String, params: RegisterParameters) extends Command("register")

case class SaveBatchParameters(id: UUID, signature: String)

case class SaveBatch(id: String, params: SaveBatchParameters) extends Command("batch")

case class Challenge(id: String, params: ChallengeAnswer) extends Command("challenge")

case class CompleteRequest(request: JsonResponse) extends SidecarMessage

case class Connected(socket: ActorRef) extends SidecarMessage

case class Disconnect() extends SidecarMessage

case class InquiryReply(id: String, params: InquiryReplyParameters) extends Command("inquiry-response")
case class InquiryReplyParameters(id: Option[UUID], body: Option[String], inquiry: UUID, total: Int, item: Int)
