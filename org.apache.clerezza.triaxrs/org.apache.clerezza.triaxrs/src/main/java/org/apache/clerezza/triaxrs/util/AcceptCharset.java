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
import java.util.Collections;
import java.util.List;

import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * Represent HTTP Accept-Charset header.
 * <p>
 * This version of the API does not support construction.
 * 
 * @see <a href='http://tools.ietf.org/html/rfc2616#section-14.4'>RFC 2616
 *      14.4</a>
 */
public class AcceptCharset {

	public static final class ValuedCharset implements Comparable<ValuedCharset> {

		public final double qValue;
		public final String charset;

		public ValuedCharset(double qValue, String charset) {
			this.qValue = qValue;
			this.charset = charset;
		}

		@Override
		public int compareTo(ValuedCharset other) {
			return Double.compare(qValue, other.qValue);
		}

		public boolean isWildcard() {
			return charset == null;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ValuedCharset)) {
				return false;
			}

			ValuedCharset other = (ValuedCharset) obj;
			if (other.qValue != this.qValue) {
				return false;
			}

			if (charset == null) {
				if (other.charset != null) {
					return false;
				}
			} else {
				if (!charset.equals(other.charset)) {
					return false;
				}
			}

			return true;
		}

		@Override
		public int hashCode() {
			int result = 17;
			result = 31 * result + Double.valueOf(qValue).hashCode();
			result = 31 * result + charset.hashCode();
			return result;
		}

		@Override
		public String toString() {
			return charset + ";q=" + qValue;
		}
	}
	private static final HeaderDelegate<AcceptCharset> delegate =
			RuntimeDelegate.getInstance().createHeaderDelegate(AcceptCharset.class);
	private final String acceptCharsetHeader;
	private final boolean anyAllowed;
	private final List<String> acceptable;
	private final List<String> banned;
	private final List<AcceptCharset.ValuedCharset> valuedCharsets;

	public AcceptCharset(String acceptCharset,
			List<String> acceptableCharsets,
			List<String> bannedCharsets,
			boolean anyCharsetAllowed,
			List<AcceptCharset.ValuedCharset> valuedCharsets) {
		this.acceptCharsetHeader = acceptCharset;
		this.anyAllowed = anyCharsetAllowed;
		this.banned = Collections.unmodifiableList(bannedCharsets);
		boolean isISO8859Explicit = false;
		for (ValuedCharset vCharset : valuedCharsets) {
			if ("ISO-8859-1".equalsIgnoreCase(vCharset.charset)) { //$NON-NLS-1$
				isISO8859Explicit = true;
				break;
			}
		}
		if (!isISO8859Explicit && !anyAllowed) {
			ArrayList<String> acceptableCharsetsTemp = new ArrayList<String>(acceptableCharsets);
			acceptableCharsetsTemp.add(0, "ISO-8859-1"); //$NON-NLS-1$
			this.acceptable = Collections.unmodifiableList(acceptableCharsetsTemp);

			ArrayList<AcceptCharset.ValuedCharset> valuedCharsetsTemp =
					new ArrayList<ValuedCharset>(valuedCharsets);
			valuedCharsetsTemp.add(0, new AcceptCharset.ValuedCharset(1.0d, "ISO-8859-1")); //$NON-NLS-1$
			this.valuedCharsets = Collections.unmodifiableList(valuedCharsetsTemp);
		} else {
			this.acceptable = Collections.unmodifiableList(acceptableCharsets);
			this.valuedCharsets = Collections.unmodifiableList(valuedCharsets);
		}
	}

	/**
	 * Provide a list of character sets which are acceptable for the client. If
	 * any charset is acceptable with some non-zero priority (see
	 * {@link #isAnyCharsetAllowed()}), only character sets more preferable than
	 * wildcard are listed.
	 *
	 * @return unmodifiable list, never <code>null</code>; the list is sorted
	 *         starting with the most preferable charset
	 */
	public List<String> getAcceptableCharsets() {
		return acceptable;
	}

	/**
	 * Is any character set acceptable? Note that expressions are listed by
	 * {@link #getBannedCharsets()}. This means that the value contains wildcard
	 * (with non-zero priority) of the header is not present at all.
	 *
	 * @return <code>true</code> if any character set is acceptable
	 */
	public boolean isAnyCharsetAllowed() {
		return anyAllowed;
	}

	/**
	 * A list of non-acceptable (q-value 0) character sets, i.e. exception of
	 * {@link #isAnyCharsetAllowed()}.
	 *
	 * @return never <code>null</code>; always empty if wildcard is not included
	 */
	public List<String> getBannedCharsets() {
		return banned;
	}

	/**
	 * Creates a new instance of AcceptCharset by parsing the supplied string.
	 *
	 * @param value the Accept-Charset string
	 * @return the newly created AcceptCharset
	 * @throws IllegalArgumentException if the supplied string cannot be parsed
	 */
	public static AcceptCharset valueOf(String value) throws IllegalArgumentException {
		return delegate.fromString(value);
	}

	public String getAcceptCharsetHeader() {
		return acceptCharsetHeader;
	}

	public List<AcceptCharset.ValuedCharset> getValuedCharsets() {
		return valuedCharsets;
	}

	@Override
	public String toString() {
		return delegate.toString(this);
	}
}
