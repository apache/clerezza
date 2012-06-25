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
import java.io.StringWriter;
import java.io.Writer;

import java.util.SortedMap;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.templating.seedsnipe.datastructure.DataFieldResolver;
import org.apache.clerezza.templating.seedsnipe.datastructure.FieldDoesNotHaveDimensionException;
import org.apache.clerezza.templating.seedsnipe.datastructure.FieldIndexOutOfBoundsException;
import org.apache.clerezza.templating.seedsnipe.datastructure.InvalidElementException;

/**
 * The loop-keyword handler for {@link DefaultParser}.
 * 
 * @author daniel, reto
 * 
 */
class LoopKeywordResolver implements KeywordResolver {

	private static Logger log = LoggerFactory.getLogger(LoopKeywordResolver.class);

	@Override
	public void resolve(DefaultParser parser, String parametersString,
			Reader oin, Writer out, DataFieldResolver dataFieldResolver,
			int[] arrayPositioner) throws IOException {

		if (out != null) {
			out.flush();
		}

		String[] endMarkers = {"/loop"};

		if (!(oin instanceof MultiMarkLineNumberReader)) {
			throw new RuntimeException("MultiMarkLineNumberReader required");
		}
		MultiMarkLineNumberReader in = (MultiMarkLineNumberReader) oin;
		in.mark(64000); // this is the maximum codelength in a loop

		int[] newArrayPositioner = new int[arrayPositioner.length + 1]; // add a loop recursion
		for (int i = 0; i < arrayPositioner.length; i++) {
			newArrayPositioner[i] = arrayPositioner[i]; // copy existing values
		}
		newArrayPositioner[newArrayPositioner.length - 1] = 0;

		String[] parameters = parametersString.trim().split(" ");
		boolean sortValues;
		SortedMap<Object, String> sortedMap = null;
		boolean invertSortOrder = false;
		String sortKeyField = null;
		if (parameters[0].equals("sort")) {
			sortValues = true;
			if (parameters[1].equals("desc")) {
				invertSortOrder = true;
			} else {
				if (!parameters[1].equals("asc")) {
					throw new RuntimeException(
							"the keyword sort must be followed by either asc or desc");
				}
			}
			sortedMap = new TreeMap<Object, String>();
			sortKeyField = parameters[2];
		} else {
			sortValues = false;
		}

		while (true) {
			final WrappedDataFieldResolver wrappedDataFieldResolver = new WrappedDataFieldResolver(dataFieldResolver, arrayPositioner.length);
			final StringWriter outBuffer = new StringWriter();
			if (out != null) { // parse loop content
				parser.perform(outBuffer, endMarkers, newArrayPositioner, wrappedDataFieldResolver);
			} else { // skip to the end tag
				parser.perform(null, endMarkers, newArrayPositioner, wrappedDataFieldResolver);
			}
			if (out == null) {
				break;
			}
			if (!wrappedDataFieldResolver.isFieldResolvedUsingFullArrayPos()) {
				// none of the fields evaluating to a value used the full length
				// of the array positioner (so incrementing the loop variable
				// wouldn't yield to a different result)
				break;
			} else {
				if (out != null) {
					if (sortValues) {
						Object value = null;
						try {
							value = dataFieldResolver.resolveAsObject(sortKeyField,
									newArrayPositioner);

						} catch (FieldDoesNotHaveDimensionException ex) {
							log.warn("the sort-key" + sortKeyField + "doesn't have as many dimensions as loops are nested");
						} catch (FieldIndexOutOfBoundsException ex) {
							log.debug("the sort-key" + sortKeyField + "end before an other field in the loop");
						} catch (InvalidElementException ex) {
							log.warn("the sort-key" + sortKeyField + "is an invalid elemend");
						}
						SortKeyValue sortKeyValue = new SortKeyValue(
								newArrayPositioner[newArrayPositioner.length - 1],
								value, invertSortOrder);
						sortedMap.put(sortKeyValue, outBuffer.toString());
					} else {
						out.write(outBuffer.toString());
					}
				}
				in.reset(); // reset back to the beginning of the loop
			}
			newArrayPositioner[newArrayPositioner.length - 1]++;// increment loop variable

		}
		in.removeMark(); // recursion ended, remove corresponding mark
		if (sortValues) {
			for (String string : sortedMap.values()) {
				out.write(string);
			}
		}
		return;
	}

	/**
	 * a comparable object, comparing value as first criterion and pos as second
	 */
	private static class SortKeyValue implements Comparable<SortKeyValue> {

		private int pos;
		private Object value;
		private boolean invert;

		public SortKeyValue(int pos, Object value, boolean invert) {
			this.pos = pos;
			this.value = value;
			this.invert = invert;
		}

		private int normalCompareTo(SortKeyValue o) {
			if (value == null) {
				if (o.value == null) {
					return compareByPos(o);
				} else {
					return -1;
				}
			}
			if (o.value == null) {
				return 1;
			}
			int valueCompare = valueCompare(o);
			if (valueCompare == 0) {
				return compareByPos(o);
			} else {
				return valueCompare;
			}
		}

		private int compareByPos(SortKeyValue o) {
			return pos - o.pos;
		}

		@Override
		public int compareTo(SortKeyValue o) {
			if (invert) {
				return -normalCompareTo(o);
			} else {
				return normalCompareTo(o);
			}
		}

		private int valueCompare(SortKeyValue o) {

			if (value instanceof Comparable) {
				try {
					return ((Comparable) value).compareTo(o.value);
				} catch (Exception e) {
					log.debug("Falling back to string coparison on exception in compareTo");
				}
			}
			return value.toString().compareTo(o.value.toString());
		}
	}

	static class WrappedDataFieldResolver extends DataFieldResolver {

		private DataFieldResolver wrapped;
		private int dimension;
		private boolean fieldResolvedUsingFullArrayPos = false;

		private WrappedDataFieldResolver(DataFieldResolver dataFieldResolver, int dimension) {
			wrapped = dataFieldResolver;
			this.dimension = dimension;
		}

		@Override
		public Object resolveAsObject(String fieldName, int[] arrayPos)
				throws FieldDoesNotHaveDimensionException,
				FieldIndexOutOfBoundsException, InvalidElementException, IOException {
			Object result = null;
			try {
				result = wrapped.resolveAsObject(fieldName, arrayPos);
			} catch (FieldIndexOutOfBoundsException ex) {
				if (ex.getDimension() > dimension) {
					fieldResolvedUsingFullArrayPos = true;
				}
				throw ex;
			}
			fieldResolvedUsingFullArrayPos = true;
			return result;
		}

		public boolean isFieldResolvedUsingFullArrayPos() {
			return fieldResolvedUsingFullArrayPos;
		}

		@Override
		public String resolve(String fieldName, int[] arrayPos)
				throws FieldDoesNotHaveDimensionException,
				FieldIndexOutOfBoundsException, InvalidElementException,
				IOException {
			String result = null;
			try {
				result = wrapped.resolve(fieldName, arrayPos);
			} catch (FieldIndexOutOfBoundsException ex) {
				if (ex.getDimension() > dimension) {
					fieldResolvedUsingFullArrayPos = true;
				}
				throw ex;
			}
			fieldResolvedUsingFullArrayPos = true;
			return result;
		}
	}
}