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
package org.apache.clerezza.rdf.core.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.clerezza.rdf.core.InvalidLiteralTypeException;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.NoConvertorException;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.util.Base64;
import org.apache.clerezza.rdf.core.impl.util.W3CDateFormat;

/**
 * An implementation of literal factory currently supporting only
 * byte[]/base64Binary and Java.util.Date/date
 * 
 * @author reto
 */

public class SimpleLiteralFactory extends LiteralFactory {

	final static Class<? extends byte[]> byteArrayType;

	static {
		byte[] byteArray = new byte[0];
		byteArrayType = byteArray.getClass();
	}

	private static interface TypeConverter<T> {
		TypedLiteral createTypedLiteral(T value);
		T createObject(TypedLiteral literal);		
	}

	private static class  ByteArrayConverter implements TypeConverter<byte[]> {

		private static final UriRef base64Uri =
			new UriRef("http://www.w3.org/2001/XMLSchema#base64Binary");

		@Override
		public TypedLiteral createTypedLiteral(byte[] value) {
			return new TypedLiteralImpl(Base64.encode((byte[]) value), base64Uri);
		}

		@Override
		public byte[] createObject(TypedLiteral literal) {
			if (!literal.getDataType().equals(base64Uri)) {
				throw new InvalidLiteralTypeException(byteArrayType, literal.getDataType());
			}
			return (byte[])Base64.decode(literal.getLexicalForm());
		}

		
	}
	private static class  DateConverter implements TypeConverter<Date> {

		private static final UriRef dateTimeUri =
			new UriRef("http://www.w3.org/2001/XMLSchema#dateTime");
		private static final DateFormat DATE_FORMAT = new W3CDateFormat();

		@Override
		public TypedLiteral createTypedLiteral(Date value) {
			return new TypedLiteralImpl(DATE_FORMAT.format(value), dateTimeUri);
		}

		@Override
		public Date createObject(TypedLiteral literal) {
			if (!literal.getDataType().equals(dateTimeUri)) {
				throw new InvalidLiteralTypeException(Date.class, literal.getDataType());
			}
			try {
				return DATE_FORMAT.parse(literal.getLexicalForm());
			} catch (ParseException ex) {
				throw new RuntimeException("Exception parsing literal as date", ex);
			}
		}


	}

	private static class BooleanConverter implements TypeConverter<Boolean> {

		private static final UriRef booleanUri =
			new UriRef("http://www.w3.org/2001/XMLSchema#boolean");

		@Override
		public TypedLiteral createTypedLiteral(Boolean value) {
			return new TypedLiteralImpl(value.toString(), booleanUri);
		}

		@Override
		public Boolean createObject(TypedLiteral literal) {
			if (!literal.getDataType().equals(booleanUri)) {
				throw new InvalidLiteralTypeException(Boolean.class, literal.getDataType());
			}
			return new Boolean(literal.getLexicalForm());
		}
	}

	private static class StringConverter implements TypeConverter<String> {

		private static final UriRef stringUri =
			new UriRef("http://www.w3.org/2001/XMLSchema#string");

		@Override
		public TypedLiteral createTypedLiteral(String value) {
			return new TypedLiteralImpl(value, stringUri);
		}

		@Override
		public String createObject(TypedLiteral literal) {
			if (!literal.getDataType().equals(stringUri)) {
				throw new InvalidLiteralTypeException(String.class, literal.getDataType());
			}
			return literal.getLexicalForm();
		}
	}

	private static class IntegerConverter implements TypeConverter<Integer> {

		private static final UriRef intUri =
			new UriRef("http://www.w3.org/2001/XMLSchema#int");

		@Override
		public TypedLiteral createTypedLiteral(Integer value) {
			return new TypedLiteralImpl(value.toString(), intUri);
		}

		@Override
		public Integer createObject(TypedLiteral literal) {
			if (!literal.getDataType().equals(intUri)) {
				throw new InvalidLiteralTypeException(Integer.class, literal.getDataType());
			}
			return new Integer(literal.getLexicalForm());
		}
	}

	private static class LongConverter implements TypeConverter<Long> {

		private static final UriRef longUri =
			new UriRef("http://www.w3.org/2001/XMLSchema#integer");

		@Override
		public TypedLiteral createTypedLiteral(Long value) {
			return new TypedLiteralImpl(value.toString(), longUri);
		}

		@Override
		public Long createObject(TypedLiteral literal) {
			if (!literal.getDataType().equals(longUri)) {
				throw new InvalidLiteralTypeException(Long.class, literal.getDataType());
			}
			return new Long(literal.getLexicalForm());
		}
	}

	private static class DoubleConverter implements TypeConverter<Double> {

		private static final UriRef doubleUri =
			new UriRef("http://www.w3.org/2001/XMLSchema#double");

		@Override
		public TypedLiteral createTypedLiteral(Double value) {
			return new TypedLiteralImpl(value.toString(), doubleUri);
		}

		@Override
		public Double createObject(TypedLiteral literal) {
			if (!literal.getDataType().equals(doubleUri)) {
				throw new InvalidLiteralTypeException(Double.class, literal.getDataType());
			}
			return new Double(literal.getLexicalForm());
		}
	}

	private Map<Class<?>, TypeConverter<?>> typeConverterMap = new HashMap<Class<?>, TypeConverter<?>>();

	{
		typeConverterMap.put(byteArrayType, new ByteArrayConverter());
		typeConverterMap.put(Date.class, new DateConverter());
		typeConverterMap.put(Boolean.class, new BooleanConverter());
		typeConverterMap.put(String.class, new StringConverter());
		typeConverterMap.put(Integer.class, new IntegerConverter());
		typeConverterMap.put(Long.class, new LongConverter());
		typeConverterMap.put(Double.class, new DoubleConverter());
	}


	@SuppressWarnings("unchecked")
	@Override
	public TypedLiteral createTypedLiteral(Object value) throws NoConvertorException {
		for (Map.Entry<Class<?>, TypeConverter<?>> converterEntry : typeConverterMap.entrySet()) {
			if (converterEntry.getKey().isAssignableFrom(value.getClass())) {
				TypeConverter<Object> converter = (TypeConverter<Object>) converterEntry.getValue();
				return converter.createTypedLiteral(value);
			}
		}
		throw new NoConvertorException(value.getClass());

	}

	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T createObject(Class<T> type, TypedLiteral literal)
			throws NoConvertorException, InvalidLiteralTypeException {
		for (Map.Entry<Class<?>, TypeConverter<?>> converterEntry : typeConverterMap.entrySet()) {
			if (type.isAssignableFrom(converterEntry.getKey())) {
				TypeConverter<T> converter = (TypeConverter<T>) converterEntry.getValue();
				return converter.createObject(literal);
			}
		}
		throw new NoConvertorException(type);
	}
}
