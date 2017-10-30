package org.leveloneproject.central.kms.domain.sidecars

import java.util.UUID

import akka.actor.ActorRef

import scala.collection.mutable

class SidecarList {

  lazy val sidecars = new mutable.LinkedHashMap[UUID, SidecarAndActor]()

  def registered(): Seq[Sidecar] = sidecars.values.map(_.sidecar).filter(_.status == SidecarStatus.Registered).toSeq

  def register(sidecarWithActor: SidecarAndActor): Unit = sidecars += (sidecarWithActor.sidecar.id → sidecarWithActor)

  def unregister(id: UUID): Unit = sidecars -= id

  def actorById(id: UUID): Option[ActorRef] = {
    sidecars.get(id).map(_.actor)
  }

  def byName(name: String): Option[SidecarAndActor] = {
    sidecars.values.filter(s ⇒ s.serviceName == name && s.status == SidecarStatus.Registered).lastOption
  }
}
