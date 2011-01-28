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
package org.apache.clerezza.triaxrs.providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.clerezza.triaxrs.util.MediaTypeComparator;
import org.apache.clerezza.triaxrs.util.MultivaluedMapImpl;

class SelectableProviders<T> {

	private Set<T> providers;
	private MultivaluedMap<MediaType, T> type2Provider;
	private Map<BaseMediaType, SortedSet<MediaType>> availableProviderTypes;
	private ProviderCriteria<T> tester;

	SelectableProviders(Set<T> producers, ProviderCriteria<T> tester) {
		this.providers = producers;
		this.tester = tester;
		createMaps();
	}

	T selectFor(Class<?> c, Type t,
			Annotation[] as, MediaType mediaType) {
		if (mediaType == null) {
			mediaType = MediaType.WILDCARD_TYPE;
		}
		BaseMediaType baseType = new BaseMediaType(mediaType.getType(),
				mediaType.getSubtype());

		while (true) {
			SortedSet<MediaType> typesForBaseType = availableProviderTypes.get(
					baseType);
			if (typesForBaseType != null) {
				Iterator<MediaType> available = typesForBaseType.iterator();
				while (available.hasNext()) {
					MediaType availableType = available.next();
					if (availableType.isCompatible(mediaType)) {
						List<T> readers = type2Provider.get(
								availableType);
						for (T messageBodyReader : readers) {
							if (tester.isAcceptable(messageBodyReader, c, t, as,
									availableType)) {
								return messageBodyReader;
							}
						}
					}
				}
			}
			if (baseType.equals(BaseMediaType.WILDCARD_TYPE)) {
				return null;
			}
			generalize(baseType);
		}

	}

	private void generalize(BaseMediaType baseType) {
		if (!baseType.getSubtype().equals("*")) {
			baseType.setSubtype("*");
			return;
		}
		baseType.setType("*");
	}

	private void createMaps() {
		type2Provider = new MultivaluedMapImpl<MediaType, T>();
		availableProviderTypes =
				new HashMap<BaseMediaType, SortedSet<MediaType>>();//TreeSet<MediaType>();
		for (T bodyReader : providers) {
			MediaType[] types = getConsumedType(bodyReader);
			for (MediaType type : types) {
				type2Provider.add(type, bodyReader);
				BaseMediaType baseType = new BaseMediaType(type.getType(),
						type.getSubtype());
				while (true) {
					SortedSet<MediaType> typeForBaseType = availableProviderTypes
							.get(baseType);
					if (typeForBaseType == null) {
						typeForBaseType = new TreeSet<MediaType>(
								new MediaTypeComparator());
						try {
							availableProviderTypes.put((BaseMediaType)baseType.clone(), typeForBaseType);
						} catch (CloneNotSupportedException ex) {
							throw new RuntimeException(ex);
						}
					}
					typeForBaseType.add(type);
					if (baseType.equals(BaseMediaType.WILDCARD_TYPE)) {
						break;
					}
					generalize(baseType);
				}
			}

		}
	}

	@Deprecated
	Set<T> getAll() {
		return providers;
	}

	private MediaType[] getConsumedType(T provider) {
		MediaType[] result;
		String[] mediaTypeStrings  = tester.getMediaTypeAnnotationValues(provider);
		if (mediaTypeStrings == null) {
			result = new MediaType[1];
			result[0] = MediaType.WILDCARD_TYPE;
		} else {
			result = new MediaType[mediaTypeStrings.length];
			for (int i = 0; i < mediaTypeStrings.length; i++) {
				result[i] = MediaType.valueOf(mediaTypeStrings[i]);

			}
		}
		return result;
	}
}