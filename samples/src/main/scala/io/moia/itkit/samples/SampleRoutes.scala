package io.moia.itkit.samples

import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.{Directive, Route}
import io.moia.itkit.Logger

object SampleRoutes extends Logger {
  def hostnameAndPort: Directive[(String, Int)] = Directive[(String, Int)] { inner => ctx =>
    val authority = ctx.request.uri.authority
    inner((authority.host.address(), authority.port))(ctx)
  }

  val routes: Route =
    path("hello") {
      get {
        hostnameAndPort { (hostname, port) =>
          log.info(s"Received a get on $hostname:$port")

          complete(s"$hostname:$port")
        }
      }
    }
}

trait SampleRoutes {
  val routes: Route = SampleRoutes.routes
}
