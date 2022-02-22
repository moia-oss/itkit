/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia
package itkit

import pureconfig.ConfigSource
import pureconfig.generic.auto.exportReader

object ConfigLoader extends Logger {
  def load(): Config =
    ConfigSource.default.at("itkit").load[Config] match {
      case Right(config) =>
        config

      case Left(failures) =>
        log.error("Could not read application config due to: {}.\nLoading default configuration.", failures)
        Config()
    }
}
