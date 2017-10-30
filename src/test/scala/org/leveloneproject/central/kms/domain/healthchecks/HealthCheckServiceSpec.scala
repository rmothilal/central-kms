package org.leveloneproject.central.kms.domain.healthchecks

import java.time.Instant
import java.util.UUID

import akka.testkit.TestProbe
import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheckLevel.Ping
import org.leveloneproject.central.kms.domain.healthchecks.HealthCheckStatus.Pending
import org.leveloneproject.central.kms.domain.sidecars.SidecarList
import org.leveloneproject.central.kms.util.{IdGenerator, InstantProvider}
import org.leveloneproject.central.kms.utils.AkkaSpec
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class HealthCheckServiceSpec extends FlatSpec with AkkaSpec with Matchers with MockitoSugar with AwaitResult {

  trait Setup {
    final val store: HealthCheckStore = mock[HealthCheckStore]
    final val currentInstant: Instant = Instant.now()
    final val sidecarList: SidecarList = mock[SidecarList]
    final val healthCheckId: UUID = UUID.randomUUID()

    val service: HealthCheckService = new HealthCheckService(store, sidecarList) with IdGenerator with InstantProvider {
      override def newId(): UUID = healthCheckId

      override def now(): Instant = currentInstant
    }

    final val sidecarId: UUID = UUID.randomUUID()
  }

  "create" should "create, save and return new HealthCheck" in new Setup {
    val request = CreateHealthCheckRequest(sidecarId, Ping)

    private val check = HealthCheck(healthCheckId, sidecarId, Ping, currentInstant, Pending)

    private val sidecarProbe = TestProbe()
    when(sidecarList.actorById(sidecarId)).thenReturn(Some(sidecarProbe.ref))
    when(store.create(check)).thenReturn(Future(check))

    await(service.create(request)) shouldBe Right(check)

    verify(store, times(1)).create(check)
    sidecarProbe.expectMsg(check)
  }

  it should "return error if sidecar not found" in new Setup {
    val request = CreateHealthCheckRequest(sidecarId, Ping)

    private val error = KmsError.unregisteredSidecar(sidecarId)
    when(sidecarList.actorById(sidecarId)).thenReturn(None)

    await(service.create(request)) shouldBe Left(error)
  }
}
