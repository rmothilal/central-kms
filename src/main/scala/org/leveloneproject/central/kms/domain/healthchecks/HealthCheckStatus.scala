package org.leveloneproject.central.kms.domain.healthchecks

sealed abstract class HealthCheckStatus(val id: Int, val value: String)


object HealthCheckStatus {
  def apply(status: String): HealthCheckStatus = status match {
    case "pending" ⇒ Pending
  }

  def apply(id: Int): HealthCheckStatus = id match {
    case 1 ⇒ Pending
  }

  case object Pending extends HealthCheckStatus(1, "pending")

}
