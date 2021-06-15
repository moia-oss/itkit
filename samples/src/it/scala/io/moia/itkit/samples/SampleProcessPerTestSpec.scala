package io.moia.itkit.samples

import akka.http.scaladsl.unmarshalling.Unmarshal
import io.moia.itkit.OneServerPerTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike

class SampleProcessPerTestSpec extends AsyncWordSpecLike with Matchers with OneServerPerTest {
  val mainClass = "io.moia.itkit.samples.Main"

  "Sample server per test" should {

    "show that a new server is started per test with individual routes while using loan-fixture method `withTestServer`" when {

      "first test is executed" in withProcessAndClient(SampleProcessProvider(mainClass, Map("akka.loglevel" -> "DEBUG"))) { f =>
        f.client.getRequest("/hello").flatMap { response =>
          Unmarshal(response.entity).to[String].map { s =>
            s shouldBe s"localhost:${f.process.port}"
          }
        }
      }

      "second test is executed" in withProcessAndClient(SampleProcessProvider(mainClass)) { f =>
        f.client.getRequest("/hello").flatMap { response =>
          Unmarshal(response.entity).to[String].map { s =>
            s shouldBe s"localhost:${f.process.port}"
          }
        }
      }

      "third test is executed" in withProcessAndClient(SampleProcessProvider(mainClass)) { f =>
        f.client.getRequest("/hello").flatMap { response =>
          response.status.isSuccess() shouldBe true
        }
      }
    }
  }
}