/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia
package itkit

/** Wraps client and process into one fixture */
final case class ProcessWithClientFixture(client: TestClient, process: ServerProcess)
