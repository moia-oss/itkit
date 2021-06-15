package io.moia.itkit.samples

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.settings.ServerSettings
import io.moia.itkit.Logger

import scala.util.{Failure, Success}

object Main extends Logger {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    import system.dispatcher

    type OptionMap = Map[Symbol, String]

    @scala.annotation.tailrec
    def nextOption(map: OptionMap, list: List[String]): OptionMap = list match {
      case Nil =>
        map

      case "-port" :: value :: tail =>
        nextOption(map ++ Map(Symbol("port") -> value), tail)

      case "-host" :: value :: tail =>
        nextOption(map ++ Map(Symbol("host") -> value), tail)

      case option :: _ =>
        println("Unknown option " + option)
        sys.exit(1)
    }
    val options = nextOption(Map(), args.toList)

    val port      = options.getOrElse(Symbol("port"), "50001").toInt
    val interface = options.getOrElse(Symbol("host"), "localhost")

    Http().newServerAt(interface, port).withSettings(ServerSettings(system)).bind(SampleRoutes.routes).onComplete {
      case Success(res) =>
        log.info(s"Application is started up on port: ${res.localAddress.getPort}.")

      case Failure(_) =>
        log.error("Failed to bind server.")
    }
  }
}
