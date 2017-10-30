package org.leveloneproject.central.kms.domain.sidecars

import java.util.UUID

import org.leveloneproject.central.kms.domain.KmsError

import scala.concurrent.Future

trait SidecarStore {

  def create(sidecar: Sidecar): Future[Either[KmsError, Sidecar]]

  def updateStatus(id: UUID, sidecarStatus: SidecarStatus): Future[Int]
}
