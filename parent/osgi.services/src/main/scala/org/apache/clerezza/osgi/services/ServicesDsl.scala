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
package org.apache.clerezza.osgi.services

import org.osgi.framework.BundleContext
import scala.collection.JavaConversions._

class ServicesDsl(bundleContext: BundleContext) {

	/**
	 * returns an instance of a service exposing T
	 */
	def $[T](implicit m: Manifest[T]): T = {
		getService(m.erasure.asInstanceOf[Class[T]])
	}

	private def getService[T](clazz : Class[T]) : T= {
		val serviceReference = bundleContext.getServiceReference(clazz.getName)
		if (serviceReference != null) {
			bundleContext.getService(serviceReference).asInstanceOf[T]
		} else null.asInstanceOf[T]
	}
}
