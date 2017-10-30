package org.leveloneproject.central.kms.domain.sidecars

import akka.http.scaladsl.server.Route
import com.google.inject.Inject
import org.leveloneproject.central.kms.routing.Router

class SidecarRouter @Inject()(sidecarService: SidecarService) extends Router {
  def route: Route = {
    path("sidecars") {
      get {
        complete(sidecarService.active())
      }
    }
  }
}
