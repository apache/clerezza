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
package org.apache.clerezza.triaxrs.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author reto
 */
public class AcceptHeader {



	@Override
	public String toString() {
		return entries.toString();
	}
	private static final Logger logger = LoggerFactory.getLogger(AcceptHeader.class);

	static class AcceptHeaderEntry implements Comparable<AcceptHeaderEntry> {

		private MediaTypeComparator mediaTypeComparator = new MediaTypeComparator();
		MediaType mediaType;
		int quality; //from 0 to 1000

		AcceptHeaderEntry(MediaType mediaType) {
			Map<String, String> parametersWithoutQ = new HashMap<String, String>();
			parametersWithoutQ.putAll(mediaType.getParameters());
			String qValue = parametersWithoutQ.remove("q");
			this.mediaType = new MediaType(mediaType.getType(),
					mediaType.getSubtype(), parametersWithoutQ);
			if (qValue == null) {
				quality = 1000;
			} else {
				quality = (int) (Float.parseFloat(qValue) * 1000);
			}
		}

		@Override
		public int compareTo(AcceptHeaderEntry o) {
			if (equals(o)) {
				return 0;
			}
			if (quality == o.quality) {
				return mediaTypeComparator.compare(mediaType, o.mediaType);
			}
			return (o.quality - quality);
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return mediaType + " with q=" + quality + ";";
		}
	}
	private SortedSet<AcceptHeaderEntry> entries = new TreeSet<AcceptHeaderEntry>();

	public AcceptHeader(List<String> entryStrings) {
		if ((entryStrings == null) || (entryStrings.size() == 0)) {
			entries.add(new AcceptHeaderEntry(MediaType.WILDCARD_TYPE));
		} else {
			for (String string : entryStrings) {
				try {
					entries.add(new AcceptHeaderEntry(MediaType.valueOf(string)));
				} catch (IllegalArgumentException ex) {
					logger.warn("The string \"" + string + "\" is not a valid mediatype", ex);
				}
			}
		}
	}

	/**
	 * 
	 * @return a sorted list of the Mediatypes
	 */
	public List<MediaType> getEntries() {
		List<MediaType> result = new ArrayList<MediaType>();
		for (AcceptHeaderEntry entry : entries) {
			result.add(entry.mediaType);
		}
		return result;
	}

	/**
	 * 
	 * @param type
	 * @return a value from 0 to 1000 to indicate the quality in which type is accepted
	 */
	public int getAcceptedQuality(MediaType type) {
		for (AcceptHeaderEntry acceptHeaderEntry : entries) {
			if (isSameOrSubtype(type, acceptHeaderEntry.mediaType)) {
				return acceptHeaderEntry.quality;
			}
		}
		
		Object[] reverseEntries = entries.toArray();
		for(int i = entries.size()-1; i >=0 ; i--){
			AcceptHeaderEntry entry = (AcceptHeaderEntry)reverseEntries[i];
			if (isSameOrSubtype(entry.mediaType, type)){
				return entry.quality;
			}
		}
		
		return 0;
	}

	/**
	 *
	 * @param type
	 * @return the media-types in the accept header that are would best accept
	 * type, i.e. all pattern with the highest same q-value accepting type are
	 * returned
	 */
	public Set<MediaType> getAcceptingMediaType(MediaType type) {
		Set<MediaType> result = new HashSet<MediaType>();
		double currentQValue = 0;
		for (AcceptHeaderEntry acceptHeaderEntry : entries) {
			if (acceptHeaderEntry.mediaType.isCompatible(type)) {
				if (acceptHeaderEntry.quality >= currentQValue) {
					currentQValue = acceptHeaderEntry.quality;
					result.add(acceptHeaderEntry.mediaType);
				} else {
					break;
				}
			}
		}
		return result;
	}

	/**
	 * 
	 * @param t1
	 * @param t2
	 * @return true if t1 is the same or a subtype ot t2 such as when t1 is
	 *         text/plain and t2 is text/*
	 */
	private boolean isSameOrSubtype(MediaType t1, MediaType t2) {
		String type1 = t1.getType();
		String subtype1 = t1.getSubtype();
		String type2 = t2.getType();
		String subtype2 = t2.getSubtype();

		if (type2.equals(MediaType.MEDIA_TYPE_WILDCARD) && subtype2.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
			return true;
		} else if (type1.equalsIgnoreCase(type2) && subtype2.equals(MediaType.MEDIA_TYPE_WILDCARD)) {
			return true;
		} else {
			return type1.equalsIgnoreCase(type2) && subtype1.equalsIgnoreCase(subtype2);
		}
	}
}
