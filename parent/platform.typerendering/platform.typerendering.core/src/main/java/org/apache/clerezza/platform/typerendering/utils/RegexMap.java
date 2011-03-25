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

import java.util.*;
import java.util.regex.Pattern;

/**
 *
 * A mapping from regular expressions to values, with support to get all
 * entries matching a given term.
 *
 * This could be implemented efficiently, the current implementations stupidly
 * iterated over all entries.
 *
 * A null regex or term is treated as an empty string.
 *
 * @author reto
 */
public class RegexMap<T> {

	private class Tuple implements Comparable<Tuple> {

		public Tuple(String regex, T entry) {
			this.pattern = Pattern.compile(regex);
			this.entry = entry;
			for (char ch : regex.toCharArray()) {
				if ((ch >= 'a') && (ch <= 'Z')) {regexStrength++;} else
				if ((ch >= '0') && (ch <= '9')) regexStrength++;
			}

		}

		Pattern pattern;
		T entry;
		int regexStrength = 0;

		@Override
		public int compareTo(Tuple o) {
			return regexStrength - o.regexStrength;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final Tuple other = (Tuple) obj;
			if (this.pattern.pattern().equals(other.pattern.pattern())) {
				return false;
			}
			if (!this.entry.equals(other.entry)) {
				return false;
			}
			return true;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 59 * hash + this.pattern.pattern().hashCode();
			hash = 59 * hash + this.entry.hashCode();
			return hash;
		}


	}

	private List<Tuple> tuples = new ArrayList<Tuple>();

	/**
	 * Adds an entry for the specified regex.
	 *
	 * @param regex
	 * @param entry
	 */
	public void addEntry(String pRegex, T entry) {
		final String regex = pRegex != null ? pRegex : "";
		tuples.add(new Tuple(regex, entry));
	}

	/**
	 * returns the first entry associated with the specified Regex
	 * @return an entry or null
	 */
	public T getFirstExactMatch(String pRegex) {
		final String regex = pRegex != null ? pRegex : "";
		for (Tuple t : tuples) {
			if (t.pattern.pattern().equals(regex)) {
				return t.entry;
			}
		}
		return null;
	}


	/**
	 * 
	 * @param term the term that must match against the regex of the entry
	 * @return an iterator of matching entries, sorted by length of match
	 */
	public Iterator<T> getMatching(String pTerm) {
		final String term = pTerm != null ? pTerm : "";
		return new Iterator<T>() {

			Iterator<Tuple> tupleIter = tuples.iterator();

			T prepareNext() {
				while (tupleIter.hasNext()) {
					Tuple current = tupleIter.next();
					if (current.pattern.matcher(term).matches()) {
						return current.entry;
					}
				}
				return null;
			}

			T next = prepareNext();

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public T next() {
				if (next == null) {
					throw new NoSuchElementException();
				}
				T current = next;
				next = prepareNext();
				return current;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Not supported.");
			}
		};
	}

	public Set<Map.Entry<String,T>> entrySet() {
		Map<String,T> map = new HashMap<String, T>();
		for (Tuple tuple : tuples) {
			map.put(tuple.pattern.pattern(), tuple.entry);
		}
		return map.entrySet();
	}
}
