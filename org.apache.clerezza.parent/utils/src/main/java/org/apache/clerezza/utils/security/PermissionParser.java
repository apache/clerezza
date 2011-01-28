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

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Permission;


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
		PermissionInfo permissionInfo = parse(permissionDescription);
		try {
			Class clazz = classLoader.loadClass(permissionInfo.className);
			Constructor<?> constructor = clazz.getConstructor(
					String.class, String.class);
			return (Permission) constructor.newInstance(
					permissionInfo.name, permissionInfo.actions);
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

	private static PermissionInfo parse(String permissionDescription) {
		StringReader reader = new StringReader(permissionDescription);
		try {
			return parse(reader);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static PermissionInfo parse(StringReader reader) throws IOException {
		PermissionInfo result = new PermissionInfo();
		for (int ch = reader.read(); ch != -1; ch = reader.read()) {
			if (ch == ' ') {
				continue;
			}
			if (ch =='(') {
				parseFromClassName(reader, result);
				break;
			} else {
				throw new IllegalArgumentException("Permission description does not start with '('");
			}
		}
		for (int ch = reader.read(); ch != -1; ch = reader.read()) {
			if (ch != ' ') {
				throw new IllegalArgumentException("Unparsable characters after closing ')'");
			}
		}
		return result;
	}

	private static void parseFromClassName(StringReader StringReader, PermissionInfo result) throws IOException {
		PushbackReader reader = new PushbackReader(StringReader, 1);
		result.className = readSection(reader);
		result.name = readSection(reader);
		result.actions = readSection(reader);
		byte closingBracketsCount = 0;
		for (int ch = reader.read(); ch != -1; ch = reader.read()) {
			if (ch == ' ')  {
				continue;
			}
			if (ch == ')')  {
				closingBracketsCount++;
				if (closingBracketsCount > 1) {
					throw new IllegalArgumentException("more than 1 closing bracket");
				}
				continue;
			}
			else {
				throw new IllegalArgumentException("illegal character at this position: "+ch);
			}
		}
	}

	private static String readSection(PushbackReader reader) throws IOException {
		for (int ch = reader.read(); ch != -1; ch = reader.read()) {
			if (ch == ' ')  {
				continue;
			} else {
				reader.unread(ch);
				return readSectionWithNoHeadingSpace(reader);
			}
		}
		return null;
	}

	private static String readSectionWithNoHeadingSpace(PushbackReader reader) throws IOException {
		StringBuilder sectionWriter = new StringBuilder();
		for (int ch = reader.read(); ch != -1; ch = reader.read()) {
			if (ch == '"')  {
				if (sectionWriter.length() > 0) {
					throw new IllegalArgumentException("Quote at wrong position, characters before quote: "+sectionWriter.toString());
				}
				sectionWriter = null;
				return readTillQuote(reader);
			}
			if (ch == ' ') {
				return sectionWriter.toString();
			}
			if (ch  == ')') {
				reader.unread(ch);
				return sectionWriter.toString();
			}
			sectionWriter.append((char)ch);
		}
		throw new IllegalArgumentException("missing closing bracket (')')");
	}

	private static String readTillQuote(PushbackReader reader) throws IOException {
		StringBuilder sectionWriter = new StringBuilder();
		for (int ch = reader.read(); ch != -1; ch = reader.read()) {
			if (ch == '"')  {
				return sectionWriter.toString();
			}
			sectionWriter.append((char)ch);
		}
		throw new IllegalArgumentException("missing closing quote ('=')");
	}

	private static class PermissionInfo {
		String className, name, actions;
	}
}
