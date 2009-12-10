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
package org.apache.clerezza.rdf.core.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;

/**
 * For now this is a minimalistic implementation, without any indexes or other
 * optimizations.
 *
 * This class is not public, implementations should use {@link SimpleGraph} or
 * {@link SimpleMGraph}.
 *
 * @author reto
 */
class SimpleTripleCollection extends AbstractTripleCollection {

	Set<Triple> triples;

	/**
	 * Creates an empty SimpleTripleCollection
	 */
	public SimpleTripleCollection() {
		triples = new HashSet<Triple>();
	}

	/**
	 * Creates a SimpleTripleCollection using the passed iterator, the iterator 
	 * is consumed before the constructor returns
	 * 
	 * @param iterator
	 */
	public SimpleTripleCollection(Iterator<Triple> iterator) {
		triples = new HashSet<Triple>();
		while (iterator.hasNext()) {
			Triple triple = iterator.next();
			triples.add(triple);
		}
	}

	/**
	 * Creates a SimpleTripleCollection for the specified set of triples, 
	 * subsequent modification of baseSet do affect the created instance.
	 * 
	 * @param baseSet
	 */
	public SimpleTripleCollection(Set<Triple> baseSet) {
		this.triples = baseSet;
	}

	@Override
	public int size() {
		return triples.size();
	}

	@Override
	public Iterator<Triple> performFilter(final NonLiteral subject, final UriRef predicate, final Resource object) {
		Iterator<Triple> baseIter = triples.iterator();
		final List<Triple> tripleList = new ArrayList<Triple>();
		while (baseIter.hasNext()) {
			Triple triple = baseIter.next();
			if ((subject != null) &&
				(!triple.getSubject().equals(subject))) {
					continue;
				}
				if ((predicate != null) &&
						(!triple.getPredicate().equals(predicate))) {
					continue;
				}
				if ((object != null) &&
						(!triple.getObject().equals(object))) {
					continue;
				}
			tripleList.add(triple);			
		}
		
		final Iterator<Triple> listIter = tripleList.iterator();
		
		return new Iterator<Triple>() {

			private Triple currentNext;
			
			@Override
			public boolean hasNext() {
				return listIter.hasNext();
			}

			@Override
			public Triple next() {
				currentNext = listIter.next();
				return currentNext;
			}

			@Override
			public void remove() {
				listIter.remove();
				triples.remove(currentNext);
			}			
		};
	}


	@Override
	public boolean performAdd(Triple e) {
		return triples.add(e);
	}
}
