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

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.templating.seedsnipe.datastructure.DataFieldResolver;
import org.apache.clerezza.templating.seedsnipe.datastructure.FieldDoesNotHaveDimensionException;
import org.apache.clerezza.templating.seedsnipe.datastructure.FieldIndexOutOfBoundsException;
import org.apache.clerezza.templating.seedsnipe.datastructure.InvalidElementException;

/**
 *
 * @author rbn
 */
public class ExpressionTest {

	private DataFieldResolver dataFieldResolver = new DataFieldResolver() {

		@Override
		public Object resolveAsObject(String fieldName, int[] arrayPos) throws FieldDoesNotHaveDimensionException, FieldIndexOutOfBoundsException, InvalidElementException {
			if ("testfield".equals(fieldName)) {
				return "testvalue";
			}
			if ("aliasfield".equals(fieldName)) {
				return "testvalue";
			}
			if ("yes()".equals(fieldName)) {
				return true;
			}
			if ("object()".equals(fieldName)) {
				return new Object();
			}
			if ("complexfield".equals(fieldName)) {
				return "foo=\"bar\"";
			}
			if ("two".equals(fieldName)) {
				return 2;
			}
			throw new UnsupportedOperationException("Not supported yet.");
		}
	};

	@Test
	public void stringEquality() throws Exception {
		Expression expression = new Expression("\"hello\" = \"hello\"");
		Assert.assertTrue(expression.evaluate(dataFieldResolver, new int[0]));
	}

	@Test
	public void stringInequality() throws Exception {
		Expression expression = new Expression("\"hullo\" = \"hello\"");
		Assert.assertFalse(expression.evaluate(dataFieldResolver, new int[0]));
	}

	@Test
	public void stringFieldEquality() throws Exception {
		Expression expression = new Expression("testfield = \"testvalue\"");
		Assert.assertTrue(expression.evaluate(dataFieldResolver, new int[0]));
	}

	@Test
	public void complexStringFieldEquality() throws Exception {
		Expression expression = new Expression("complexfield = \"foo=\\\"bar\\\"\"");
		Assert.assertTrue(expression.evaluate(dataFieldResolver, new int[0]));
	}

	@Test
	public void fieldEquality() throws Exception {
		Expression expression = new Expression("testfield = aliasfield");
		Assert.assertTrue(expression.evaluate(dataFieldResolver, new int[0]));
	}

	@Test
	public void booleanField() throws Exception {
		Expression expression = new Expression("yes()");
		Assert.assertTrue(expression.evaluate(dataFieldResolver, new int[0]));
	}

	@Test
	public void fieldExistence() throws Exception {
		Expression expression = new Expression("object()");
		Assert.assertTrue(expression.evaluate(dataFieldResolver, new int[0]));
	}

	@Test
	public void compareStringAndNumber() throws Exception {
		Expression expression = new Expression("two = \"2\"");
		Assert.assertTrue(expression.evaluate(dataFieldResolver, new int[0]));
	}
}
