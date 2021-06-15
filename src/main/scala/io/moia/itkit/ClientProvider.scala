/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia.itkit

/** Provides a client instance which can be used in a test suite. */
trait ClientProvider {

  /** Returns client specifications available in the test suite or individual test. */
  def client: TestClient
}
