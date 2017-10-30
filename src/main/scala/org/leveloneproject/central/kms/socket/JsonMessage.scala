package org.leveloneproject.central.kms.socket

sealed trait JsonMessage

case class StopClient()

case class JsonRequest(jsonrpc: String, id: String, method: String, params: Option[AnyRef]) extends JsonMessage

case class JsonResponse(jsonrpc: String, result: Option[AnyRef], error: Option[AnyRef], id: String) extends JsonMessage
