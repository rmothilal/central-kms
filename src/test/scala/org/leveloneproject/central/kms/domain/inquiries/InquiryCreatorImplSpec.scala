package org.leveloneproject.central.kms.domain.inquiries

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

import akka.testkit.TestProbe
import org.leveloneproject.central.kms.AwaitResult
import org.leveloneproject.central.kms.domain.KmsError
import org.leveloneproject.central.kms.domain.sidecars.{Sidecar, SidecarAndActor, SidecarList, SidecarStatus}
import org.leveloneproject.central.kms.util.{IdGenerator, InstantProvider}
import org.leveloneproject.central.kms.utils.AkkaSpec
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.Future

class InquiryCreatorImplSpec extends FlatSpec with Matchers with MockitoSugar with AwaitResult with AkkaSpec {

  trait Setup {
    val store: InquiriesStore = mock[InquiriesStore]
    val sidecarList: SidecarList = mock[SidecarList]
    val id: UUID = UUID.randomUUID()
    val newInstant: Instant = Instant.now()
    val creator = new InquiryCreatorImpl(sidecarList, store) with InstantProvider with IdGenerator {
      override def now(): Instant = newInstant

      override def newId(): UUID = id
    }
  }

  "create" should "return error if startTime and endTime are greater than 30 days apart" in new Setup {
    private val endTime = newInstant
    private val startTime = endTime.minus(31, ChronoUnit.DAYS)
    private val request = CreateInquiryRequest("serviceName", startTime, endTime)

    await(creator.create(request)) shouldBe Left(KmsError.invalidDateRange)
  }

  it should "return error if startTime is after endTime" in new Setup {
    private val endTime = newInstant
    private val startTime = endTime.plusSeconds(1)
    private val request = CreateInquiryRequest("serviceName", startTime, endTime)

    await(creator.create(request)) shouldBe Left(KmsError.invalidDateRange)
  }

  it should "return error if service is not currently registered" in new Setup {
    private val name = "serviceName"
    when(sidecarList.byName(name)).thenReturn(None)
    private val request = CreateInquiryRequest(name, newInstant.minusSeconds(1), newInstant)
    await(creator.create(request)) shouldBe Left(KmsError.unregisteredSidecar(name))
  }

  it should "create and return new inquiry" in new Setup {
    private val startTime: Instant = Instant.now().minusSeconds(400)
    private val endTime: Instant = Instant.now().plusSeconds(400)
    private val serviceName = "service"
    private val request = CreateInquiryRequest(serviceName, startTime, endTime)
    private val sidecarId = UUID.randomUUID
    when(sidecarList.byName(serviceName)).thenReturn(Some(SidecarAndActor(Sidecar(sidecarId, serviceName, SidecarStatus.Registered, ""), TestProbe().ref)))
    private val inquiry = Inquiry(id, serviceName, startTime, endTime, newInstant, InquiryStatus.Created, sidecarId)
    when(store.insert(inquiry)).thenReturn(Future(inquiry))

    private val result = await(creator.create(request))

    result shouldBe Right(inquiry)
    verify(store, times(1)).insert(inquiry)
  }

  it should "send inquiry to sidecar actor" in new Setup {
    private val startTime: Instant = Instant.now().minusSeconds(400)
    private val endTime: Instant = Instant.now().plusSeconds(400)
    private val serviceName = "service"
    private val request = CreateInquiryRequest(serviceName, startTime, endTime)
    private val sidecarId = UUID.randomUUID
    private val sidecarActorProbe = TestProbe()
    when(sidecarList.byName(serviceName)).thenReturn(Some(SidecarAndActor(Sidecar(sidecarId, serviceName, SidecarStatus.Registered, ""), sidecarActorProbe.ref)))
    private val inquiry = Inquiry(id, serviceName, startTime, endTime, newInstant, InquiryStatus.Created, sidecarId)
    when(store.insert(inquiry)).thenReturn(Future(inquiry))

    await(creator.create(request))

    sidecarActorProbe.expectMsg(inquiry)
  }
}
