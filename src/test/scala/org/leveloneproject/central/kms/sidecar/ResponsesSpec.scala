package org.leveloneproject.central.kms.sidecar

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.domain.batches.Batch
import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheck, HealthCheckLevel, HealthCheckStatus}
import org.leveloneproject.central.kms.domain.inquiries.{Inquiry, InquiryStatus}
import org.leveloneproject.central.kms.util.JsonSerializer
import org.scalatest.{FlatSpec, Matchers}

class ResponsesSpec extends FlatSpec with Matchers with JsonSerializer {

  import Responses._

  trait Setup {
    val now: Instant = Instant.now()
    val sidecarId: UUID = UUID.randomUUID()
    val commandId: String = UUID.randomUUID().toString
  }

  "serialize response" should "serialize HealthCheckRequest" in new Setup {
    private val healthCheckId = UUID.randomUUID()
    private val level = HealthCheckLevel.Ping
    private val healthCheck = HealthCheck(healthCheckId, sidecarId, level, now, HealthCheckStatus.Pending)
    private val result = serialize(healthCheckRequest(healthCheck))
    result shouldBe s"""{"jsonrpc":"2.0","id":"$healthCheckId","method":"healthcheck","params":{"level":"ping"}}"""
  }

  it should "serialize batchCreated" in new Setup {
    private val batchId = UUID.randomUUID()
    private val batch = Batch(batchId, sidecarId, "signature", now)
    private val result = serialize(batchCreated(commandId, batch))
    result shouldBe s"""{"jsonrpc":"2.0","result":{"id":"$batchId"},"id":"$commandId"}"""
  }

  it should "serialize registered" in new Setup {
    private val batchKey = "batch key"
    private val rowKey = "row key"
    private val challenge = "challenge"
    private val registeredResult = RegisteredResult(sidecarId, batchKey, rowKey, challenge)
    private val result = serialize(sidecarRegistered(commandId, registeredResult))
    result shouldBe s"""{"jsonrpc":"2.0","result":{"id":"$sidecarId","batchKey":"$batchKey","rowKey":"$rowKey","challenge":"$challenge"},"id":"$commandId"}"""
  }

  it should "serialize inquiry command" in new Setup {
    private val inquiryId = UUID.randomUUID()
    private val startTime = now.minusSeconds(360000)
    private val endTime = now.minusSeconds(10000)
    private val inquiry = Inquiry(inquiryId, "service name", startTime, endTime, now, InquiryStatus.Created, sidecarId)

    private val result = serialize(inquiryCommand(inquiry))
    result shouldBe s"""{"jsonrpc":"2.0","id":"$inquiryId","method":"inquiry","params":{"inquiry":"$inquiryId","startTime":"${startTime}","endTime":"${endTime}"}}"""
  }
}
