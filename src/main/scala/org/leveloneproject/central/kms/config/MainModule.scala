package org.leveloneproject.central.kms.config

import java.security.Security

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Singleton
import com.typesafe.config.Config
import com.tzavellas.sse.guice.ScalaModule
import net.codingwell.scalaguice.ScalaMultibinder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.flywaydb.core.Flyway
import org.leveloneproject.central.kms.Service
import org.leveloneproject.central.kms.crypto._
import org.leveloneproject.central.kms.domain.batches.{BatchFinder, BatchFinderImpl, BatchStore}
import org.leveloneproject.central.kms.domain.healthchecks.{HealthCheckRouter, HealthCheckStore}
import org.leveloneproject.central.kms.domain.inquiries._
import org.leveloneproject.central.kms.domain.keys._
import org.leveloneproject.central.kms.domain.sidecars.{SidecarList, SidecarLogsStore, SidecarRouter, SidecarStore}
import org.leveloneproject.central.kms.persistance._
import org.leveloneproject.central.kms.persistance.postgres._
import org.leveloneproject.central.kms.routing.{RouteAggregator, Router}
import org.leveloneproject.central.kms.socket.{SocketRouter, WebSocketService}


class MainModule(config: Config) extends ScalaModule {
  implicit val system = ActorSystem("kms", config)

  def configure(): Unit = {

    Security.addProvider(new BouncyCastleProvider)

    implicit val materializer = ActorMaterializer()

    val asymmetric = new TweetNaClKeys()
    val symmetric = new CmacKeys
    bind[Config].toInstance(config)
    bind[ActorSystem].toInstance(system)
    bind[ActorMaterializer].toInstance(materializer)
    bind[SidecarList].in[Singleton]
    bindDatabase
    bind[InquiryCreator].to[InquiryCreatorImpl]
    bind[Flyway]
    bind[Migrator]
    bind[Service]
    bind[AsymmetricKeyGenerator].toInstance(asymmetric)
    bind[SymmetricKeyGenerator].toInstance(symmetric)
    bind[AsymmetricVerifier].toInstance(asymmetric)
    bind[SymmetricVerifier].toInstance(symmetric)
    bind[BatchFinder].to[BatchFinderImpl]
    bind[KeyVerifier].to[KeyVerifierImpl]
    bind[KeyFinder].to[KeyFinderImpl]
    bind[InquiryResponseVerifier].to[InquiryResponseVerifierImpl]
    bind[RouteAggregator]
    bind[WebSocketService]

    bindRouters()
  }


  private def bindDatabase = {
    bind[DbProvider].to[PostgresDbProvider].in[Singleton]
    bind[BatchStore].to[PostgresBatchStore]
    bind[KeyStore].to[PostgresKeyStore]
    bind[SidecarStore].to[PostgresSidecarStore]
    bind[HealthCheckStore].to[PostgresHealthCheckStore]
    bind[SidecarLogsStore].to[PostgresSidecarLogsStore]
    bind[InquiriesStore].to[PostgresInquiriesStore]
    bind[InquiryResponsesStore].to[PostgresInquiryResponsesStore]
  }

  private def bindRouters(): Unit = {
    val routerBinder = ScalaMultibinder.newSetBinder[Router](binder)
    routerBinder.addBinding.to[SocketRouter]
    routerBinder.addBinding.to[SidecarRouter]
    routerBinder.addBinding.to[HealthCheckRouter]
    routerBinder.addBinding.to[InquiryRouter]
  }

}


