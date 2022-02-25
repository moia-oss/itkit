/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia.itkit

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import org.scalatest.*

trait OneClientPerSuite extends AsyncTestSuiteMixin with ClientProvider { this: AsyncTestSuite =>
  private val clientConfig = ConfigFactory.parseString("""
    akka {
      loglevel = "INFO"
    }
  """)

  implicit private val system: ActorSystem[Nothing] = ActorSystem(
    guardianBehavior = Behaviors.empty,
    name = s"itkit-client-${suiteId.hashCode().toString}",
    config = ConfigFactory.load(clientConfig)
  )

  implicit protected val actorMaterializer: Materializer = implicitly[Materializer]

  abstract override def run(testName: Option[String], args: Args): Status = {
    val status = super.run(testName, args)
    status.whenCompleted { _ =>
      client.http.shutdownAllConnectionPools()
      ()
    }
    status
  }

  lazy val suite: AsyncTestSuite = this

  def client: TestClient = new TestClient {
    override def serverUri: Option[Uri] = suite match {
      case serverSuite: OneServerPerSuite => Some(Uri.from(scheme = "http", host = "localhost", port = serverSuite.process.port))
      case _                              => None
    }

    override def http: HttpExt = Http()
  }
}
