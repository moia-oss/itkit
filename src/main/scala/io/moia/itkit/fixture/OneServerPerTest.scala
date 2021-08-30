/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia.itkit.fixture

import io.moia.itkit.{ProcessProvider, ServerProcess}
import org.scalatest.{AsyncTestSuiteMixin, FixtureAsyncTestSuite, FutureOutcome}

/** Provides one process containing the server application per test using the same configuration for each process. The process is destroyed
  * upon termination of the test.
  */
trait OneServerPerTest extends AsyncTestSuiteMixin with ProcessProvider { this: FixtureAsyncTestSuite =>
  protected type FixtureParam = ServerProcess

  override def withFixture(test: OneArgAsyncTest): FutureOutcome = {

    val process = runProcessBuilder(suiteName)

    complete {
      this.withFixture(test.toNoArgAsyncTest(process))
    } lastly {
      process.destroy()
    }
  }
}
