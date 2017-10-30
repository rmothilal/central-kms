package org.leveloneproject.central.kms.domain

import java.util.UUID

sealed case class KmsError(code: Int, message: String, data: Option[Any] = None)

object KmsError {

  def invalidDateRange = KmsError(40010, "Date range must be greater than 0 and less than 30 days")

  def healthCheckDoesNotExist = KmsError(121, "Health check does not exist")

  def parseError = KmsError(-32700, "Parse error")

  def invalidRequest = KmsError(-32600, "Invalid Request")

  def methodNotFound = KmsError(-32601, "Method not found")

  def invalidParams = KmsError(-32602, "Invalid params")

  def internalError = KmsError(-32603, "Internal error")

  def sidecarExistsError(id: UUID) = KmsError(40001, "Sidecar with id '%s' already exists".format(id))

  def batchExistsError(id: UUID) = KmsError(120, "Batch with id '%s' already exists".format(id))

  def methodNotAllowed(method: String) = KmsError(-32601, s"'$method' method not allowed in current state")

  def notFound(resource: String, id: UUID) = KmsError(40400, s"$resource with id of $id does not exist")

  def unregisteredSidecar(id: UUID) = KmsError(40401, "Sidecar '%s' is not registered".format(id))

  def unregisteredSidecar(name: String) = KmsError(40002, s"No current sidecars are registered for service '${name}'")

  val verificationFailed = KmsError(100, "Verification of signature failed")

  val invalidBatchSignature = KmsError(40004, "Invalid batch signature")

  val invalidRowSignature = KmsError(40005, "Invalid row signature")
}

