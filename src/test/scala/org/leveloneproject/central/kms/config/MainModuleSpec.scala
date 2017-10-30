package org.leveloneproject.central.kms.config


import com.google.inject.{Guice, Injector}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar

class MainModuleSpec extends FlatSpec with MockitoSugar {

  trait Setup {
    val config: Config = ConfigFactory.load()

    val module = new MainModule(config)
    val injector: Injector = Guice.createInjector(module)
  }

  it should "bind Config to config" in new Setup {
    assert(injector.getInstance(classOf[Config]) == config)
  }
}
