package org.leveloneproject.central.kms.domain.sidecars

import akka.actor.ActorRef
import scala.language.implicitConversions

case class SidecarAndActor(sidecar: Sidecar, actor: ActorRef)

object SidecarAndActor {
  implicit def toSidecar(sidecarAndActor: SidecarAndActor): Sidecar = sidecarAndActor.sidecar

  implicit def toActor(sidecarAndActor: SidecarAndActor): ActorRef = sidecarAndActor.actor
}

