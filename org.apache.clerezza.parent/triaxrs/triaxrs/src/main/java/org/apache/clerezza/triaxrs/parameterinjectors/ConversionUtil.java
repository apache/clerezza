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
package org.apache.clerezza.triaxrs.parameterinjectors;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author reto
 */
class ConversionUtil {

	static interface  Convertor<T> {
		public T convert(String string);
	};

	/**
	 * T must either 
	 * 1. Be a primitive type
	 * 2. Be the type for a provided convertor
	 * 3. Have a constructor that accepts a single String argument
	 * 4. Have a static method named valueOf that accepts a single String argument (see, for example, Integer.valueOf(String))
	 * 5. Be List<T>, Set<T> or SortedSet<T>, where T satisfies 2 or 3 above. The resulting collection is read-only.
	 * 
	 * @param values
	 * @param type
	 * @return
	 */
	// TODO handling of primitive types
	@SuppressWarnings("unchecked")
	static <T> T convert(List<String> values, Type type,
			Convertor<?>... convertors) throws UnsupportedFieldType {
		if (values == null) {
			return null;
		}
		if (type instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) type;
			Class rawClass = (Class) pType.getRawType();
			if (Collection.class.isAssignableFrom(rawClass)) {
				Class argClass = (Class) pType.getActualTypeArguments()[0];
				if (List.class.isAssignableFrom(rawClass)) {
					if (String.class.isAssignableFrom(argClass)) {
						return (T) Collections.unmodifiableList(values);
					} else {
						List result = new ArrayList();
						for (String val : values) {
							result.add(convert(val, argClass, convertors));
						}
						return (T) result;
					}
				}
				if (Set.class.isAssignableFrom(rawClass)) {
					if (String.class.isAssignableFrom(argClass)) {
						return (T) Collections.unmodifiableSortedSet(
							new TreeSet<String>(values));
					} else {
						Set result = new TreeSet();
						for (String val : values) {
							result.add(convert(val, argClass, convertors));
						}
						return (T) result;
					}
				}
			}
		}
		if (values.size() == 0) {
			return null;
		}
		if (type instanceof Class) {
			return (T) convert(values.get(0), (Class) type, convertors);
		}
		throw new IllegalArgumentException("Can't handle type: " + type +
				" having getClass(): " + type.getClass());
	}

	@SuppressWarnings("unchecked")
	private static <T> T convert(String value, Class<T> type, Convertor<?>... convertors) throws UnsupportedFieldType {
		if (value == null) {
			return null;
		}
		for (Convertor<?> convertor : convertors) {
			if (((ParameterizedType)convertor.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0].equals(type)) {
				return (T) convertor.convert(value);
			}
		}
		Constructor<T> stringConstructor = null;
		try {
			if (type.isPrimitive()) {
				return getPrimitiveValue(value, type);
			}
			try {
				stringConstructor = type.getConstructor(String.class);
				return stringConstructor.newInstance(value);
			} catch (NoSuchMethodException e) {
				//keep it null
			}
			try {
				Method valueOfMethod = type.getMethod("valueOf",
						String.class);
				return (T) valueOfMethod.invoke(null, value);
			} catch (NoSuchMethodException e) {
				//keep it null
			}

		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		throw new UnsupportedFieldType("cannot convert value to " + type);
	}
	
	private static <T> T getPrimitiveValue(String value, Class<T> type) {
		//boolean, byte, char, short, int, long, float, and double)
		if (type.equals(boolean.class)) {
			return (T) Boolean.valueOf(value);
		}
		if (type.equals(byte.class)) {
			return (T) Byte.valueOf(value);
		}
		if (type.equals(char.class)) {
			return (T) Character.valueOf(value.charAt(0));
		}
		if (type.equals(short.class)) {
			return (T) Short.valueOf(value);
		}
		if (type.equals(int.class)) {
			return (T) Integer.valueOf(value);
		}
		if (type.equals(long.class)) {
			return (T) Long.valueOf(value);
		}
		if (type.equals(float.class)) {
			return (T) Float.valueOf(value);
		}
		if (type.equals(double.class)) {
			return (T) Double.valueOf(value);
		}
		throw new RuntimeException(type+" is not a known primitive type");
	}
}
