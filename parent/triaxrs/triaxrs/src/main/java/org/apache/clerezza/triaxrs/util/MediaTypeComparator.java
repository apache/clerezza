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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

/**
 * Sorts media types in accordance with an accept-header, falling back to
 * a built-in priority list and to literal sorting to guarantee constistency.
 *
 * Also provides static Utility methods for Media Type comparison.
 *
 * @author reto
 */
public class MediaTypeComparator implements Comparator<MediaType> {

	/** if the media-types have equal priority in the accept header
	 * they are sorted according to the following q-values
	 */
	private static final Map<MediaType, Float> fallBackQ = new HashMap<MediaType, Float>();

	static {
		fallBackQ.put(MediaType.APPLICATION_XHTML_XML_TYPE, new Float(1.0f));
		fallBackQ.put(MediaType.valueOf("text/html"), new Float(0.9f));
		fallBackQ.put(MediaType.valueOf("application/rdf+xml"), new Float(0.8f));
	}

	private static int fallBackCompare(MediaType o1, MediaType o2) {
		float q1 = getFallBackQ(o1);
		float q2 = getFallBackQ(o2);
		if (q1 == q2) {
			return 0;
		}
		if (q1 > q2) {
			return -1;
		} else {
			return 1;
		}
	}
	private AcceptHeader acceptHeader;

	public MediaTypeComparator() {
	}

	public MediaTypeComparator(AcceptHeader acceptHeader) {
		this.acceptHeader = acceptHeader;
	}

	/**
	 * this is not consistent with equals
	 * @param o1
	 * @param o2
	 * @return
	 */
	public static int inconsistentCompare(MediaType o1, MediaType o2) {
		if ((o1 == null) && (o2 == null)) {
			return 0;
		}
		if (o1 == null) {
			return 1;
		}
		if (o2 == null) {
			return -1;
		}
		int wilchCharComparison = compareByWildCardCount(o1, o2);
		if (wilchCharComparison == 0) {
			float q1 = getQ(o1);
			float q2 = getQ(o2);
			if (q1 == q2) {
				return fallBackCompare(o1, o2);
			}
			if (q1 > q2) {
				return -1;
			} else {
				return 1;
			}
		} else {
			return wilchCharComparison;
		}
	}

	@Override
	public int compare(MediaType o1, MediaType o2) {
		if (o1.equals(o2)) {
			return 0;
		}
		if (acceptHeader != null) {
			if (acceptHeader.getAcceptedQuality(o1) > acceptHeader.getAcceptedQuality(o2)) {
				return -1;
			}
			if (acceptHeader.getAcceptedQuality(o1) < acceptHeader.getAcceptedQuality(o2)) {
				return 1;
			}
		}
		int inconsistentCompare = inconsistentCompare(o1, o2);
		if (inconsistentCompare == 0) {
			return o1.toString().compareTo(o2.toString());
		} else {
			return inconsistentCompare;
		}

	}

	/**
	 * 
	 * @param o1
	 * @param o2
	 * @return -1 if o1 contains less wildcards
	 */
	public static int compareByWildCardCount(MediaType o1, MediaType o2) {
		int w1 = countWildChars(o1);
		int w2 = countWildChars(o2);
		if (w1 == w2) {
			return 0;
		}
		if (w1 < w2) {
			return -1;
		} else {
			return 1;
		}
	}

	public static int countWildChars(MediaType m) {
		if (m.getType().equals("*")) {
			return 2;
		}
		if (m.getSubtype().equals("*")) {
			return 1;
		}
		return 0;
	}

	private static float getQ(MediaType m) {
		String qString = m.getParameters().get("q");
		if (qString == null) {
			return 1;
		} else {
			return Float.parseFloat(qString);
		}
	}

	private static float getFallBackQ(MediaType m) {
		if (fallBackQ.containsKey(m)) {
			return fallBackQ.get(m);
		} else {
			return 0f;
		}
	}
}
