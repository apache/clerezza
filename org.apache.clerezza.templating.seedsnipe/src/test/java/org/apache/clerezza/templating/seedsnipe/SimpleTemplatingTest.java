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
package org.apache.clerezza.templating.seedsnipe;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Test;
import org.junit.Assert;

import org.apache.clerezza.templating.seedsnipe.datastructure.DataFieldResolver;
import org.apache.clerezza.templating.seedsnipe.datastructure.FieldIndexOutOfBoundsException;
import org.apache.clerezza.templating.seedsnipe.simpleparser.DefaultParser;

/**
 * Unit test for simple Simple Templating.
 * 
 * @author daniel
 */
public class SimpleTemplatingTest {

	@Test
	public void fieldTest() throws IOException {
		DataFieldResolver dataFieldResolver = new DataFieldResolver() {

			@Override
			public Object resolveAsObject(String fieldName, int[] arrayPos)
					throws FieldIndexOutOfBoundsException {
				if (fieldName.equals("number")) {
					return 5;
				} else {
					throw new FieldIndexOutOfBoundsException(fieldName,
							arrayPos, 0);
				}
			}
		};

		StringReader reader = new StringReader("${number}${foo}");
		StringWriter writer = new StringWriter();
		new DefaultParser(reader, writer).perform(dataFieldResolver);

		Assert.assertTrue(writer.toString().startsWith(
				"5<!-- could not resolve: foo "));
	}

	@Test
	public void loopTest() throws IOException {
		DataFieldResolver dataFieldResolver = new DataFieldResolver() {
			private int[] numberArray = { 0, 1, 2, 3, 4, 5 };

			public Object resolveAsObject(String fieldName, int[] arrayPos)
					throws FieldIndexOutOfBoundsException {
				if (arrayPos[0] == numberArray.length) {
					throw new FieldIndexOutOfBoundsException(fieldName,
							arrayPos, 0);
				}
				return numberArray[arrayPos[0]];
			}
		};

		StringReader reader = new StringReader("${loop}${numberArray}${/loop}");
		StringWriter writer = new StringWriter();
		new DefaultParser(reader, writer).perform(dataFieldResolver);

		Assert.assertEquals("012345", writer.toString());
	}

	@Test
	public void ifInloopTest() throws IOException {
		DataFieldResolver dataFieldResolver = new DataFieldResolver() {
			private int[] numberArray = { 0, 1, 2, 3, 4, 5 };

			public Object resolveAsObject(String fieldName, int[] arrayPos)
					throws FieldIndexOutOfBoundsException {
				if (arrayPos[0] == numberArray.length) {
					throw new FieldIndexOutOfBoundsException(fieldName,
							arrayPos, 0);
				}
				return numberArray[arrayPos[0]];
			}
		};

		// no else block
		StringReader reader = new StringReader("${loop}"
				+ "${if numberArray = \"2\"}" + "${numberArray}" + "${/if}"
				+ "${/loop}");
		StringWriter writer = new StringWriter();
		new DefaultParser(reader, writer).perform(dataFieldResolver);
		Assert.assertEquals("2", writer.toString());

		StringReader reader2 = new StringReader("${loop}" + "${numberArray}"
				+ "${if numberArray = \"2\"}" + "${numberArray}" + "${/if}"
				+ "${/loop}");
		StringWriter writer2 = new StringWriter();
		new DefaultParser(reader2, writer2).perform(dataFieldResolver);
		Assert.assertEquals("0122345", writer2.toString());

		StringReader reader3 = new StringReader("${loop}"
				+ "${if numberArray = \"2\"}" + "${numberArray}" + "${/if}"
				+ "${numberArray}" + "${/loop}");
		StringWriter writer3 = new StringWriter();
		new DefaultParser(reader3, writer3).perform(dataFieldResolver);
		Assert.assertEquals("0122345", writer3.toString());

		// empty else block
		StringReader reader4 = new StringReader("${loop}"
				+ "${if numberArray = \"2\"}" + "${numberArray}" + "${else}"
				+ "${/if}" + "${/loop}");
		StringWriter writer4 = new StringWriter();
		new DefaultParser(reader4, writer4).perform(dataFieldResolver);
		Assert.assertEquals("2", writer4.toString());

		StringReader reader5 = new StringReader("${loop}" + "${numberArray}"
				+ "${if numberArray = \"2\"}" + "${numberArray}" + "${else}"
				+ "${/if}" + "${/loop}");
		StringWriter writer5 = new StringWriter();
		new DefaultParser(reader5, writer5).perform(dataFieldResolver);
		Assert.assertEquals("0122345", writer5.toString());

		StringReader reader6 = new StringReader("${loop}"
				+ "${if numberArray = \"2\"}" + "${numberArray}" + "${else}"
				+ "${/if}" + "${numberArray}" + "${/loop}");
		StringWriter writer6 = new StringWriter();
		new DefaultParser(reader6, writer6).perform(dataFieldResolver);
		Assert.assertEquals("0122345", writer6.toString());

		// with content in else block
		StringReader reader7 = new StringReader("${loop}"
				+ "${if numberArray = \"2\"}" + "${numberArray}" + "${else}"
				+ "${numberArray}" + "${/if}" + "${/loop}");
		StringWriter writer7 = new StringWriter();
		new DefaultParser(reader7, writer7).perform(dataFieldResolver);
		Assert.assertEquals("012345", writer7.toString());

		StringReader reader8 = new StringReader("${loop}" + "${numberArray}"
				+ "${if numberArray = \"2\"}" + "${numberArray}" + "${else}"
				+ "${numberArray}" + "${/if}" + "${/loop}");
		StringWriter writer8 = new StringWriter();
		new DefaultParser(reader8, writer8).perform(dataFieldResolver);
		Assert.assertEquals("001122334455", writer8.toString());

		StringReader reader9 = new StringReader("${loop}"
				+ "${if numberArray = \"2\"}" + "${numberArray}" + "${else}"
				+ "${numberArray}" + "${/if}" + "${numberArray}" + "${/loop}");
		StringWriter writer9 = new StringWriter();
		new DefaultParser(reader9, writer9).perform(dataFieldResolver);
		Assert.assertEquals("001122334455", writer9.toString());
	}

	@Test
	public void sortedLoopTest() throws IOException {
		DataFieldResolver dataFieldResolver = new DataFieldResolver() {
			private int[] numberArray = { 4, 1, 5, 3, 0, 2 };

			public Object resolveAsObject(String fieldName, int[] arrayPos)
					throws FieldIndexOutOfBoundsException {
				if (arrayPos[0] == numberArray.length) {
					throw new FieldIndexOutOfBoundsException(fieldName,
							arrayPos, 0);
				}
				return numberArray[arrayPos[0]];
			}
		};

		StringReader reader = new StringReader(
				"${loop sort asc numberArray}${numberArray}${/loop}");
		StringWriter writer = new StringWriter();
		new DefaultParser(reader, writer).perform(dataFieldResolver);

		Assert.assertEquals("012345", writer.toString());
	}

	@Test
	public void sortDescendingLoopTest() throws IOException {
		DataFieldResolver dataFieldResolver = new DataFieldResolver() {
			private int[] numberArray = { 4, 1, 5, 3, 0, 2 };

			public Object resolveAsObject(String fieldName, int[] arrayPos)
					throws FieldIndexOutOfBoundsException {
				if (arrayPos[0] == numberArray.length) {
					throw new FieldIndexOutOfBoundsException(fieldName,
							arrayPos, 0);
				}
				return numberArray[arrayPos[0]];
			}
		};

		StringReader reader = new StringReader(
				"${loop sort desc numberArray}${numberArray}${/loop}");
		StringWriter writer = new StringWriter();
		new DefaultParser(reader, writer).perform(dataFieldResolver);

		Assert.assertEquals("543210", writer.toString());
	}

	// it should sort by the value of the field not its string-representation
	@Test
	public void sortedLoopNonStringTest() throws IOException {
		DataFieldResolver dataFieldResolver = new DataFieldResolver() {
			private int[] numberArray = { 4, 1, 10, 15 };

			public Object resolveAsObject(String fieldName, int[] arrayPos)
					throws FieldIndexOutOfBoundsException {
				if (arrayPos[0] == numberArray.length) {
					throw new FieldIndexOutOfBoundsException(fieldName,
							arrayPos, 0);
				}
				return numberArray[arrayPos[0]];
			}
		};

		StringReader reader = new StringReader(
				"${loop sort asc numberArray}${numberArray}${/loop}");
		StringWriter writer = new StringWriter();
		new DefaultParser(reader, writer).perform(dataFieldResolver);

		Assert.assertEquals("141015", writer.toString());
	}

	@Test
	public void sortNotComparable() throws IOException {
		DataFieldResolver dataFieldResolver = new DataFieldResolver() {
			Object value1 = new Object() {
				@Override
				public String toString() {
					return "*1";
				}
			};
			Object value2 = new Object() {
				@Override
				public String toString() {
					return "*2";
				}
			};
			private Object[] numberArray = { value2, value1 };

			public Object resolveAsObject(String fieldName, int[] arrayPos)
					throws FieldIndexOutOfBoundsException {
				if (arrayPos[0] == numberArray.length) {
					throw new FieldIndexOutOfBoundsException(fieldName,
							arrayPos, 0);
				}
				return numberArray[arrayPos[0]];
			}
		};

		StringReader reader = new StringReader(
				"${loop sort asc numberArray}${numberArray}${/loop}");
		StringWriter writer = new StringWriter();
		new DefaultParser(reader, writer).perform(dataFieldResolver);

		Assert.assertEquals("*1*2", writer.toString());
	}

	@Test
	public void nestedLoopTest() throws IOException {
		
		//outer field contains less elements than inner field
		StringReader reader = new StringReader("${loop}${loop}${outer}${inner}${/loop}${/loop}");
		StringWriter writer = new StringWriter();
		new DefaultParser(reader, writer).perform(
				createOuterInnerDataFieldResolver(new int[]{0, 1}, new int[]{0, 1, 2}));
		
		Assert.assertEquals("000102101112", writer.toString());
		
		//outer field contains more elements than inner field
		reader = new StringReader("${loop}${loop}${outer}${inner}${/loop}${/loop}");
		writer = new StringWriter();
		new DefaultParser(reader, writer).perform(
				createOuterInnerDataFieldResolver(new int[]{0, 1, 2}, new int[]{0, 1}));
		Assert.assertEquals("000110112021", writer.toString());
				
		//inner and outer field have same length
		reader = new StringReader("${loop}${loop}${outer}${inner}${/loop}${/loop}");
		writer = new StringWriter();
		new DefaultParser(reader, writer).perform(
				createOuterInnerDataFieldResolver(new int[]{0, 1}, new int[]{2, 3}));
		
		Assert.assertEquals("02031213", writer.toString());
		
		//outer field is empty
		reader = new StringReader("${loop}${loop}${outer}${inner}${/loop}${/loop}");
		writer = new StringWriter();
		new DefaultParser(reader, writer).perform(
				createOuterInnerDataFieldResolver(new int[]{}, new int[]{0, 1}));
		
		Assert.assertEquals("", writer.toString());
		
		//inner field is empty
		reader = new StringReader("${loop}${outer}${loop}${inner}${/loop}${/loop}");
		writer = new StringWriter();
		new DefaultParser(reader, writer).perform(
				createOuterInnerDataFieldResolver(new int[]{0,1}, new int[]{}));
		
		Assert.assertEquals("01", writer.toString());

		//inner field is empty, and no access to outer
		reader = new StringReader("${loop}*${loop}${inner}${/loop}${/loop}");
		writer = new StringWriter();
		new DefaultParser(reader, writer).perform(
				createOuterInnerDataFieldResolver(new int[]{0,1}, new int[]{}));

		Assert.assertEquals("**", writer.toString());
		
		//both fields are empty
		reader = new StringReader("${loop}${loop}${outer}${inner}${/loop}${/loop}");
		writer = new StringWriter();
		new DefaultParser(reader, writer).perform(
				createOuterInnerDataFieldResolver(new int[]{}, new int[]{}));
		
		Assert.assertEquals("", writer.toString());
	}

	@Test
	public void ifTest() throws IOException {
		DataFieldResolver dataFieldResolver = new DataFieldResolver() {
			public Object resolveAsObject(String fieldName, int[] arrayPos) {
				if (fieldName.equals("number"))
					return 5;
				else
					return 1;
			}
		};

		StringReader reader = new StringReader(
				"${number}${if number = \"5\"}==5${else}!=5${/if}"
						+ "${number2}${if number2 = \"5\"}==5${else}!=5${/if}");
		StringWriter writer = new StringWriter();
		new DefaultParser(reader, writer).perform(dataFieldResolver);

		Assert.assertEquals("5==51!=5", writer.toString());
	}

	@Test
	public void combinedTest() throws IOException {

		StringReader reader = new StringReader("${loop}" + "${loop}"
				+ "${if outer = \"1\"}"
				+ "${if inner = \"2\"}${outer}${inner}${/if}" + "${/if}"
				+ "${/loop}" + "${/loop}");
		StringWriter writer = new StringWriter();
		new DefaultParser(reader, writer).perform(
				createOuterInnerDataFieldResolver(new int[]{0,1}, new int[]{0,1,2}));

		Assert.assertEquals("12", writer.toString());

		reader = new StringReader("${loop}" + "${loop}"
				+ "${if outer = inner}same${/if}" + "${/loop}" + "${/loop}");
		writer = new StringWriter();
		new DefaultParser(reader, writer).perform(
				createOuterInnerDataFieldResolver(new int[]{0,1}, new int[]{0,1,2}));

		Assert.assertEquals("samesame", writer.toString());
	}
	
	/*
	Creates a DataFieldResolver for nested loops. 
	The fieldname of the outer loop is "outer" and the fieldname of the inner loop is "inner".
	*/
	private DataFieldResolver createOuterInnerDataFieldResolver(final int[] outer, final int[] inner) {
		return new DataFieldResolver() {
			public Object resolveAsObject(String fieldName, int[] arrayPos)
					throws FieldIndexOutOfBoundsException {
				
				if(arrayPos[0] == outer.length) {
					throw new FieldIndexOutOfBoundsException(fieldName, arrayPos, 0);					
				} else if(arrayPos.length == 2 && arrayPos[1] == inner.length) {
					throw new FieldIndexOutOfBoundsException(fieldName, arrayPos, 1);					
				}
				
				if(fieldName.equals("outer")) {
					return outer[arrayPos[0]];
				} else {
					return inner[arrayPos[1]];
				}
			}
		};
	}
}
