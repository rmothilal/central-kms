package org.leveloneproject.central.kms.socket

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream._
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.google.inject.Inject
import org.leveloneproject.central.kms.sidecar._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class WebSocketService @Inject()(sidecarSupport: SidecarActions)
                                (implicit val system: ActorSystem, implicit val materializer: ActorMaterializer) extends InputConverter with OutputConverter {

  private def toStrictText(implicit mat: Materializer): Flow[Message, TextMessage.Strict, NotUsed] = {
    Flow[Message]
      .map(_.asTextMessage)
        .mapAsync(3) {
          case t: TextMessage.Streamed ⇒ t.textStream.runFold("")(_ ++ _)
          case t: TextMessage.Strict ⇒ Future(t.getStrictText)
        }
      .map(TextMessage.Strict)
  }

  private def commandExecutionFlow(sidecarActor: ActorRef): Flow[AnyRef, AnyRef, NotUsed] = {
    val inputFlow = Flow[AnyRef].to(Sink.actorRef(sidecarActor, Disconnect))

    val outputFlow = Source.actorRef[AnyRef](100, OverflowStrategy.dropTail)
      .mapMaterializedValue(sidecarActor ! Connected(_))

    Flow.fromSinkAndSource(inputFlow, outputFlow)
  }

  def sidecarFlow(): Flow[Message, Message, NotUsed] = {
    val sidecarActor = system.actorOf(SidecarActor.props(sidecarSupport))

    Flow[Message]
      .via(toStrictText)
      .map(fromMessage)
      .via(commandExecutionFlow(sidecarActor))
      .map(a ⇒ toMessage(a).orNull)
  }
}

