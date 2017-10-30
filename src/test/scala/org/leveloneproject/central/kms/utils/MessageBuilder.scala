package org.leveloneproject.central.kms.utils

import java.util.UUID

import org.leveloneproject.central.kms.util.JsonSerializer

trait MessageBuilder extends JsonSerializer {
  def batchRequest(
    requestId: String = UUID.randomUUID().toString,
    batchId: UUID = UUID.randomUUID(),
    signature: String = ""
    ): String =
    s"""{"jsonrpc":"2.0","id":"$requestId","method":"batch","params":{"id":"$batchId","signature":"$signature"}}"""

  def batchResponse(
   requestId: String = UUID.randomUUID().toString,
   batchId: UUID = UUID.randomUUID()
   ): String = s"""{"jsonrpc":"2.0","result":{"id":"$batchId"},"id":"$requestId"}"""

  def challengeRequest(requestId: String): String = s"""{"jsonrpc":"2.0","id":"$requestId","method":"challenge","params":{"batchSignature":"","rowSignature":""}}"""

  def challengeResponse(requestId: String): String = s"""{"jsonrpc":"2.0","result":{"status":"OK"},"id":"$requestId"}"""

  def registerRequest(
   requestId: String = UUID.randomUUID().toString,
   sidecarId: UUID = UUID.randomUUID(),
   serviceName: String = "service name"): String =
    s"""{"jsonrpc":"2.0","id":"$requestId","method":"register","params":{"id":"$sidecarId","serviceName":"$serviceName"}}"""

  def registerResponse(
    requestId: String = UUID.randomUUID().toString,
    sidecarId: UUID = UUID.randomUUID(),
    batchKey: String = "",
    rowKey: String = "",
    challenge: String = ""): String =
      s"""{"jsonrpc":"2.0","result":{"id":"$sidecarId","batchKey":"$batchKey","rowKey":"$rowKey","challenge":"$challenge"},"id":"$requestId"}"""

}
