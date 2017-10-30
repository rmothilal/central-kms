package org.leveloneproject.central.kms.domain.sidecars

import scala.concurrent.Future

trait SidecarLogsStore {
  def create(log: SidecarLog): Future[SidecarLog]
}
