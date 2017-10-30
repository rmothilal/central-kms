package org.leveloneproject.central.kms

import scala.concurrent.{Await, Awaitable, ExecutionContext}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

trait AwaitResult {
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, 4.seconds)
}
