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
package org.apache.clerezza.utils.imageprocessing.metadataprocessing;

import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;


/**
 * This class represents a MetaData data structure of some medium (image, audio, etc.).
 * 
 * @author daniel
 *
 */
public class MetaData<E extends DataSet> implements Iterable<List<E>> {
	
	
	private MultivaluedMap<String, E> metadata;
	
	/**
	 * Creates a new MetaData object.
	 */
	public MetaData() {
		metadata = new MultivaluedMapImpl<String, E>();
	}
	
	/**
	 * Adds a data set to the data structure. 
	 * If a mapping for the given key already exists, 
	 * then the value gets appended to the value list.
	 * 
	 * @param dataSet  the data set to add.
	 */
	public void add(E dataSet) {
		metadata.add(dataSet.getKey(), dataSet);
	}
	
	/**
	 * Returns all data sets for a key.
	 * 
	 * More formally, if this map contains a mapping from a key 
	 * k to a value v such that (key==null ? k==null : key.equals(k)), 
	 * then v will be part of the returned list.
	 * 
	 * @param key  the key whose associated value is to be returned.
	 * @return  the list of data sets or null if none is found for given key.
	 */
	public List<E> get(String key) {
		return metadata.get(key);
	}

	/**
	 * Removes all meta data.
	 */
	public void clear() {
		metadata.clear();
	}

	/**
	 * Checks if there is a data set with the specified key. 
	 * 
	 * @param key  the key
	 * @return  true is there is a mapping for the specified key, false otherwise.
	 */
	public boolean containsKey(String key) {
		return metadata.containsKey(key);
	}

	/**
	 * Checks if any data set contains the specified value.
	 * 
	 * The values are considered the same if they are equal according to 
	 * the String equals method.
	 * 
	 * More formally, returns true if and only if this map contains 
	 * at least one mapping to a value v such that 
	 * (value==null ? v==null : value.equals(v)).
	 * 
	 * @param value  the value to check for.
	 * @return true if the value is saved in any data set, false otherwise.
	 */
	public boolean containsValue(String value) {
		return metadata.containsValue(value);
	}

	/**
	 * Checks if this meta data contains data sets.
	 * 
	 * @return  true if there are no key to data set mappings, false otherwise.
	 */
	public boolean isEmpty() {
		return metadata.isEmpty();
	}

	/**
	 * Returns the number of saved data sets.
	 * 
	 * @return  the number of saved data sets.
	 */
	public int size() {
		return metadata.size();
	}

	@Override
	public String toString() {
		return metadata.toString();
	}

	@Override
	public Iterator<List<E>> iterator() {
		return metadata.values().iterator();
	}
}
