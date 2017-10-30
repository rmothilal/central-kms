package org.leveloneproject.central.kms.util

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

class FutureEither[L, R](val future: Future[Either[L, R]]) {
  def flatMap[R2](block: R ⇒ FutureEither[L, R2]): FutureEither[L, R2] = {
    val result = future.flatMap {
      case Right(r) ⇒ block(r).future
      case Left(l) ⇒ Future(Left(l))
    }
    new FutureEither(result)
  }

  def map[R2](block: R ⇒ R2): FutureEither[L, R2] = {
    val result = future.map {
      case Right(r) ⇒ Right(block(r))
      case Left(l) ⇒ Left(l)
    }
    new FutureEither(result)
  }

  def recover(block: PartialFunction[L, R]): Future[R] = {
    future.map {
      case Right(r) ⇒ r
      case Left(l) ⇒ block(l)
    }
  }
}

object FutureEither {
  def apply[L, R](future: Future[Either[L,R]]): FutureEither[L,R] = new FutureEither(future)
  implicit def toFutureEither[L, R](future: Future[Either[L, R]]): FutureEither[L, R] = new FutureEither(future)
  implicit def fromFutureEither[L, R](fe: FutureEither[L,R]): Future[Either[L, R]] = fe.future
}