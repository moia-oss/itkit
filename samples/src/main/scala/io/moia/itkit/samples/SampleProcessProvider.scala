/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia.itkit.samples

import io.moia.itkit.ProcessProvider

import scala.util.matching.Regex

/**
  * An example of a `ProcessProvider` which can be directly used in the tests, if messages of your application
  * match the regular expressions in this class.
  *
  * @param mainClass package path of the class to be used for starting the process. E.g. "io.moia.itkit.Main"#
  * @param additionalSystemProperties configuration parametes to be added as -D params during start of the process

  */
final case class SampleProcessProvider(mainClass: String, override val additionalSystemProperties: Map[String, String] = Map.empty)
    extends ProcessProvider {
  override val mainSuccessMessage = "Application is started up."
  override def portMessage: Regex = ".*port: ([0-9]+).*".r
}
