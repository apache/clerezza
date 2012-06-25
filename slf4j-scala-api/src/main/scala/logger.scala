/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

/* 
 * Copyright (c) 2010 Weigle Wilczek GmbH
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.slf4j.scala

import org.slf4j.{ Logger => SLF4JLogger, LoggerFactory }

/**
 * Factory for Loggers.
 */
object Logger {

  /**
   * Creates a Logger named corresponding to the given class.
   * @param clazz Class used for the Logger's name. Must not be null!
   */
  def apply(clazz: Class[_]) = {
    require(clazz != null, "clazz must not be null!")
    new DefaultLogger(LoggerFactory getLogger clazz)
  }

  /**
   * Creates a Logger with the given name.
   * @param name The Logger's name. Must not be null!
   */
  def apply(name: String) = {
    require(name != null, "loggerName must not be null!")
    new DefaultLogger(LoggerFactory getLogger name)
  }
}

/**
 * Thin wrapper for SLF4J making use of by-name parameters to improve performance.
 */
trait Logger {

  /**
   * The name of this Logger.
   */
  lazy val name = slf4jLogger.getName

  /**
   * Log a message with ERROR level.
   * @param msg The message to be logged
   */
  def error(msg: => String) {
    if (slf4jLogger.isErrorEnabled) slf4jLogger error msg
  }

  /**
   * Log a message with ERROR level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  def error(msg: => String, t: Throwable) {
    if (slf4jLogger.isErrorEnabled) slf4jLogger.error(msg, t)
  }

  /**
   * Log a message with WARN level.
   * @param msg The message to be logged
   */
  def warn(msg: => String) {
    if (slf4jLogger.isWarnEnabled) slf4jLogger warn msg
  }

  /**
   * Log a message with WARN level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  def warn(msg: => String, t: Throwable) {
    if (slf4jLogger.isWarnEnabled) slf4jLogger.warn(msg, t)
  }

  /**
   * Log a message with INFO level.
   * @param msg The message to be logged
   */
  def info(msg: => String) {
    if (slf4jLogger.isInfoEnabled) slf4jLogger info msg
  }

  /**
   * Log a message with INFO level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  def info(msg: => String, t: Throwable) {
    if (slf4jLogger.isInfoEnabled) slf4jLogger.info(msg, t)
  }

  /**
   * Log a message with DEBUG level.
   * @param msg The message to be logged
   */
  def debug(msg: => String) {
    if (slf4jLogger.isDebugEnabled) slf4jLogger debug msg
  }

  /**
   * Log a message with DEBUG level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  def debug(msg: => String, t: Throwable) {
    if (slf4jLogger.isDebugEnabled) slf4jLogger.debug(msg, t)
  }

  /**
   * Log a message with TRACE level.
   * @param msg The message to be logged
   */
  def trace(msg: => String) {
    if (slf4jLogger.isTraceEnabled) slf4jLogger trace msg
  }

  /**
   * Log a message with TRACE level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  def trace(msg: => String, t: Throwable) {
    if (slf4jLogger.isTraceEnabled) slf4jLogger.trace(msg, t)
  }

  /**
   * The wrapped SLF4J Logger.
   */
  protected val slf4jLogger: SLF4JLogger
}

private[scala] class DefaultLogger(override protected val slf4jLogger: SLF4JLogger) extends Logger
