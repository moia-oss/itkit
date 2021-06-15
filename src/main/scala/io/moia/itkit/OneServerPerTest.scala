/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia
package itkit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.{Http, HttpExt}
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import org.scalatest.compatible.Assertion
import org.scalatest.{AsyncTestSuite, AsyncTestSuiteMixin}
import scala.concurrent.Future

/** Provides one process containing the server application per test using a dedicated configuration for each process.
  * The process is destroyed upon termination of test suite.
  */
trait OneServerPerTest extends AsyncTestSuiteMixin { this: AsyncTestSuite =>
  private val clientConfig = ConfigFactory.parseString("""
    akka {
      loglevel = "INFO"
    }
  """)

  private implicit val system: ActorSystem          = ActorSystem(s"itkit-client-${suiteId.hashCode().toString}", ConfigFactory.load(clientConfig))
  protected implicit val materializer: Materializer = implicitly[Materializer]

  private lazy val suiteHttp: HttpExt = Http()

  // Start process for the particular test, use the configuration contained in `provider`.
  private def createProcess(provider: ProcessProvider) =
    provider.runProcessBuilder(suiteName)

  /** Injects a `ServerProcess` instance into the test according to the configuration provided by process `provider`.
    *
    * @param provider Contains information about the process including package path of main class, port information,
    *                 message which is yield upon the start of process.
    * @param test test for the process to be injected into.
    * @return the result of test application.
    */
  def withProcess(provider: ProcessProvider)(test: ServerProcess => Future[Assertion]): Future[Assertion] = {
    val process = createProcess(provider)

    val futureAssertion = test(process)

    // Destroy the process when all tests are completed.
    futureAssertion.onComplete { _ =>
      process.destroy()
    }
    futureAssertion
  }

  def withProcessAndClient(provider: ProcessProvider)(test: ProcessWithClientFixture => Future[Assertion]): Future[Assertion] = {
    val process = createProcess(provider)

    val client = new TestClient {

      /** If known, the server uri to send the requests against. */
      override def serverUri: Option[Uri] = Some(Uri.from(scheme = "http", host = "localhost", port = process.port))

      /** Exposes the http extension. This can be used in order to send requests. */
      override val http: HttpExt = suiteHttp
    }

    val futureAssertion = test(ProcessWithClientFixture(client, process))

    // Destroy the process when all tests are completed.
    futureAssertion.onComplete { _ =>
      process.destroy()
      client.http.shutdownAllConnectionPools()
    }
    futureAssertion
  }
}
