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
package org.apache.clerezza.triaxrs.testutils;


import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.apache.clerezza.triaxrs.JaxRsHandler;

/**
 * This utility is for the creation <code>JaxRsHandler</code> objects, which can
 * be used for testing. 
 * 
 * @author mir
 * 
 */
public class HandlerCreator {

	/**
	 * Creates a <code>JaxRsHandler</code> object, which binds an
	 * <code>javax.ws.rs.core.Application</code> which provides the specified
	 * classes as resources and components.
	 * 
	 */
	public static JaxRsHandler getHandler(final Class<?>... clazzes) {
		return getHandler("", null, clazzes);
	}
	
	/**
	 * Creates a <code>JaxRsHandler</code> object, which binds an
	 * <code>javax.ws.rs.core.Application</code> which provides the specified
	 * classes as resources and components.
	 * 
	 */
	public static JaxRsHandler getHandler(final Object... singletons) {
		return getHandler("", singletons);
	}

	/**
	 * Creates a <code>JaxRsHandler</code> object, which binds an
	 * <code>javax.ws.rs.core.Application</code> which provides the specified
	 * classes as resources and components. The application resources (clazzes)
	 * and the components bound to the handler will the provided pathPrefix as
	 * path prefix.
	 * 
	 * @param pathPrefix
	 * @param components
	 * @param clazzes
	 */
	public static JaxRsHandler getHandler(final String pathPrefix,
			final Object components[], final Class<?>... clazzes) {

		final Application applicationConfig = new Application() {

			@Override
			public Set<Class<?>> getClasses() {
				Set<Class<?>> result = new HashSet<Class<?>>();
				for (Class<?> clazz : clazzes) {
					result.add(clazz);
				}
				return result;
			}
		};


		JaxRsHandler handler = new JaxRsHandler() {

			{ // this code is in an initializer to be able to call protected
				// methods
				if (components != null) {
					for (Object component : components) {
						registerComponent(component, pathPrefix);
					}
				}
				if (clazzes != null){
					registerApplicationConfig(applicationConfig, pathPrefix);	
				}
			}
		};
		return handler;
	}
}
