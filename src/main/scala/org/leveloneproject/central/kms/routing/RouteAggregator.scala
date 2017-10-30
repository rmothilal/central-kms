package org.leveloneproject.central.kms.routing

import akka.http.scaladsl.server.{Directives, Route}
import com.google.inject.Inject

class RouteAggregator @Inject()(routes: Set[Router]) extends Directives {
  def route: Route = routes.map(_.route).reduceLeft((all, next) ⇒ all ~ next)
}
