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

import org.osgi.framework.{BundleContext, Constants, ServiceEvent, ServiceListener}
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

	/**
	 * executes action as soon as a service exposing T is available, if such
	 * a service is already available the action is executed immedtely and the
	 * method blocks until the action finished executing, otherwise the method
	 * returns and action will be executed when a respective becomes available.
	 */
	def doWith[T](action: T => Unit)(implicit m: Manifest[T]) {
		val clazz = m.erasure.asInstanceOf[Class[T]]
		val service = getService(clazz)
		if (service != null) {
			action(service)
		} else {
			lazy val serviceListener: ServiceListener = new ServiceListener {
					def serviceChanged(e: ServiceEvent) = {
						if (e.getType == ServiceEvent.REGISTERED) {
							bundleContext.removeServiceListener(serviceListener)
							action(bundleContext.getService(e.getServiceReference).asInstanceOf[T])
						}
					}
				}
			bundleContext.addServiceListener(serviceListener,
											 "("+Constants.OBJECTCLASS+"="+clazz.getName+")")
		}
	}

	def doWith[T,U](action: (T,U) => Unit)(implicit mt: Manifest[T], mu: Manifest[U]) {
		doWith[T] {
			t: T => {
				val clazz = mu.erasure.asInstanceOf[Class[U]]
				val service = getService(clazz)
				if (service != null) {
					action(t, service)
				} else {
					doWith[U,T] {
						(iu: U, it: T) => action(it,iu)
					}
					}
			}
		}
	}

	def doWith[T,U,V](action: (T,U,V) => Unit)(implicit mt: Manifest[T],
												mu: Manifest[U], mv: Manifest[V]) {
		doWith[T,U] {
			(t: T, u: U) => {
				val clazz = mv.erasure.asInstanceOf[Class[V]]
				val service: V = getService(clazz)
				if (service != null) {
					action(t, u, service)
				} else {
					doWith[U,V,T] {
						(iu: U, iv: V, it: T) => action(it,iu,iv)
					}
				}
			}
		}
	}
}
