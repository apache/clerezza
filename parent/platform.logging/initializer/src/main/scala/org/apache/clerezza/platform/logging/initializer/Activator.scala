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
package org.apache.clerezza.platform.logging.initializer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.BundleActivator

class Activator extends BundleActivator {

	private val configurationAdminClassName = classOf[ConfigurationAdmin].getName

	def start(context: BundleContext) {
		import scala.collection.JavaConversions._
		val paxLoggingLocation: String = context.getBundles().
			find(bundle => bundle.getSymbolicName().equals("org.ops4j.pax.logging.pax-logging-service")) match {
				case Some(b) => b.getLocation();
				case _ => throw new RuntimeException("org.ops4j.pax.logging.pax-logging-service not found")
			}
		val serviceReference = context.getServiceReference(configurationAdminClassName);
		def configureIfUnconfigured(serviceReference: ServiceReference) {
			val configurationAdmin = context.getService(serviceReference).asInstanceOf[ConfigurationAdmin]
			val config: Configuration = configurationAdmin.getConfiguration(
				"org.ops4j.pax.logging", paxLoggingLocation);
			if (config.getProperties() == null) {
				val props: Dictionary[String, String] = new Hashtable[String, String]();
				props.put("log4j.rootLogger", "INFO, R, stdout");
				props.put("log4j.logger.org.apache.clerezza","DEBUG")
				props.put("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
				props.put("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
				props.put("log4j.appender.stdout.Threshold", "WARN");
				// Pattern to output the caller's file name and line number.
				props.put("log4j.appender.stdout.layout.ConversionPattern", "%d [%t] %5p [%t] (%F\\:%L) - %m%n");
				props.put("log4j.appender.R", "org.apache.log4j.FileAppender");
				props.put("log4j.appender.R.File", "clerezza.log");
				props.put("log4j.appender.R.layout", "org.apache.log4j.PatternLayout");
				props.put("log4j.appender.R.layout.ConversionPattern", "%d [%t] %p %t %c - %m%n");

				config.update(props);
			}
		}
		if (serviceReference != null) {
			configureIfUnconfigured(serviceReference);
		} else {
			val filter: String = "(objectclass=" + configurationAdminClassName + ")";
			context.addServiceListener(new ServiceListener{
				def serviceChanged(e: ServiceEvent) {
					if (e.getType == ServiceEvent.REGISTERED) {
						configureIfUnconfigured(e.getServiceReference)
					}
				}
			},filter)
		}
		
	}

	def stop(context: BundleContext) {

	}

}
