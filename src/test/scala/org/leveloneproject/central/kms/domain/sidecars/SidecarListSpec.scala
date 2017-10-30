package org.leveloneproject.central.kms.domain.sidecars

import java.util.UUID

import akka.testkit.TestProbe
import org.leveloneproject.central.kms.utils.AkkaSpec
import org.scalatest.{FlatSpec, Matchers}

class SidecarListSpec extends FlatSpec with Matchers with AkkaSpec {
  trait Setup {
    val list = new SidecarList
  }

  "byName" should "return sidecar by name" in new Setup {
    private val sidecarId = UUID.randomUUID
    private val name = "service name"
    val sidecar = Sidecar(sidecarId, name, SidecarStatus.Registered, "")

    private val andActor = SidecarAndActor(sidecar, TestProbe().ref)
    list.register(andActor)
    list.register(SidecarAndActor(Sidecar(UUID.randomUUID, s"not ${name}", SidecarStatus.Registered, ""), TestProbe().ref))

    val result = list.byName(name)

    result shouldBe Some(andActor)
  }

  it should "return none if no sidecars registered by name" in new Setup {
    list.register(SidecarAndActor(Sidecar(UUID.randomUUID, UUID.randomUUID.toString, SidecarStatus.Registered, ""), TestProbe().ref))

    list.byName(UUID.randomUUID.toString) shouldBe None
  }

  it should "only return registered sidecars" in new Setup {
    private val name = "name"
    list.register(SidecarAndActor(Sidecar(UUID.randomUUID, name, SidecarStatus.Challenged, ""), TestProbe().ref))
    private val registered = SidecarAndActor(Sidecar(UUID.randomUUID, name, SidecarStatus.Registered, ""), TestProbe().ref)
    list.register(registered)

    list.byName(name) shouldBe Some(registered)
  }

  it should "return latest registered sidecar" in new Setup {
    private val name = "name"
    private val first = SidecarAndActor(Sidecar(UUID.randomUUID, name, SidecarStatus.Registered, ""), TestProbe().ref)
    private val last = SidecarAndActor(Sidecar(UUID.randomUUID, name, SidecarStatus.Registered, ""), TestProbe().ref)

    list.register(first)
    list.register(last)

    list.byName(name) shouldBe Some(last)
  }
}
