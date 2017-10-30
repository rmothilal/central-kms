package org.leveloneproject.central.kms.socket

import java.util.UUID

import akka.http.scaladsl.model.ws.{BinaryMessage, TextMessage}
import akka.util.ByteString
import org.leveloneproject.central.kms.domain.sidecars.ChallengeAnswer
import org.leveloneproject.central.kms.sidecar._
import org.leveloneproject.central.kms.utils.MessageBuilder
import org.scalatest.{FlatSpec, Matchers}

class InputConverterSpec extends FlatSpec with Matchers with MessageBuilder {

  import org.leveloneproject.central.kms.domain.KmsError._
  import org.leveloneproject.central.kms.sidecar.Responses._

  trait Setup {
    val converter = new InputConverter {}
  }

  "fromMessage" should "convert BinaryMessage to ParseError" in new Setup {
    val message = BinaryMessage.Strict(ByteString("test"))
    converter.fromMessage(message) shouldBe parseError
  }

  it should "convert empty json to ParseError" in new Setup {
    val message = TextMessage.Strict("")
    converter.fromMessage(message) shouldBe parseError
  }

  it should "convert invalid json to ParseError" in new Setup {
    val message = TextMessage.Strict("invalid json")
    converter.fromMessage(message) shouldBe parseError
  }

  it should "convert non rpc request to InvalidRequest" in new Setup {
    val message = TextMessage.Strict("{}")
    converter.fromMessage(message) shouldBe invalidRequest
  }

  it should "convert unknown method to MethodNotFound" in new Setup {
    val message = TextMessage.Strict("""{"jsonrpc":"2.0","id":"test","method":"unknown","params":{}}""")
    converter.fromMessage(message) shouldBe commandError("test", methodNotFound)
  }

  it should "convert register method with bad parameters to InvalidParams" in new Setup {
    val message = TextMessage.Strict("""{"jsonrpc":"2.0","id":"test","method":"register","params":{}}""")
    converter.fromMessage(message) shouldBe commandError("test", invalidParams)
  }

  it should "convert register method with non UUID id to InvalidParams" in new Setup {
    val message = TextMessage.Strict("""{"jsonrpc":"2.0","id":"test","method":"register","params":{"id":"not a UUID","serviceName":"value"}}""")
    converter.fromMessage(message) shouldBe commandError("test", invalidParams)
  }

  it should "convert register method to RegisterCommand" in new Setup {
    private val sidecarId = UUID.randomUUID()
    private val serviceName = "value"
    private val requestId = "test"
    val message = TextMessage.Strict(registerRequest(requestId, sidecarId = sidecarId, serviceName = serviceName))
    converter.fromMessage(message) shouldBe Register(requestId, RegisterParameters(sidecarId, serviceName))
  }

  it should "convert batch method with no parameters to InvalidParams" in new Setup {
    val message = TextMessage.Strict("""{"jsonrpc":"2.0","id":"test","method":"batch","params":{}}""")
    converter.fromMessage(message) shouldBe commandError("test", invalidParams)
  }

  it should "convert batch method with invalid parameters to InvalidParams" in new Setup {
    val message = TextMessage.Strict(s"""{"jsonrpc":"2.0","id":"test","method":"batch","params":{"id":"${UUID.randomUUID()}","bad_parameter_name":"test"}}""")
    converter.fromMessage(message) shouldBe commandError("test", invalidParams)
  }

  it should "convert batch method with non UUID id to InvalidParams" in new Setup {
    val message = TextMessage.Strict("""{"jsonrpc":"2.0","id":"test","method":"batch","params":{"id":"not a UUID","signature":"value"}}""")
    converter.fromMessage(message) shouldBe commandError("test", invalidParams)
  }

  it should "convert batch method to BatchCommand" in new Setup {

    private val requestId = UUID.randomUUID().toString
    private val batchId = UUID.randomUUID()
    private val signature = "some signature"
    val message = TextMessage.Strict(batchRequest(requestId, batchId, signature))

    converter.fromMessage(message) shouldBe SaveBatch(requestId, SaveBatchParameters(batchId, signature))
  }

  it should "convert command response to CompleteRequest" in new Setup {
    private val commandId = UUID.randomUUID()
    val message = TextMessage.Strict(s"""{"jsonrpc":"2.0","result":{},"id":"$commandId"}""")

    converter.fromMessage(message) shouldBe CompleteRequest(JsonResponse("2.0", Some(Map()), None, commandId.toString))
  }

  "challenge" should "convert challenge request to Challenge command" in new Setup {
    private val commandId = UUID.randomUUID().toString
    private val batchSignature = UUID.randomUUID().toString
    private val rowSignature = UUID.randomUUID().toString
    val message = TextMessage.Strict(s"""{"jsonrpc":"2.0","id":"$commandId","method":"challenge","params":{"batchSignature":"$batchSignature","rowSignature":"$rowSignature"}}""")

    converter.fromMessage(message) shouldBe Challenge(commandId, ChallengeAnswer(batchSignature, rowSignature))
  }

  "inquiry-response" should "convert inquiry-response to InquiryReply command" in new Setup {
    private val commandId = UUID.randomUUID().toString
    private val inquiryId = UUID.randomUUID()
    private val batchId = UUID.randomUUID()
    private val total = 10
    private val item = 3
    private val body = UUID.randomUUID().toString

    val message = TextMessage(s"""{"jsonrpc":"2.0","id":"$commandId","method":"inquiry-response","params":{"id":"$batchId","inquiry":"$inquiryId","body":"$body","total":$total,"item":$item}}""")
    converter.fromMessage(message) shouldBe InquiryReply(commandId, InquiryReplyParameters(Some(batchId), Some(body), inquiryId, total, item))
  }

  it should "convert empty inquiry-response to InquiryReply command" in new Setup {
    private val commandId = UUID.randomUUID().toString
    private val inquiryId = UUID.randomUUID()

    val message = TextMessage(s"""{"jsonrpc":"2.0","id":"$commandId","method":"inquiry-response","params":{"inquiry":"$inquiryId","total":0,"item":0}}""")
    converter.fromMessage(message) shouldBe InquiryReply(commandId, InquiryReplyParameters(None, None, inquiryId, 0, 0))
  }

}
