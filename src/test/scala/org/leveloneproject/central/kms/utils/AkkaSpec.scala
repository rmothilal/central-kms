package org.leveloneproject.central.kms.utils

import akka.actor.ActorSystem
import org.scalatest.{BeforeAndAfterAll, Suite}

import scala.concurrent.Await
import scala.concurrent.duration._

trait AkkaSpec extends BeforeAndAfterAll {
  this: Suite ⇒

  implicit val system = ActorSystem()

  override protected def afterAll(): Unit = {
    Await.ready(system.terminate(), 10.seconds)
    super.afterAll()
  }
}
