/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia.itkit

import akka.Done

import scala.concurrent.{Future, Promise}
import scala.sys.process.ProcessLogger
import scala.util.matching.Regex

/** Write process output to log stream. */
final class ProcessOutputToLogStream extends ProcessLogger {
  override def out(msg: => String): Unit = println(msg)
  override def err(msg: => String): Unit = println(msg)
  override def buffer[T](f: => T): T     = f
}

/** Provides a promise corresponding to a string yielded upon startup of the process. */
trait StartAwaitingLogger extends AwaitingLogger[Done] with Logger {
  protected def success: Regex
  protected def error: Regex

  override protected def checkString(string: String): String = {
    if (!promise.isCompleted) {
      if (success.pattern.matcher(string).find()) {
        log.debug(s"Found log message matching the regex: $success")
        promise.trySuccess(Done)
      }
      if (error.pattern.matcher(string).find()) promise.tryFailure(new IllegalStateException(string))
    }
    string
  }
}

/** Provides a promise corresponding to a string containing port information. */
trait PortAwaitingLogger extends AwaitingLogger[Int] with Logger {
  protected val port: Regex

  override protected def checkString(string: String): String =
    string match {
      case port(p) =>
        log.debug(s"Found port in log message and extracting port '$p'")
        promise.trySuccess(p.toInt)
        string

      case _ =>
        string
    }
}

/** Provides a promise containing information encoded into logging messages of a process.
  *
  * @tparam A type of the information yielded by logging messages.
  */
trait AwaitingLogger[A] extends ProcessLogger {
  def foundMatchedString: Future[A] = promise.future

  protected def logger: ProcessLogger
  protected def checkString(string: String): String

  override def out(s: => String): Unit = logger.out(checkString(s))
  override def err(s: => String): Unit = logger.err(checkString(s))

  protected val promise: Promise[A]  = Promise()
  override def buffer[T](f: => T): T = f
}
