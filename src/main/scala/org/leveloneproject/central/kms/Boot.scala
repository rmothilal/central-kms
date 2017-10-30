package org.leveloneproject.central.kms

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler}
import com.google.inject.Guice
import com.typesafe.config.ConfigFactory
import org.leveloneproject.central.kms.config.MainModule

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

object Boot extends App {

  val config = ConfigFactory.load("common")
  val injector = Guice.createInjector(new MainModule(config))

  val service = injector.getInstance(classOf[Service])
  service.migrate()
  implicit val system = service.system
  implicit val materializer = service.materializer

  implicit val rejectionHandler: RejectionHandler = ErrorHandlers.rejectionHandler
  implicit val exceptionHandler: ExceptionHandler = ErrorHandlers.exceptionHandler

  Http().bindAndHandle(service.route, "0.0.0.0", 8080)

}


