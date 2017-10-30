package org.leveloneproject.central.kms.socket

import java.util.UUID

import akka.http.scaladsl.model.ws.TextMessage
import org.leveloneproject.central.kms.domain._
import org.leveloneproject.central.kms.domain.sidecars.ChallengeResult
import org.leveloneproject.central.kms.sidecar.Responses
import org.scalatest.{FlatSpec, Matchers}

class OutputConverterSpec extends FlatSpec with Matchers with OutputConverter {

  "toMessage" should "convert input error to rpc error response" in {
    val error = KmsError(14, "some message")

    toMessage(error) shouldBe Some(TextMessage.Strict("""{"jsonrpc":"2.0","error":{"code":14,"message":"some message"},"id":null}"""))
  }

  it should "convert JsonResponse to Some JsonResponse" in {
    val response = JsonResponse("2.0", None, Some(KmsError(100, "some message")), "commandId")

    toMessage(response) shouldBe Some(TextMessage.Strict("""{"jsonrpc":"2.0","error":{"code":100,"message":"some message"},"id":"commandId"}"""))
  }

  it should "return None for other Types" in {
    toMessage(SomeResponse(UUID.randomUUID(), 1000)) shouldBe None
    toMessage("some string") shouldBe None
  }

  it should "convert challengeAccepted to json response" in {
    val commandId = UUID.randomUUID().toString
    val response = Responses.challengeAccepted(commandId, ChallengeResult.success)
    toMessage(response) shouldBe Some(TextMessage.Strict(s"""{"jsonrpc":"2.0","result":{"status":"OK"},"id":"$commandId"}"""))
  }
}

case class SomeResponse(id: UUID, value: Int)