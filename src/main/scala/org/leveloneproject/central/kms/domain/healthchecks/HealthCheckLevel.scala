package org.leveloneproject.central.kms.domain.healthchecks

sealed abstract class HealthCheckLevel(val id: Int, val value: String)

object HealthCheckLevel {
  def apply(id: Int): HealthCheckLevel = id match {
    case 1 ⇒ Ping
  }

  case object Ping extends HealthCheckLevel(1, "ping")

}


