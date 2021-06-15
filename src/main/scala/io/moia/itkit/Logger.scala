/*
 * Copyright (c) MOIA GmbH 2017
 */

package io.moia
package itkit

import com.typesafe.scalalogging
import org.slf4j.LoggerFactory

/** Defines `log` as a value initialized with an underlying `org.slf4j.Logger`
  * named according to the class into which this trait is mixed.
  */
trait Logger {

  protected val log: scalalogging.Logger =
    scalalogging.Logger(LoggerFactory.getLogger(getClass.getName))
}
