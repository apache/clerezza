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

import org.junit.Test;
import junit.framework.Assert;

import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
/**
 *
 * @author reto
 *
 */

public class TypedLiteralImplTest {
    
	
	@Test public void typedLiteralEquality() {
		String stringValue = "some text";
		UriRef uriRef = new UriRef("http://example.org/datatypes/magic");
		TypedLiteral literal1 = new TypedLiteralImpl(stringValue, uriRef);
		TypedLiteral literal2 = new TypedLiteralImpl(stringValue, uriRef);		
		Assert.assertEquals(literal1, literal2);
		Assert.assertEquals(literal1.hashCode(), literal2.hashCode());
		TypedLiteral literal3 = new TypedLiteralImpl("something else", uriRef);
		Assert.assertFalse(literal1.equals(literal3));
		UriRef uriRef2 = new UriRef("http://example.org/datatypes/other");
		TypedLiteral literal4 = new TypedLiteralImpl(stringValue, uriRef2);
		Assert.assertFalse(literal1.equals(literal4));
	}


	/**
	 * The hascode is equals to the hascode of the lexical form plus the hashcode of the dataTyp
	 */
	@Test public void checkHashCode() {
		String stringValue = "some text";
		UriRef uriRef = new UriRef("http://example.org/datatypes/magic");
		TypedLiteral literal =  new TypedLiteralImpl(stringValue, uriRef);
		Assert.assertEquals(stringValue.hashCode() + uriRef.hashCode(), literal.hashCode());
	}

}
