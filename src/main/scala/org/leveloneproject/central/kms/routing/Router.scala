package org.leveloneproject.central.kms.routing

import akka.http.scaladsl.server.{Directives, Route}

trait Router extends Directives with JsonSupport {
  def route: Route
}
