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

package org.apache.clerezza.rdf.cris;

import java.util.ArrayList;
import org.apache.lucene.search.SortField;

/**
 * Specifies CRIS result sorting. 
 *
 * @author daniel
 */
public class SortSpecification {
	
	/**
	 * Interpret values as bytes.
	 */
	public static final int BYTE = SortField.BYTE;
	
	/**
	 * Interpret values as doubles.
	 */
	public static final int DOUBLE = SortField.DOUBLE;
	
	/**
	 * Interpret values as floats.
	 */
	public static final int FLOAT = SortField.FLOAT;
	
	/**
	 * Interpret values as integers.
	 */
	public static final int INT = SortField.INT;
	
	/**
	 * Interpret values as longs.
	 */
	public static final int LONG = SortField.LONG;
	
	/**
	 * Interpret values as shorts.
	 */
	public static final int SHORT = SortField.SHORT;
	
	/**
	 * Interpret values as strings (using ordinals, faster).
	 */
	public static final int STRING = SortField.STRING;
	
	/**
	 * Interpret values as strings (using compareTo, slower).
	 */
	public static final int STRING_COMPARETO = SortField.STRING_VAL;
	
	/**
	 * Sort by indexing order (first indexed resource is first, etc.).
	 */
	public static final SortEntry INDEX_ORDER = new SortEntry() {
		@Override
		SortField getSortField() {
			return SortField.FIELD_DOC;
		}
	};
	
	/**
	 * Sort by Lucene document score.
	 */
	public static final SortEntry RELEVANCE = new SortEntry() {
		@Override
		SortField getSortField() {
			return SortField.FIELD_SCORE;
		}
	};
	
	/**
	 * A SortSPecification Entry.
	 */
	public static class SortEntry {
		private SortField sortField;
		
		private SortEntry() {};
		
		/**
		 * Constructor.
		 * 
		 * @param property	The property.
		 * @param type		The property-values type.
		 * @param reverse	True sorts in reverse. False uses standard order.
		 */
		SortEntry(VirtualProperty property, int type, boolean reverse) {
			sortField = new SortField(GraphIndexer.SORT_PREFIX + property.getStringKey(), type, reverse);
		}
		
		/**
		 * Return a the Lucene SortField corresponding to this entry.
		 * 
		 * @return	the SortField 
		 */
		SortField getSortField() {
			return sortField;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == null) {
				return false;
			}
			if(!(obj instanceof SortEntry)) {
				return false;
			}
			return getSortField().equals(((SortEntry) obj).getSortField());
		}

		@Override
		public int hashCode() {
			return getSortField().hashCode();
		}
	}
	
	private ArrayList<SortEntry> sortPriority;

	/**
	 * Creates a new SortSpecification.
	 */
	public SortSpecification() {
		sortPriority = new ArrayList<SortEntry>();
	}
	
	/**
	 * Add a property to sort on.
	 * 
	 * Note: 
	 * The order of addition determines the search priority.
	 * The property to search on has to be indexed.
	 * The indexed value should not be tokenized.
	 * The property's value is interpreted according to specified type.
	 * 
	 * @param property	the property
	 * @param type	the type
	 */
	public void add(VirtualProperty property, int type) {
		add(property, type, false);
	}
	
	/**
	 * Add a property to sort on.
	 * 
	 * Note: 
	 * The order of addition determines the search priority.
	 * The property to search on has to be indexed.
	 * The indexed value should not be tokenized.
	 * The property's value is interpreted according to specified type.
	 * 
	 * @param property	the property
	 * @param type	the type
	 * @param reverse whether to sort in reverse.
	 */
	public void add(VirtualProperty property, int type, boolean reverse) {
		SortEntry sortEntry = new SortEntry(property, type, reverse);
		add(sortEntry);
	}
	
	/**
	 * Add a SortEntry.
	 * 
	 * Note: 
	 * The order of addition determines the search priority.
	 */
	public void add(SortEntry entry) {
		if(!sortPriority.contains(entry)) {
			sortPriority.add(entry);
		}
	}
	
	/**
	 * Remove the property with specified type.
	 * 
	 * @param property the property.
	 * @param type the type.
	 */
	public void remove(VirtualProperty property, int type) {
		remove(property, type, false);
	}
	
	/**
	 * Remove the property with specified type.
	 * 
	 * @param property the property.
	 * @param type the type.
	 * @param reverse whether the sort is specified in reverse.
	 */
	public void remove(VirtualProperty property, int type, boolean reverse) {
		SortEntry sortEntry = new SortEntry(property, type, reverse);
		remove(sortEntry);
	}
	
	/**
	 * Remove a SortEntry.
	 */
	public void remove(SortEntry entry) {
		ArrayList<SortEntry> old = new ArrayList<SortEntry>(sortPriority);
		sortPriority.clear();
		for(SortEntry e : old) {
			if(!e.equals(entry)) {
				sortPriority.add(e);
			}
		}
	}
	
	/**
	 * Clear the sort specification.
	 */
	public void clear() {
		sortPriority.clear();
	}
	
	/**
	 * Returns the number of added entries.
	 * 
	 * @return	the number of entries. 
	 */
	public int size() {
		return sortPriority.size();
	}
	
	/**
	 * Get all entries as SortFields.
	 * 
	 * @return	the SortFields 
	 */
	SortField[] getSortFields() {
		SortField[] array = new SortField[size()];
		for(int i = 0; i < size(); ++i) {
			array[i] = sortPriority.get(i).getSortField();
		}
		
		return array;
	}
}
