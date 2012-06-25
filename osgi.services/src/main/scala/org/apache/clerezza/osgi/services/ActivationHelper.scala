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


import scala.collection.JavaConversions.asJavaDictionary
import scala.collection.mutable
import org.osgi.framework.{ServiceRegistration, BundleContext, BundleActivator}

/**
 * A trait to facilitate creating bundle activators to register service.
 *
 */
trait ActivationHelper extends BundleActivator {

	/**
	 * this is intended to be used exclusively in the argument to the register-methods
	 */
	protected var context: BundleContext= null

	/**
	 * Registers a JAX-RS Root Resource
	 */
	protected def registerRootResource(rootResource: =>Object) {
		registerService(rootResource, classOf[Object], "javax.ws.rs" -> true)
	}

	/**
	 * Register a Renderlet
	 * Note: renderlet must implement org.apache.clerezza.platform.typerendering.TypeRenderlet, argument not decalred on
	 * this type to avoid dependency
	 */
	protected def registerRenderlet(renderlet: =>Object) {
		registerServiceStringInterfaces(renderlet, Seq("org.apache.clerezza.platform.typerendering.TypeRenderlet"), Map[String, Any]())
	}

	/**
	 * Register a TypeHandler
	 */
	protected def registerTypeHandler(typeHandler: => Object) {
		registerService(typeHandler, classOf[Object], "org.apache.clerezza.platform.typehandler" -> true)
	}

	/**
	 * Register a service exposing a specified interface with an arbitrary number of
	 * arguments
	 */
	protected def registerService(instance: => AnyRef, interface:Class[_],
																arguments: (String, Any)*) {
		registerService(instance, Seq(interface), Map(arguments:_*))
	}

	/**
	 * Registers a service for a Seq of interfaces and a map of arguments
	 */
	protected def registerService(instance: => AnyRef, interfaces: Seq[Class[_]],
																arguments: Map[String, Any]) {
		  registerServiceStringInterfaces(instance, for (i <- interfaces) yield i.getName, arguments)
	}
	/**
	 * Registers a service for a Seq of interfaces and a map of arguments
	 */
	private def registerServiceStringInterfaces(instance: => AnyRef, interfaces: Seq[String],
																arguments: Map[String, Any]) {
		managedServices ::= ((() => instance, interfaces, arguments))
	}

	/**
	 * invoked by the OSGi environment when the bundle is started, this method registers
	 * the services for which the register-methods hqave been called (during object construction)
	 */
	def start(context: BundleContext) {
		this.context = context
		registeredServices = Nil
		for (entry <- managedServices) {
			val args = asJavaDictionary(mutable.Map(entry._3.toSeq:_*))
			registeredServices ::= context.registerService(
				entry._2.toArray, entry._1(), args)
		}
		this.context = null
	}

	/**
	 * called when the bundle is stopped, this method unregisters the provided service
	 */
	def stop(context: BundleContext) {
		for(sr <- registeredServices) {
			sr.unregister();
		}
		registeredServices = null
	}

	private var managedServices: List[(() => Any, Seq[String], Map[String, Any])] = Nil

	private var registeredServices: List[ServiceRegistration] = null
}