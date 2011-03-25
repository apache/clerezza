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

package org.apache.clerezza.platform.typerendering.utils;

import org.apache.clerezza.platform.typerendering.TypeRenderlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.print.attribute.standard.Media;
import javax.ws.rs.core.MediaType;

/**
 *
 * A mapping from regular media-types to values.
 *
 * A media type, or media-type pattern can be associated to a value. The
 * getMatching method will return entries of which the MediaType-Pattern
 * matches the given media type.
 *
 * @author reto
 */
public class MediaTypeMap<T> {

	private final Map<MediaType, Set<T>> exactTypeEntries = new HashMap<MediaType, Set<T>>();
	private final Map<String, Set<T>> primaryTypeEntries = new HashMap<String, Set<T>>();
	private final Set<T> wildCardEntries = new HashSet<T>();


	public void addEntry(final MediaType mediaType, final T entry) {
		if (mediaType.isWildcardType()) {
			wildCardEntries.add(entry);
			return;
		}
		if (mediaType.isWildcardSubtype()) {
			String primaryType = mediaType.getType();
			Set<T> entries = primaryTypeEntries.get(primaryType);
			if (entries == null) {
				entries = new HashSet<T>();
				primaryTypeEntries.put(primaryType, entries);
			}
			entries.add(entry);
			return;
		}
		Set<T> entries = exactTypeEntries.get(mediaType);
		if (entries == null) {
			entries = new HashSet<T>();
			exactTypeEntries.put(mediaType, entries);
		}
		entries.add(entry);
	}


	/**
	 * Returns entries matching the specified media-type.
	 *
	 * The current implementation is efficient for concrete media-types while
	 * wildcards require iterating throw parts of the entries
	 *
	 * @param mediaType
	 * @return an iterator of available entries, the one with the most concrete key media type first
	 */
	public Iterator<T> getMatching(final MediaType mediaType) {
		List<T> resultList = new ArrayList<T>();
		if (mediaType.isWildcardType()) {
			for (Set<T> entries: exactTypeEntries.values()) {
				resultList.addAll(entries);
			}
			for (Set<T> entries: primaryTypeEntries.values()) {
				resultList.addAll(entries);
			}
		} else {
			final String primaryType = mediaType.getType();
			if (!mediaType.isWildcardSubtype()) {
				//exact media types
				if (exactTypeEntries.containsKey(mediaType)) {
					resultList.addAll(exactTypeEntries.get(mediaType));
				}
			} else {
				//primary  type and wildcard subtype
				for (Map.Entry<MediaType, Set<T>> mapEntry: exactTypeEntries.entrySet()) {
					if (mapEntry.getKey().getType().equals(primaryType)) {
						resultList.addAll(mapEntry.getValue());
					}
				}
			}
			if (primaryTypeEntries.containsKey(primaryType)) {
				resultList.addAll(primaryTypeEntries.get(primaryType));
			}
		}
		resultList.addAll(wildCardEntries);
		return resultList.iterator();
	}

	public boolean remove(T toBeRemoved) {
		if (removeFromSetMap(exactTypeEntries, toBeRemoved)) {
			return true;
		}
		if (removeFromSetMap(primaryTypeEntries, toBeRemoved)) {
			return true;
		}
		return wildCardEntries.remove(toBeRemoved);
	}

	private <U> boolean removeFromSetMap(Map<U, Set<T>> map, T toBeRemoved) {
		Iterator<Map.Entry<U, Set<T>>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<?, Set<T>> entry = iter.next();
			Set<T> values = entry.getValue();
			if (values.remove(toBeRemoved)) {
				if (values.isEmpty()) {
					iter.remove();
				}
				return true;
			}
		}
		return false;
	}

}
