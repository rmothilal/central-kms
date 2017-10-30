package org.leveloneproject.central.kms.domain.sidecars

import java.util.UUID

import akka.actor.ActorRef

case class SidecarRef(id: UUID, actor: ActorRef)
