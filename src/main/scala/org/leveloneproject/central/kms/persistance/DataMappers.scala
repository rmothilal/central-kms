package org.leveloneproject.central.kms.persistance

import java.sql.Timestamp
import java.time.Instant

import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheckLevel, HealthCheckStatus}
import org.leveloneproject.central.kms.domain.inquiries.InquiryStatus
import org.leveloneproject.central.kms.domain.sidecars.SidecarStatus
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType

trait DataMappers {
  this: DbProfile ⇒

  import profile.api._

  implicit val instantMapper: JdbcType[Instant] with BaseTypedType[Instant] = MappedColumnType.base[Instant, Timestamp](
    i ⇒ Timestamp.from(i),
    ts ⇒ ts.toInstant
  )

  implicit val healthCheckLevelMapper: JdbcType[HealthCheckLevel] with BaseTypedType[HealthCheckLevel] = MappedColumnType.base[HealthCheckLevel, Int](
    l ⇒ l.id,
    s ⇒ HealthCheckLevel(s)
  )

  implicit val healthCheckStatusMapper: JdbcType[HealthCheckStatus] with BaseTypedType[HealthCheckStatus] = MappedColumnType.base[HealthCheckStatus, Int](
    status ⇒ status.id,
    status ⇒ HealthCheckStatus(status)
  )

  implicit val sidecarStatusMapper: JdbcType[SidecarStatus] with BaseTypedType[SidecarStatus] = MappedColumnType.base[SidecarStatus, Int](
    status ⇒ status.id,
    status ⇒ SidecarStatus(status)
  )

  implicit val inquiryStatusMapper: JdbcType[InquiryStatus] with BaseTypedType[InquiryStatus] = MappedColumnType.base[InquiryStatus, Int](
    status ⇒ status.id,
    status ⇒ InquiryStatus(status)
  )
}
