/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia.itkit.fixture

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import io.moia.itkit.*
import org.scalatest.{AsyncTestSuiteMixin, FixtureAsyncTestSuite, FutureOutcome}

/** Provides one process containing the server application per test using the same configuration for each process. The process is destroyed
  * upon termination of the test.
  */
trait OneServerPerTestWithClient extends AsyncTestSuiteMixin with ProcessProvider { this: FixtureAsyncTestSuite =>
  private val clientConfig = ConfigFactory.parseString("""
    akka {
      loglevel = "INFO"
    }
  """)

  implicit private val system: ActorSystem = ActorSystem(s"itkit-client-${suiteId.hashCode().toString}", ConfigFactory.load(clientConfig))

  implicit protected val actorMaterializer: Materializer = implicitly[Materializer]

  private lazy val suiteHttp: HttpExt = Http()

  protected type FixtureParam = ProcessWithClientFixture

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {
    val process = runProcessBuilder(suiteName)

    def client: TestClient = new TestClient {
      override def serverUri: Option[Uri] = Some(Uri.from(scheme = "http", host = "localhost", port = process.port))

      override def http: HttpExt = suiteHttp
    }

    complete(withFixture(test.toNoArgAsyncTest(ProcessWithClientFixture(client, process)))).lastly(process.destroy())
  }
}
