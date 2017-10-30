package org.leveloneproject.central.kms

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.google.inject.Inject
import org.leveloneproject.central.kms.persistance.Migrator
import org.leveloneproject.central.kms.routing.{RouteAggregator, Router}

case class Service @Inject()(system: ActorSystem, materializer: ActorMaterializer, migrator: Migrator, routeAggregator: RouteAggregator) extends Router {
  def migrate(): Unit = {
    migrator.migrate()
  }

  def route: Route = routeAggregator.route
}
