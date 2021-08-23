package io.moia.itkit.samples.fixture

import akka.http.scaladsl.unmarshalling.Unmarshal
import io.moia.itkit.fixture.OneServerPerTestWithClient
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.FixtureAsyncWordSpecLike

import scala.util.matching.Regex

class SampleProcessPerTestSpec extends FixtureAsyncWordSpecLike with Matchers with OneServerPerTestWithClient {
  override val portMessage: Regex = ".*port: ([0-9]+).*".r
  override val mainClass          = "io.moia.itkit.samples.Main"
  override val mainSuccessMessage = "Application is started up."

  "Sample server per test" should {

    "show that a new server is started per test using the same routes while using a get-fixture method" when {

      "first test is executed" in { f =>
        f.client.getRequest("/hello").flatMap { response =>
          Unmarshal(response.entity).to[String].map { s =>
            s shouldBe s"localhost:${f.process.port}"
          }
        }
      }

      "second test is executed" in { f =>
        f.client.getRequest("/hello").flatMap { response =>
          Unmarshal(response.entity).to[String].map { s =>
            s shouldBe s"localhost:${f.process.port}"
          }
        }
      }
    }
  }
}
