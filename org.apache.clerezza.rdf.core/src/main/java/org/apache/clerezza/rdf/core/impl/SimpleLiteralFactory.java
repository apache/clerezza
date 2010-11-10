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

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.clerezza.rdf.core.InvalidLiteralTypeException;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.NoConvertorException;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.util.Base64;
import org.apache.clerezza.rdf.core.impl.util.W3CDateFormat;
import org.apache.clerezza.rdf.ontologies.XSD;


/**
 * An implementation of literal factory currently supporting only
 * byte[]/base64Binary and Java.util.Date/date
 * 
 * @author reto
 */

public class SimpleLiteralFactory extends LiteralFactory {

	

	final private static Set<UriRef> decimalTypes = new HashSet<UriRef>();
	static {
		Collections.addAll(decimalTypes, XSD.integer, XSD.int_, XSD.byte_, XSD.short_, XSD.long_, XSD.negativeInteger,
				  XSD.nonNegativeInteger, XSD.nonPositiveInteger, XSD.positiveInteger);
	}


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



		@Override
		public TypedLiteral createTypedLiteral(byte[] value) {
			return new TypedLiteralImpl(Base64.encode((byte[]) value), XSD.base64Binary);
		}

		@Override
		public byte[] createObject(TypedLiteral literal) {
			if (!literal.getDataType().equals(XSD.base64Binary)) {
				throw new InvalidLiteralTypeException(byteArrayType, literal.getDataType());
			}
			return (byte[])Base64.decode(literal.getLexicalForm());
		}

		
	}
	private static class  DateConverter implements TypeConverter<Date> {

		private static final DateFormat DATE_FORMAT = new W3CDateFormat();

		@Override
		public TypedLiteral createTypedLiteral(Date value) {
			return new TypedLiteralImpl(DATE_FORMAT.format(value), XSD.dateTime);
		}

		@Override
		public Date createObject(TypedLiteral literal) {
			if (!literal.getDataType().equals(XSD.dateTime)) {
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


		@Override
		public TypedLiteral createTypedLiteral(Boolean value) {
			return new TypedLiteralImpl(value.toString(), XSD.boolean_);
		}

		@Override
		public Boolean createObject(TypedLiteral literal) {
			if (!literal.getDataType().equals(XSD.boolean_)) {
				throw new InvalidLiteralTypeException(Boolean.class, literal.getDataType());
			}
			return Boolean.valueOf(literal.getLexicalForm());
		}
	}

	private static class StringConverter implements TypeConverter<String> {

		@Override
		public TypedLiteral createTypedLiteral(String value) {
			return new TypedLiteralImpl(value, XSD.string);
		}

		@Override
		public String createObject(TypedLiteral literal) {
			if (!literal.getDataType().equals(XSD.string)) {
				throw new InvalidLiteralTypeException(String.class, literal.getDataType());
			}
			return literal.getLexicalForm();
		}
	}

	private static class IntegerConverter implements TypeConverter<Integer> {


		@Override
		public TypedLiteral createTypedLiteral(Integer value) {
			return new TypedLiteralImpl(value.toString(), XSD.int_);
		}

		@Override
		public Integer createObject(TypedLiteral literal) {
			if (!decimalTypes.contains(literal.getDataType())) {
				throw new InvalidLiteralTypeException(Integer.class, literal.getDataType());
			}
			//todo: warning: this can throw exception if passed integer is too big
			return new Integer(literal.getLexicalForm());
		}
	}

	private static class LongConverter implements TypeConverter<Long> {

		

		@Override
		public TypedLiteral createTypedLiteral(Long value) {
			return new TypedLiteralImpl(value.toString(), XSD.integer);
		}

		@Override
		public Long createObject(TypedLiteral literal) {
			if (!decimalTypes.contains(literal.getDataType())) {
				throw new InvalidLiteralTypeException(Long.class, literal.getDataType());
			}
			//todo: warning: this can throw exception if passed integer is too big
			return new Long(literal.getLexicalForm());
		}
	}

	private static class DoubleConverter implements TypeConverter<Double> {



		@Override
		public TypedLiteral createTypedLiteral(Double value) {
			return new TypedLiteralImpl(value.toString(), XSD.double_);
		}

		@Override
		public Double createObject(TypedLiteral literal) {
			if (!literal.getDataType().equals(XSD.double_)) {
				throw new InvalidLiteralTypeException(Double.class, literal.getDataType());
			}
			return new Double(literal.getLexicalForm());
		}
	}

	private static class BigIntegerConverter implements TypeConverter<BigInteger> {



		@Override
		public TypedLiteral createTypedLiteral(BigInteger value) {
			return new TypedLiteralImpl(value.toString(), XSD.integer);
		}

		@Override
		public BigInteger createObject(TypedLiteral literal) {
			if (!decimalTypes.contains(literal.getDataType())) {
				throw new InvalidLiteralTypeException(Double.class, literal.getDataType());
			}
			return new BigInteger(literal.getLexicalForm());
		}
	}
	
	private static class UriRefConverter implements TypeConverter<UriRef> {



		@Override
		public TypedLiteral createTypedLiteral(UriRef value) {
			return new TypedLiteralImpl(value.getUnicodeString(), XSD.anyURI);
		}

		@Override
		public UriRef createObject(TypedLiteral literal) {
			if (!literal.getDataType().equals(XSD.anyURI)) {
				throw new InvalidLiteralTypeException(UriRef.class, literal.getDataType());
			}
			return new UriRef(literal.getLexicalForm());
		}
	}

	final private static Map<Class<?>, TypeConverter<?>> typeConverterMap = new HashMap<Class<?>, TypeConverter<?>>();

	static {
		typeConverterMap.put(byteArrayType, new ByteArrayConverter());
		typeConverterMap.put(Date.class, new DateConverter());
		typeConverterMap.put(Boolean.class, new BooleanConverter());
		typeConverterMap.put(String.class, new StringConverter());
		typeConverterMap.put(Integer.class, new IntegerConverter());
		typeConverterMap.put(BigInteger.class, new BigIntegerConverter());
		typeConverterMap.put(Long.class, new LongConverter());
		typeConverterMap.put(Double.class, new DoubleConverter());
		typeConverterMap.put(UriRef.class, new UriRefConverter());
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
