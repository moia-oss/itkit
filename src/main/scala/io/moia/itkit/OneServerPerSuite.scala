/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia
package itkit

import org.scalatest.{Args, AsyncTestSuite, AsyncTestSuiteMixin, Status}

/** Provides one process containing the server application for all tests using the same configuration. The process is destroyed upon
  * termination of test suite.
  */
trait OneServerPerSuite extends AsyncTestSuiteMixin with ProcessProvider { this: AsyncTestSuite =>
  // The process needs to be globally available for all tests, but lazily started when the first test tries to access it.
  lazy val process: ServerProcess = runProcessBuilder(suiteName = suiteName)

  /** Runs this suite of tests.
    *
    * @param testName
    *   an optional name of one test to execute. If <code>None</code>, all relevant tests should be executed.
    * I.e., <code>None</code> acts like a wildcard that means execute all relevant tests in this <code>Suite</code>.
    * @param args
    *   the <code>Args</code> for this run
    * @return
    *   a <code>Status</code> object that indicates when all tests and nested suites started by this method have completed, and whether or
    *   not a failure occurred.
    */
  @SuppressWarnings(Array("CatchException"))
  abstract override def run(testName: Option[String], args: Args): Status = {
    try {
      val status = super.run(testName, args)

      // Destroy the process when all tests are completed.
      status.whenCompleted { _ =>
        process.destroy()
      }
      status
    } catch {
      case e: Exception =>
        process.destroy()
        throw e
    }
  }
}
