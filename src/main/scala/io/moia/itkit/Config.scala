/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia
package itkit

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

final case class Config(process: ProcessConfig = ProcessConfig(), client: ClientConfig = ClientConfig())

final case class ProcessConfig(
    awaitLogTimeout: FiniteDuration = 5.seconds,
    initialMemoryAllocationPool: String = "128m",
    maximumMemoryAllocationPool: String = "512m",
    concGCThreads: Int = 2,
    parallelGCThreads: Int = 2,
    parallelismMin: Int = 2,
    factor: Int = 1,
    parallelismMax: Int = 4,
    minThreads: Int = 2,
    maxThreads: Int = 4
)

final case class ClientConfig(pekko: ClientPekkaConfig = ClientPekkaConfig())

final case class ClientPekkaConfig(loglevel: String = "INFO")
