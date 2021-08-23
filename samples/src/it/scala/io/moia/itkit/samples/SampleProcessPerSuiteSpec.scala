package io.moia.itkit.samples

import akka.http.scaladsl.unmarshalling.Unmarshal
import io.moia.itkit.{OneClientPerSuite, OneServerPerSuite}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

import scala.util.matching.Regex

class SampleProcessPerSuiteSpec extends AsyncWordSpecLike with Matchers with OneClientPerSuite with OneServerPerSuite {
  override val portMessage: Regex                                   = ".*port: ([0-9]+).*".r
  override val mainClass                                            = "io.moia.itkit.samples.Main"
  override val mainSuccessMessage                                   = "Application is started up."
  override lazy val additionalSystemProperties: Map[String, String] = Map("akka.loglevel" -> "DEBUG")

  "Sample server per suite" should {

    "show that a server is only started once per suite" when {

      "first test is executed" in {
        client.getRequest("/hello").flatMap { response =>
          Unmarshal(response.entity).to[String].map { s =>
            s shouldBe s"localhost:${process.port}"
          }
        }
      }

      "second test is executed" in {

        client.getRequest("/hello").flatMap { response =>
          Unmarshal(response.entity).to[String].map { s =>
            s shouldBe s"localhost:${process.port}"
          }
        }
      }
    }
  }
}
