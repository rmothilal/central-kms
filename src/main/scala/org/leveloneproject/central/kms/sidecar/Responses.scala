package org.leveloneproject.central.kms.sidecar

import java.time.Instant
import java.util.UUID

import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.batches.Batch
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheck
import org.leveloneproject.central.kms.domain.inquiries.Inquiry
import org.leveloneproject.central.kms.domain.sidecars.ChallengeResult
import org.leveloneproject.central.kms.socket.{JsonRequest, JsonResponse}

case class RegisteredResult(id: UUID, batchKey: String, rowKey: String, challenge: String)

object Responses {
  private val version = "2.0"

  def healthCheckRequest(healthCheck: HealthCheck) = JsonRequest(version, healthCheck.id.toString, "healthcheck", Some('level → healthCheck.level))

  def batchCreated(commandId: String, batch: Batch) = JsonResponse(version, Some('id → batch.id), None, commandId)

  def sidecarRegistered(commandId: String, result: RegisteredResult) = JsonResponse(version, Some(result), None, commandId)

  def challengeAccepted(commandId: String, result: ChallengeResult) = JsonResponse(version, Some(result), None, commandId)

  def methodNotAllowed(command: Command) = JsonResponse(version, None, Some(KmsError.methodNotAllowed(command.method)), command.id)

  def commandError(commandId: String, error: AnyRef) = JsonResponse(version, None, Some(error), commandId)

  def inquiryCommand(inquiry: Inquiry) = JsonRequest(version, inquiry.id.toString, "inquiry", Some(InquiryCommandParameters(inquiry)))
}

case class InquiryCommandParameters(inquiry: UUID, startTime: Instant, endTime: Instant)

object InquiryCommandParameters {
  def apply(inquiry: Inquiry): InquiryCommandParameters = InquiryCommandParameters(inquiry.id, inquiry.startTime, inquiry.endTime)
}

