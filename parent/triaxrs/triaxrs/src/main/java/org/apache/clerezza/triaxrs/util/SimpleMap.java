/*******************************************************************************
 *     Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *     
 *      http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 *******************************************************************************/
package org.apache.clerezza.triaxrs.util;

/**
 * Simplified definition of the Map with only three operations: get, put and
 * clear.
 * <p>
 * Pay attention that the method may behave slightly differently from the
 * methods define by the Map interface, therefore it's recommended you always
 * check the javadoc of the implementing class.
 */
public interface SimpleMap<K, V> {

	/**
	 * Returns the value to which this map maps the specified key. Returns
	 * <tt>null</tt> if the map contains no mapping for this key. A return value
	 * of <tt>null</tt> does not <i>necessarily</i> indicate that the map
	 * contains no mapping for the key; it's also possible that the map
	 * explicitly maps the key to <tt>null</tt>.
	 */
	V get(K key);

	/**
	 * Associates the specified value with the specified key in this map. If the
	 * map previously contained a mapping for this key, the old value is
	 * replaced by the specified value.
	 */
	V put(K key, V value);

	/**
	 * Removes all mappings from this map (optional operation).
	 */
	void clear();
}
