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

import junit.framework.Assert;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class SimpleLiteralFactoryTest {

	final private static UriRef xsdInteger = 
			new UriRef("http://www.w3.org/2001/XMLSchema#integer");
	final private static UriRef xsdInt =
			new UriRef("http://www.w3.org/2001/XMLSchema#int");
	SimpleLiteralFactory simpleLiteralFactory = new SimpleLiteralFactory();

	@Test
	public void longToXsdIntegerAndBackToMany() {
		long value = 14l;
		TypedLiteral tl = simpleLiteralFactory.createTypedLiteral(value);
		Assert.assertEquals(xsdInteger, tl.getDataType());
		long longValue = simpleLiteralFactory.createObject(Long.class, tl);
		Assert.assertEquals(value, longValue);
		int intValue = simpleLiteralFactory.createObject(Integer.class, tl);
		Assert.assertEquals(value, intValue);
	}

	@Test
	public void intToXsdIntAndBackToMany() {
		int value = 14;
		TypedLiteral tl = simpleLiteralFactory.createTypedLiteral(value);
		Assert.assertEquals(xsdInt, tl.getDataType());
		long longValue = simpleLiteralFactory.createObject(Long.class, tl);
		Assert.assertEquals(value, longValue);
		int intValue = simpleLiteralFactory.createObject(Integer.class, tl);
		Assert.assertEquals(value, intValue);
	}
}
