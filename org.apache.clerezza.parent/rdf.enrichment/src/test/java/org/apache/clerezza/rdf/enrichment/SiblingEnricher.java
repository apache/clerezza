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
package org.apache.clerezza.rdf.enrichment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;

/**
 * add a siblingProperty to things with a common parent. every resource with a
 * parent is a sibling of itself.
 *
 * @author reto
 */
public class SiblingEnricher extends Enricher {

	//ontology part
	/**
	 * points to the parent of the subject
	 */
	static UriRef parentProperty = new UriRef("http://example.org/ontology#parent");
	static UriRef siblingProperty = new UriRef("http://example.org/ontology#sibling");

	/*
	 * we might be able to tell more about things tht have a parent
	 */
	@Override
	public ResourceFilter getSubjectFilter() {
		return getFilterForSubjectsWithProperty(parentProperty);
	}

	/*
	 * the only thing we can say is if two things are siblings
	 */
	@Override
	public ResourceFilter getPredicateFilter() {
		return getExtensionalFilter(siblingProperty);
	}
	/*
	 * the object has a parent too
	 */

	@Override
	public ResourceFilter getObjectFilter() {
		return getFilterForSubjectsWithProperty(parentProperty);
	}

	/*
	 * the predicate is null or siblingProperty
	 */
	@Override
	public Iterator<Triple> filter(final NonLiteral subject,
			final UriRef predicate, final Resource object,
			final TripleCollection base) {
		if ((subject != null) && (object != null)) {
			if (areSiblings(subject, object, base)) {
				return Collections.singleton(
						(Triple) new TripleImpl(subject, siblingProperty, object)).iterator();
			} else {
				List<Triple> emptyList = Collections.emptyList();
				return emptyList.iterator();
			}
		} else {
			if (subject != null) {
				final Iterator<NonLiteral> siblings = 
						getSiblings(subject, base).iterator();
				return new Iterator<Triple>() {

					@Override
					public boolean hasNext() {
						return siblings.hasNext();
					}

					@Override
					public Triple next() {
						return new TripleImpl(subject, siblingProperty, siblings.next());
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException(
								"cannot delete infered triple");
					}
				};
			}
			if (object != null) {
				final Iterator<NonLiteral> siblings = 
						getSiblings((NonLiteral) object,base).iterator();
				return new Iterator<Triple>() {

					@Override
					public boolean hasNext() {
						return siblings.hasNext();
					}

					@Override
					public Triple next() {
						return new TripleImpl(siblings.next(), siblingProperty, object);
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException(
								"cannot delete infered triple");
					}
				};
			}
			//both subjecta nd object are null
			//TODO
			throw new UnsupportedOperationException("not yet pmpliemented");
		}
	}

	private boolean areSiblings(NonLiteral subject, Resource object, TripleCollection base) {
		return getSiblings(subject, base).contains(object);
	}

	private Set<NonLiteral> getSiblings(NonLiteral resource, TripleCollection base) {
		Set<Resource> parents = new HashSet<Resource>();
		{
			Iterator<Triple> parentTriples = base.filter(resource, parentProperty, null);
			while (parentTriples.hasNext()) {
				parents.add(parentTriples.next().getObject());
			}
		}
		Set<NonLiteral> resultSet = new HashSet<NonLiteral>();
		for (Resource parent : parents) {
			Iterator<Triple> childTriples = base.filter(null, parentProperty, parent);
			while (childTriples.hasNext()) {
				resultSet.add(childTriples.next().getSubject());
			}
		}
		return resultSet;
	}

	@Override
	public int providedTriplesCount(TripleCollection base) {
		//raw guess
		return (int) (base.size() * 0.01);
	}
}
