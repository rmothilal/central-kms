package org.leveloneproject.central.kms.socket

import akka.http.scaladsl.model.ws.{Message, TextMessage}
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.util.JsonSerializer

trait OutputConverter extends JsonSerializer {
  def toMessage(value: AnyRef): Option[Message] = {
    def toOutput(value: AnyRef): Option[AnyRef] = {
      value match {
        case x: KmsError ⇒ Some(JsonResponse("2.0", None, Some(x), null)) // null id is required by JsonRPC spec
        case x: JsonMessage ⇒ Some(x)
        case _ ⇒ None
      }
    }

    toOutput(value).map(x ⇒ TextMessage.Strict(serialize(x)))
  }
}
