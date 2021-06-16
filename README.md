# scala-itkit

This framework aims towards reduction of boilerplate produced during development of integration tests. It contains
traits which can be mixed into your suite classes providing a process running your application and an akka-http client
which can be used for sending requests to the server instance.

## Release

[Tag](https://github.com/moia-oss/itkit/tags) the new version (e.g. `v3.0.0`) and push the tags (`git push origin --tags`).

You need a [public GPG key](https://www.scala-sbt.org/release/docs/Using-Sonatype.html) with your MOIA email and an account on https://oss.sonatype.org that can [access](https://issues.sonatype.org/browse/OSSRH-52948) the `io.moia` group.

Add your credentials to `~/.sbt/sonatype_credential` and run
```sbt
sbt:itkit> +publishSigned
```

Then close and release the [repository](https://oss.sonatype.org/#stagingRepositories).
```
sbt:itkit> +sonatypeRelease
```

Afterwards, add the release to [GitHub](https://github.com/moia-oss/itkit/releases).

## Usage

You have the choice to differentiate between 3 different structural approaches for your tests:
1. One server within one suite which is used by all tests.
2. One dedicated server per test which uses the same server configuration.
3. One dedicated server per test which uses a dedicated server configuration.

### One server per suite

The most common case might be the one of using the same server instance for all of the tests of one suite. The minimum
setup for starting a test with a server like that would look like this:

```scala
import io.moia.itkit.{OneServerPerSuite, OneClientPerSuite}
import org.scalatest.{AsyncWordSpecLike, Matchers}

class SampleProcessPerSuiteSpec extends AsyncWordSpecLike with Matchers with OneServerPerSuite with OneClientPerSuite {

  // Defines package path to the main class of the process
  override val mainClass = "io.your.project.Main"

  // The message to wait for in order to know when the server process is ready to receive http requests.
  override val mainSuccessMessage = "Application is started up."

  // A regular expression which can be applied on log messages in order to communicate the port information.
  override val portMessage = ".*port: ([0-9]+).*".r

  // Additional System Properties (-D) to be added to the process start
  override val additionalSystemProperties = Map("config.resource" -> "application.conf", "akka.loglevel" -> "DEBUG")

  "Sample server per suite" should {

      "show that a server is only started once per suite" when {

        "first test is executed" in {
          client.getRequest("/hello").flatMap { response =>
            response.status.isSuccess() shouldBe true
          }
        }
      }
    }
}
```

`OneServerPerSuite` trait delivers a process, which is started on startup of the suite. `OneClientPerSuite` is providing
a client instance which is making use of the information delivered by the process in order to retrieve server's uri.

In order to make this work, there is need to exchange some information between the integration test and server process.
This framework takes advantage of log messages in order to achieve this communication flow. In this particular case the
test needs to know when the server is ready to receive http requests which is communicated through the
`mainSuccessMessage`. The port is also communicated through a match of `portMessage` regular expression.



### One server per test with global configuration

```scala
import io.moia.itkit.fixture.OneServerPerTestWithClient
import org.scalatest.{Matchers, fixture}

class SampleProcessPerTestSpec extends fixture.AsyncWordSpecLike with Matchers with OneServerPerTestWithClient {
  override val mainClass = "io.your.project.Main"
  override val mainSuccessMessage = "Application is started up."
  override val portMessage = ".*port: ([0-9]+).*".r

  "Sample server per test" should {

    "show that a new server is started per test using the same routes while using a get-fixture method" when {

      "first test is executed" in { fixture =>
        fixture.client.getRequest("/hello").flatMap { response =>
          response.status.isSuccess() shouldBe true
        }
      }
    }
  }
}
```

### One server per test with dedicated configuration

```scala
import io.moia.itkit.{OneServerPerTest, ProcessProvider}
import org.scalatest.{AsyncWordSpecLike, Matchers}

object SampleProcessPerTestSpec {
  val firstProcessProvider = new ProcessProvider {
    override val mainClass = "io.your.first.project.Main"
    override val mainSuccessMessage = "Application is running."
    override val portMessage = ".*port: ([0-9]+).*".r
    override val additionalSystemProperties = Map("akka.loglevel" -> "DEBUG")
  }

  val secondProcessProvider = new ProcessProvider {
    override val mainClass = "io.your.second.project.Main"
    override val mainSuccessMessage = "Application is ready."
    override val portMessage = ".*port: ([0-9]+).*".r
  }
}

class SampleProcessPerTestSpec extends AsyncWordSpecLike with Matchers with OneServerPerTest {
  import SampleProcessPerTestSpec._

  "Sample server per test" should {

      "show that a new server is started per test with individual routes while using loan-fixture method `withProcess`" when {

        "first test is executed" in withProcess(firstProcessProvider) { process =>
          process.port shouldBe 2442
        }
      }
    }

  "Sample server per test" should {

    "show that a new server is started per test with individual routes while using loan-fixture method `withProcessAndClient`" when {

      "second test is executed" in withProcessAndClient(secondProcessProvider) { fixture =>
        fixture.client.getRequest("/hello").flatMap { response =>
          response.status.isSuccess() shouldBe true
        }
      }
    }
  }
}
```
