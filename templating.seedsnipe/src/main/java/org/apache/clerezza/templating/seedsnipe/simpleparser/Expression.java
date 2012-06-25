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
package org.apache.clerezza.templating.seedsnipe.simpleparser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.clerezza.templating.seedsnipe.datastructure.DataFieldResolver;
import org.apache.clerezza.templating.seedsnipe.datastructure.FieldDoesNotHaveDimensionException;
import org.apache.clerezza.templating.seedsnipe.datastructure.FieldIndexOutOfBoundsException;
import org.apache.clerezza.templating.seedsnipe.datastructure.InvalidElementException;

/**
 * An expression is something like <br/>
 * foo(ex:bar) = "hello"<br/>
 * that evaluates to a boolean value.
 * <br/>
 * This class represents such an expression and provides a method to evaluate it
 * with a given DataFieldResolver and position-array.
 *
 * Currently only the = operator is supported
 *
 * @author rbn
 */
public class Expression {

	/**
	 * the char of the operand or -1 is no operand
	 */
	private int operand = -1;
	private String leftString,  rightString;

	Expression(String stringRepresentation) {
		StringReader representationReader = new StringReader(stringRepresentation);
		leftString = readPart(representationReader);
		rightString = readPart(representationReader);
	}

	boolean evaluate(final DataFieldResolver dataFieldResolver,
			final int[] arrayPositioner) throws
			FieldDoesNotHaveDimensionException,
			FieldIndexOutOfBoundsException, InvalidElementException, IOException {
		Object leftValue = evaluate(leftString, dataFieldResolver, arrayPositioner);
		if (operand == '=') {
			Object rightValue = evaluate(rightString, dataFieldResolver, arrayPositioner);
			return leftValue.toString().equals(rightValue.toString());
		}
		return evaluateSingle(leftValue);
	}

	private Object evaluate(String string, DataFieldResolver dataFieldResolver,
			int[] arrayPositioner) throws FieldDoesNotHaveDimensionException,
			FieldIndexOutOfBoundsException,
			InvalidElementException,
			IOException {
		String trimmed = string.trim();
		if (trimmed.charAt(0) == '\"') {
			if (trimmed.charAt(trimmed.length() - 1) != '\"') {
				throw new RuntimeException("String expression must end with a quote (\")");
			}
			return trimmed.substring(1, trimmed.length() - 1);
		} else {
			return dataFieldResolver.resolveAsObject(trimmed, arrayPositioner);
		}
	}

	private boolean evaluateSingle(Object value) {
		if (value == null) {
			return false;
		}
		if (value instanceof Class) {
			return false;
		}
		if (value.getClass().isArray()) {
			if (((Object[]) value).length > 0) {
				value = ((Object[]) value)[0];
			} else {
				return false;
			}
		}
		if (value instanceof String) {
			return Boolean.parseBoolean((String) value);
		}
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		return true;
	}

	/**
	 * reads till an operator that is not in a quoted section
	 * 
	 * @param representationReader
	 * @return
	 */
	private String readPart(Reader representationReader) {
		try {
			StringWriter resultWriter = new StringWriter();
			boolean isInQuotedSection = false;
			boolean charEscaped = false;
			for (int ch = representationReader.read(); ch != -1; ch = representationReader.read()) {
				if (charEscaped) {
					charEscaped = false;
				} else {
					if (ch == '\"') {
						isInQuotedSection = !isInQuotedSection;
					} else {
						if ((ch == '=') && !isInQuotedSection) {
							//to support string with multiple (non braketed) operations
							//we would add this to a list
							operand = '=';
							break;
						} else {
							if (ch == '\\') {
								charEscaped = true;
								continue;
							}
						}
					}
				}
				resultWriter.write(ch);
			}
			return resultWriter.toString().trim();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
