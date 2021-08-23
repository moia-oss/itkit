/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia
package itkit

import java.io.File
import java.nio.file.Files

import scala.concurrent.Await
import scala.sys.process.{Process, ProcessBuilder, ProcessLogger}
import scala.util.control.NonFatal
import scala.util.matching.Regex

/** Provides necessary information for a process containing a server application. */
trait ProcessProvider extends Logger {
  // Package path of the class to be used for starting the process. E.g. "io.moia.itkit.Main"
  def mainClass: String

  // Message which is being yield when the server has been started.
  def mainSuccessMessage: String

  // Message which is being yield when the port information is provided.
  def portMessage: Regex

  // Optional configuration parametes to be added as -D params during start of the process
  def additionalSystemProperties: Map[String, String] = Map.empty

  // Optional additional arguments passed directly to the invocation of the java command,
  // e.g. to attach a remote debugger using:
  // -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005
  def additionalJavaArgs: List[String] = List.empty

  // Optional environment variables to be parsed during start of the process.
  def withEnv: Map[String, String] = Map.empty

  /** Runs the process builder returning the started process.
    *
    * @param suiteName
    *   name of the suite which uses this trait.
    * @return
    *   Started process.
    */
  def runProcessBuilder(suiteName: String): ServerProcess = {

    val portLogger = new PortAwaitingLogger {
      val port: Regex           = portMessage
      def logger: ProcessLogger = new ProcessOutputToLogStream()
    }

    val startLogger: StartAwaitingLogger =
      new StartAwaitingLogger {
        val success: Regex        = mainSuccessMessage.r
        val error: Regex          = "Could not start application".r
        val logger: ProcessLogger = portLogger
      }

    val process = processBuilder.run(startLogger)

    import scala.concurrent.ExecutionContext.Implicits.global

    val processFuture = startLogger.foundMatchedString
      .flatMap(_ => portLogger.foundMatchedString)
      .map(p =>
        new ServerProcess {
          override val port: Int = p

          override def exitValue(): Int = process.exitValue()

          override def destroy(): Unit = process.destroy()

          override def isAlive(): Boolean = process.isAlive()
        }
      )

    try {
      Await.result(processFuture, ConfigLoader.load().process.awaitLogTimeout)
    } catch {
      case NonFatal(e) =>
        log.warn(s"Destroying the process due to an error", e)
        process.destroy()
        throw e
    }
  }

  // Load application.conf of itkit.
  private val config = ConfigLoader.load()

  // Create a folder, which will be deleted upon exit.
  private def workDir: File = {
    val f = Files.createTempDirectory("itkit").toFile
    f.deleteOnExit()
    f
  }

  def urlses(cl: ClassLoader): Array[java.net.URL] = cl match {
    case null                       => Array()
    case u: java.net.URLClassLoader => u.getURLs ++ urlses(cl.getParent)
    case _                          => urlses(cl.getParent)
  }

  private val defaultSystemProperties: Map[String, String] =
    Map(
      // lower the memory pressure by limiting threads.
      "akka.actor.default-dispatcher.fork-join-executor.parallelism-min" -> config.process.parallelismMin.toString,
      "akka.actor.default-dispatcher.fork-join-executor.factor"          -> config.process.factor.toString,
      "akka.actor.default-dispatcher.fork-join-executor.parallelism-max" -> config.process.parallelismMax.toString,
      "scala.concurrent.context.minThreads"                              -> config.process.minThreads.toString,
      "scala.concurrent.context.maxThreads"                              -> config.process.maxThreads.toString
    )

  private val mergedSystemProperties: Seq[String] =
    (defaultSystemProperties ++ additionalSystemProperties).map { case (key, value) => s"-D$key=$value" }.toList

  // Returns a process builder containing all information required for startup of the process.
  private def processBuilder: ProcessBuilder = {
    val java = sys.props.get("java.home").fold("java")(_ + "/bin/java")
    val cp   = sys.props.getOrElse("java.class.path", urlses(getClass.getClassLoader).mkString(":"))

    val processConfig = config.process
    // format: off
    val cmd = Seq(
      java,
      // memory settings
      s"-Xms${processConfig.initialMemoryAllocationPool}", s"-Xmx${processConfig.maximumMemoryAllocationPool}",
      // use client settings
      "-client") ++
      additionalJavaArgs ++
      mergedSystemProperties ++
      Seq( "-classpath", cp, mainClass )
    // format: on
    Process(cmd, workDir, (sys.env ++ withEnv).toSeq: _*)
  }
}
