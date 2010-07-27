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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;

/**
 * use cases:
 * - resources with a common eg:parent should be eg:sibling
 * - for every foaf:Person there shall be a foaf:PersonalProfileDocument
 * - every non literal resource shall be subject of rdf:type rdfs:Resource statemet
 * - every eg:City shall have a eg:currentWeather property pointing to a bnode with temperature and humidity
 *
 * features:
 * - pre-filter (filter filter): only filter matching resource-matcher are forwarded
 * - on invocation with matching resource the Enricher knows which criteria matched
 *
 * @author reto
 */
/*
 * scala would allow to make reasonable use of generich, here having
 * ResourceFilter<T extends Resource> makes implementation harder as e.g. a
 * ResourceFilter<NonLiteral> could not be returned by getObjectFilter
 *
 * locks: There's currently no recommeended mechanism to prevent Enricher
 * to make the enrichted graph appear stable while in a lock
 */
public abstract class Enricher {


	/**
	 * Filters resources of a TripleCollcetion
	 */
	public static abstract class ResourceFilter {

		/**
		 *
		 * @param resource the resource to be evaluated
		 * @param tc the TripleCollcetion within which the resource is evaluated
		 * @return true if resource in tc passes the filter, false otherwise
		 */
		public abstract boolean accept(Resource resource, TripleCollection tc);

		/**
		 * @param tc the TripleCollcetion
		 * @return all the acceptable resources within tc
		 */
		public Set<Resource> getAcceptable(TripleCollection tc) {
			Set<Resource> result = new HashSet<Resource>();
			for (Triple triple : tc) {
				{
					NonLiteral subject = triple.getSubject();
					if (accept(subject, tc)) {
						result.add(subject);
					}
				}
				{
					Resource object = triple.getObject();
					if (accept(object, tc)) {
						result.add(object);
					}
				}
			}
			return result;
		}
	}

	/**
	 * returns the additional triples to a specified base graph. This method is
	 * only invoked with resources matching the respective Filter.
	 *
	 * @param subject
	 * @param predicate
	 * @param object
	 * @param base
	 * @return
	 */
	public abstract Iterator<Triple> filter(NonLiteral subject, UriRef predicate,
			Resource object, TripleCollection base);

	/**
	 * returns the number of enrichment triples for <code>base</code>
	 *
	 * @param base the triplecollection for which enrichment triples are generated
	 * @return the number of triples
	 */
	public abstract int providedTriplesCount(TripleCollection base);


	//IDEA add way for impl to specify fields that must not be null in filter
	//queries (in which case null is replaced with all matching resources)
	//and a way to specify that the impl ignores a specfied value in which case
	//a filter is applied manually on that value.
	//ISSUE: the query <tt>null, null, "13,4"</tt> might cause computation of a huge lots
	//of values
	/**
	 * The filter method of this enricher shall only be invoked with subjects
	 * matching the Filter returned by this method
	 *
	 * @return
	 */
	public ResourceFilter getSubjectFilter() {
		return acceptAll;
	}

	/**
	 * The filter method of this enricher shall only be invoked with predicates
	 * matching the Filter returned by this method
	 *
	 * @return
	 */
	public ResourceFilter getPredicateFilter() {
		return acceptAll;
	}

	/**
	 * The filter method of this enricher shall only be invoked with objects
	 * matching the Filter returned by this method
	 *
	 * @return
	 */
	public ResourceFilter getObjectFilter() {
		return acceptAll;
	}

	//utility methods for implementations

	/**
	 * A filter accepting all resources
	 */
	public ResourceFilter acceptAll = new ResourceFilter() {

		@Override
		public boolean accept(Resource resource, TripleCollection tc) {
			return true;
		}
	};

	/**
	 *
	 * @param predicate
	 * @return a filter accepting all resources that are the subject of a
	 * statement with the specified predicate
	 */
	protected static ResourceFilter getFilterForSubjectsWithProperty(final UriRef predicate) {
		return new ResourceFilter() {

			@Override
			public boolean accept(Resource resource, TripleCollection tc) {
				if (resource instanceof NonLiteral) {
					return tc.filter((NonLiteral)resource, predicate, null).hasNext();
				} else {
					return false;
				}
			}
		};
	}

	/**
	 * @return a filter accepting all resources that are the subject of a
	 * statement with the specified predicate and object
	 */
	protected static ResourceFilter getFilterForSubjectsWith(final UriRef predicate,
			final Resource object) {
		return new ResourceFilter() {

			@Override
			public boolean accept(Resource resource, TripleCollection tc) {
				if (resource instanceof NonLiteral) {
					return tc.filter((NonLiteral)resource, predicate, object).hasNext();
				} else {
					return false;
				}
			}
		};
	}

	/** creates a resource filters that matches only the specified resource
	 *
	 * @param resources
	 * @return
	 */
	protected static ResourceFilter getExtensionalFilter(final Resource... resources) {

		return new ResourceFilter() {
			Collection<Resource> acceptableResources = new HashSet<Resource>(Arrays.asList(resources));
			@Override
			public boolean accept(Resource resource, TripleCollection tc) {
				return acceptableResources.contains(resource);
			}
		};
	}

	/**
	 *
	 * @param dataType
	 * @return a filter accepting only typed literals of dataType
	 */
	protected static ResourceFilter getDataTypeFilter(final UriRef dataType) {
		return new ResourceFilter() {

			@Override
			public boolean accept(Resource resource, TripleCollection tc) {
				if (resource instanceof TypedLiteral) {
					return ((TypedLiteral)resource).getDataType().equals(dataType);
				} else {
					return false;
				}
			}
		};
	}



	private class LocalBNode extends BNode {
		boolean isFrom(Enricher enricher) {
			return (enricher == Enricher.this);
		}
	}

	/**
	 *
	 * @return a Bnode that will be accepted by localBNodeFilter
	 */
	protected BNode createLocalBNode() {
		return new LocalBNode();
	}
	private ResourceFilter localBNodeFilter = new ResourceFilter() {

		@Override
		public boolean accept(Resource resource, TripleCollection tc) {
			return (resource instanceof LocalBNode)
					&& ((LocalBNode) resource).isFrom(Enricher.this);
		}
	};

	/**
	 *
	 * @return localBNodeFilter a filter matching exclusively BNodes created by
	 * the createLocalBNode method of the same instance
	 */
	protected ResourceFilter getLocalBNodeFilter() {
		return localBNodeFilter;
	}


	/** utility method to filter an iterator
	 *
	 * @return an iterator with the triples baseIter matching the specified 
	 * subject, predicate and object where this are not null
	 */
	protected Iterator<Triple> filterIterator(NonLiteral subject, UriRef predicate,
			Resource object, final Iterator<Triple> baseIter) {
		//TODO filter on the fly without list
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
		return listIter;

	}

}
