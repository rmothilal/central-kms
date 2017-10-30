package org.leveloneproject.central.kms.domain.sidecars

sealed abstract class SidecarStatus(val id: Int, val value: String)

object SidecarStatus {
  def apply(id: Int): SidecarStatus = id match {
    case 1 ⇒ Challenged
    case 2 ⇒ Registered
    case 3 ⇒ Terminated
    case 4 ⇒ Suspended
  }

  case object Challenged extends SidecarStatus(1, "challenged")
  case object Registered extends SidecarStatus(2, "registered")
  case object Terminated extends SidecarStatus(3, "terminated")
  case object Suspended extends SidecarStatus(4, "suspended")
}


