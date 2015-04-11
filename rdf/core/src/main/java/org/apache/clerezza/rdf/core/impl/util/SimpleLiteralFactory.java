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
package org.apache.clerezza.rdf.core.impl.util;

import org.apache.clerezza.commons.rdf.impl.utils.TypedLiteralImpl;
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
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.impl.util.Base64;
import org.apache.clerezza.rdf.core.impl.util.W3CDateFormat;
import org.apache.clerezza.commons.rdf.Literal;

/**
 * An implementation of literal factory currently supporting only
 * byte[]/base64Binary and Java.util.Date/date
 * 
 * @author reto
 */

public class SimpleLiteralFactory extends LiteralFactory {

    private static final String XSD = "http://www.w3.org/2001/XMLSchema#";
    final private static IRI xsdInteger = xsd("integer");
    final private static IRI xsdInt = xsd("int");
    final private static IRI xsdShort = xsd("short");
    final private static IRI xsdByte = xsd("byte");
    final private static IRI xsdLong = xsd("long");
    

    final private static Set<IRI> decimalTypes = new HashSet<IRI>();

    final private static Map<Class<?>, TypeConverter<?>> typeConverterMap = new HashMap<Class<?>, TypeConverter<?>>();
    final static Class<? extends byte[]> byteArrayType;

    static {
        Collections.addAll(decimalTypes, xsdInteger, xsdInt, xsdByte, xsdShort, xsdLong );

        byte[] byteArray = new byte[0];
        byteArrayType = byteArray.getClass();
        typeConverterMap.put(byteArrayType, new ByteArrayConverter());
        typeConverterMap.put(Date.class, new DateConverter());
        typeConverterMap.put(Boolean.class, new BooleanConverter());
        typeConverterMap.put(String.class, new StringConverter());
        typeConverterMap.put(Integer.class, new IntegerConverter());
        typeConverterMap.put(BigInteger.class, new BigIntegerConverter());
        typeConverterMap.put(Long.class, new LongConverter());
        typeConverterMap.put(Double.class, new DoubleConverter());
        typeConverterMap.put(Float.class, new FloatConverter());
        typeConverterMap.put(IRI.class, new UriRefConverter());
    }

    final private static IRI xsdDouble =xsd("double");
    final private static IRI xsdFloat =xsd("float");
    final private static IRI xsdAnyURI =xsd("anyURI");

    final private static IRI xsd(String name) {
       return new IRI(XSD+name);
    }

    private static interface TypeConverter<T> {
        Literal createLiteral(T value);
        T createObject(Literal literal);        
    }

    private static class  ByteArrayConverter implements TypeConverter<byte[]> {

        private static final IRI base64Uri =xsd("base64Binary");

        @Override
        public Literal createLiteral(byte[] value) {
            return new TypedLiteralImpl(Base64.encode((byte[]) value), base64Uri);
        }

        @Override
        public byte[] createObject(Literal literal) {
            if (!literal.getDataType().equals(base64Uri)) {
                throw new InvalidLiteralTypeException(byteArrayType, literal.getDataType());
            }
            return (byte[])Base64.decode(literal.getLexicalForm());
        }

        
    }
    private static class  DateConverter implements TypeConverter<Date> {

        private static final IRI dateTimeUri =xsd("dateTime");
        private static final DateFormat DATE_FORMAT = new W3CDateFormat();

        @Override
        public Literal createLiteral(Date value) {
            return new TypedLiteralImpl(DATE_FORMAT.format(value), dateTimeUri);
        }

        @Override
        public Date createObject(Literal literal) {
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

        private static final IRI booleanUri =xsd("boolean");
        public static final TypedLiteralImpl TRUE = new TypedLiteralImpl("true", booleanUri);
        public static final TypedLiteralImpl FALSE = new TypedLiteralImpl("false", booleanUri);

        @Override
        public Literal createLiteral(Boolean value) {
            if (value) return TRUE;
            else return FALSE;
        }

        @Override
        public Boolean createObject(Literal literal) {
            if (literal == TRUE) return true;
            else if (literal == FALSE) return false;
            else if (!literal.getDataType().equals(booleanUri)) {
                throw new InvalidLiteralTypeException(Boolean.class, literal.getDataType());
            }
            return Boolean.valueOf(literal.getLexicalForm());
        }
    }

    private static class StringConverter implements TypeConverter<String> {

        private static final IRI stringUri =xsd("string");

        @Override
        public Literal createLiteral(String value) {
            return new TypedLiteralImpl(value, stringUri);
        }

        @Override
        public String createObject(Literal literal) {
            if (!literal.getDataType().equals(stringUri)) {
                throw new InvalidLiteralTypeException(String.class, literal.getDataType());
            }
            return literal.getLexicalForm();
        }
    }

    private static class IntegerConverter implements TypeConverter<Integer> {


        @Override
        public Literal createLiteral(Integer value) {
            return new TypedLiteralImpl(value.toString(), xsdInt);
        }

        @Override
        public Integer createObject(Literal literal) {
            if (!decimalTypes.contains(literal.getDataType())) {
                throw new InvalidLiteralTypeException(Integer.class, literal.getDataType());
            }
            return new Integer(literal.getLexicalForm());
        }
    }

    private static class LongConverter implements TypeConverter<Long> {

        

        @Override
        public Literal createLiteral(Long value) {
            return new TypedLiteralImpl(value.toString(), xsdLong);
        }

        @Override
        public Long createObject(Literal literal) {
            if (!decimalTypes.contains(literal.getDataType())) {
                throw new InvalidLiteralTypeException(Long.class, literal.getDataType());
            }
            return new Long(literal.getLexicalForm());
        }
    }


    private static class FloatConverter implements TypeConverter<Float> {

        @Override
        public Literal createLiteral(Float value) {
            return new TypedLiteralImpl(value.toString(), xsdFloat);
        }

        @Override
        public Float createObject(Literal literal) {
            if (!literal.getDataType().equals(xsdFloat)) {
                throw new InvalidLiteralTypeException(Float.class, literal.getDataType());
            }
            return Float.valueOf(literal.getLexicalForm());
        }
    }
    
    private static class DoubleConverter implements TypeConverter<Double> {



        @Override
        public Literal createLiteral(Double value) {
            return new TypedLiteralImpl(value.toString(), xsdDouble);
        }

        @Override
        public Double createObject(Literal literal) {
            if (!literal.getDataType().equals(xsdDouble)) {
                throw new InvalidLiteralTypeException(Double.class, literal.getDataType());
            }
            return new Double(literal.getLexicalForm());
        }
    }

    private static class BigIntegerConverter implements TypeConverter<BigInteger> {



        @Override
        public Literal createLiteral(BigInteger value) {
            return new TypedLiteralImpl(value.toString(), xsdInteger);
        }

        @Override
        public BigInteger createObject(Literal literal) {
            if (!literal.getDataType().equals(xsdInteger)) {
                throw new InvalidLiteralTypeException(Double.class, literal.getDataType());
            }
            return new BigInteger(literal.getLexicalForm());
        }
    }
    
    private static class UriRefConverter implements TypeConverter<IRI> {



        @Override
        public Literal createLiteral(IRI value) {
            return new TypedLiteralImpl(value.getUnicodeString(), xsdAnyURI);
        }

        @Override
        public IRI createObject(Literal literal) {
            if (!literal.getDataType().equals(xsdAnyURI)) {
                throw new InvalidLiteralTypeException(IRI.class, literal.getDataType());
            }
            return new IRI(literal.getLexicalForm());
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public Literal createTypedLiteral(Object value) throws NoConvertorException {
        TypeConverter converter = getConverterFor(value.getClass());
        return converter.createLiteral(value);
    }

    
    
    @Override
    public <T> T createObject(Class<T> type, Literal literal)
            throws NoConvertorException, InvalidLiteralTypeException {
        final TypeConverter<T> converter = getConverterFor(type);
        return converter.createObject(literal);
        
    }

    @SuppressWarnings("unchecked")
    private <T> TypeConverter<T> getConverterFor(Class<T> type) throws NoConvertorException {
        TypeConverter<T> convertor = (TypeConverter<T>) typeConverterMap.get(type);
        if (convertor != null) {
            return convertor;
        }
        for (Map.Entry<Class<?>, TypeConverter<?>> converterEntry : typeConverterMap.entrySet()) {
            if (type.isAssignableFrom(converterEntry.getKey())) {
                return (TypeConverter<T>) converterEntry.getValue();
            }
        }
        throw new NoConvertorException(type);
    }
}
