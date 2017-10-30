package org.leveloneproject.central.kms.domain.healthchecks

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.google.inject.Inject
import org.leveloneproject.central.kms.routing.Router

class HealthCheckRouter @Inject()(healthCheckService: HealthCheckService) extends Router {
  def route: Route = pathPrefix("sidecars") {
    pathPrefix(JavaUUID) { id ⇒
      path("healthchecks") {
        (post & entity(as[CreateRequest])) { req ⇒
          onSuccess(healthCheckService.create(CreateHealthCheckRequest(id, req.level))) {
            case Right(a) ⇒ complete(a)
            case Left(e) ⇒ complete(StatusCodes.BadRequest → e)
          }
        }
      }
    }
  }
}

case class CreateRequest(level: HealthCheckLevel)
