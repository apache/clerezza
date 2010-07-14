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
package org.apache.clerezza.utils.security;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Permission;


import org.osgi.service.permissionadmin.PermissionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a utility method to instantiate a permission given its string 
 * representation as returned by <code>java security.Permission.toString</code>.
 *
 * @author reto
 */
public class PermissionParser {

	final static Logger logger = LoggerFactory.getLogger(PermissionParser.class);

	/**
	 * Parsers permissionDescription and instantiates the permission using
	 * the ClassLoader of this class.
	 *  
	 * @param permissionDescription
	 * @return
	 */
	public static Permission getPermission(String permissionDescription) {
		return getPermission(permissionDescription, PermissionParser.class.getClassLoader());
	}

	/**
	 * Parsers permissionDescription and instantiates the permission using
	 * classLoader.
	 *
	 * @param permissionDescription
	 * @param classLoader
	 * @return
	 */
	public static Permission getPermission(String permissionDescription, ClassLoader classLoader) {
		PermissionInfo permissionInfo = new PermissionInfo(
					permissionDescription);

		try {
			Class clazz = classLoader.loadClass(permissionInfo.getType());
			Constructor<?> constructor = clazz.getConstructor(
					String.class, String.class);
			return (Permission) constructor.newInstance(
					permissionInfo.getName(), permissionInfo.getActions());
		} catch (InstantiationException ie) {
			logger.warn("{}", ie);
			throw new RuntimeException(ie);
		} catch (ClassNotFoundException cnfe) {
			logger.warn("{}", cnfe);
			throw new RuntimeException(cnfe);
		} catch (NoSuchMethodException nsme) {
			logger.warn("{}", nsme);
			throw new RuntimeException(nsme);
		} catch (InvocationTargetException ite) {
			logger.warn("{}", ite);
			throw new RuntimeException(ite);
		} catch (IllegalAccessException iae) {
			logger.warn("{}", iae);
			throw new RuntimeException(iae);
		}
	}
}
