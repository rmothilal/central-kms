package org.leveloneproject.central.kms.domain.sidecars

import org.leveloneproject.central.kms.domain.keys.CreateKeyResponse

case class RegisterResponse(sidecar: Sidecar, keyResponse: CreateKeyResponse)
