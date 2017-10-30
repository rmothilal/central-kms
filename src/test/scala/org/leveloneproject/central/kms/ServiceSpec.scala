package org.leveloneproject.central.kms

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import org.leveloneproject.central.kms.persistance.Migrator
import org.leveloneproject.central.kms.routing.RouteAggregator
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class ServiceSpec extends FlatSpec with Matchers with MockitoSugar {

  trait Setup {
    val migrator: Migrator = mock[Migrator]
    val routeAggregator: RouteAggregator = mock[RouteAggregator]
    val service = Service(mock[ActorSystem], mock[ActorMaterializer], migrator, routeAggregator)
  }

  it should "call migrator migrate" in new Setup {
    service.migrate()

    verify(migrator).migrate()
  }

  it should "return routeAggregator route" in new Setup {
    val route: Route = mock[Route]
    when(routeAggregator.route).thenReturn(route)

    assert(service.route === route)
  }

}
