package org.leveloneproject.central.kms.socket

import akka.http.scaladsl.server.Route
import com.google.inject.Inject
import org.leveloneproject.central.kms.routing.Router

class SocketRouter @Inject()(webSocketService: WebSocketService) extends Router {

  def route: Route = {
    path("sidecar") {
      get {
        handleWebSocketMessages(webSocketService.sidecarFlow())
      }
    }
  }
}
