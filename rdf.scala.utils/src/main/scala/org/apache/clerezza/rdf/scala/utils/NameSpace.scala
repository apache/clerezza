/*
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
 */
package org.apache.clerezza.rdf.scala.utils

import org.apache.clerezza.rdf.core.UriRef

/**
 * A IRI-namespace prefix
 */
class NameSpace(prefix: String) {

  /**
   * returns a UriRef applying this namespace prefix to the given symbol
   */
  def +(s: Symbol) = new UriRef(prefix + s.name)

  /**
   * returns a UriRef applying this prefix to the given string
   */
  def +(s: String) = new UriRef(prefix + s)
}
