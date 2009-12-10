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
package org.apache.clerezza.rdf.utils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.OWL;
import org.apache.clerezza.rdf.ontologies.RDF;

/**
 * An implementation of an <code>java.util.List</code> backed by an RDF
 * collection (rdf:List). The list allows modification that are reflected
 * to the underlying <code>TripleCollection</code>. It reads the data from the
 * <code>TripleCollection</code> when it is first needed, so changes to the
 * TripleCollection affecting the rdf:List may or may not have an effect on the
 * values returned by instances of this class. For that reason only one
 * instance of this class should be used for accessing an rdf:List of sublists
 * thereof when the lists are being modified, having multiple lists exclusively
 * for read operations (such as for immutable <code>TripleCollection</code>s) is
 * not problematic.
 *
 * @author rbn
 */
public class RdfList extends AbstractList<Resource> {

	private final static UriRef RDF_NIL =
			new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");
	/**
	 * a list of the linked rdf:List elements in order
	 */
	private List<NonLiteral> listList = new ArrayList<NonLiteral>();
	private List<Resource> valueList = new ArrayList<Resource>();
	private NonLiteral firstList;
	private TripleCollection tc;
	private boolean totallyExpanded = false;

	/**
	 * Get a list for the specified resource. If the resource does not have
	 * rdf:first and rdf:rest properties an empty list is created by
	 * specifying that the resource is owl:sameAs rdf:nil.
	 *
	 * If the list is modified using the created instance
	 * <code>listResource</code> will always be the first list.
	 *
	 * @param listResource
	 * @param tc
	 */
	public RdfList(NonLiteral listResource, TripleCollection tc) {
		firstList = listResource;
		this.tc = tc;
		if (!tc.filter(listResource, RDF.first, null).hasNext()) {
			tc.add(new TripleImpl(listResource, OWL.sameAs, RDF_NIL));
		}

	}

	private void expandTill(int pos) {
		if (totallyExpanded) {
			return;
		}
		NonLiteral currentList;
		if (listList.size() > 0) {
			currentList = listList.get(listList.size()-1);
		} else {
			currentList = firstList;
			if (tc.filter(currentList, OWL.sameAs, RDF_NIL).hasNext()) {
				return;
			}
			listList.add(currentList);
			valueList.add(getFirstEntry(currentList));
		}
		if (listList.size() >= pos) {
			return;
		}
		while (true) {				
			currentList = getRest(currentList);
			if (currentList.equals(RDF_NIL)) {
				totallyExpanded = true;
				break;
			}
			if (listList.size() == pos) {
				break;
			}
			valueList.add(getFirstEntry(currentList));
			listList.add(currentList);
		}
	}



	@Override
	public Resource get(int index) {
		expandTill(index + 1);
		return valueList.get(index);
	}

	@Override
	public int size() {
		expandTill(Integer.MAX_VALUE);		
		return valueList.size();
	}

	@Override
	public void add(int index, Resource element) {
		expandTill(index);
		if (index == 0) {
			//special casing to make sure the first list remains the same resource
			if (listList.size() == 0) {
				tc.remove(new TripleImpl(firstList, OWL.sameAs, RDF_NIL));
				tc.add(new TripleImpl(firstList, RDF.rest, RDF_NIL));
				tc.add(new TripleImpl(firstList, RDF.first, element));
				listList.add(firstList);
			} else {
				tc.remove(new TripleImpl(listList.get(0), RDF.first, valueList.get(0)));
				tc.add(new TripleImpl(listList.get(0), RDF.first, element));
				addInRdfList(1, valueList.get(0));
			}
		} else {
			addInRdfList(index, element);
		}
		valueList.add(index, element);
	}
	
	/**
	 *
	 * @param index is > 0
	 * @param element
	 */
	private void addInRdfList(int index, Resource element) {
		expandTill(index+1);
		NonLiteral newList = new BNode() {
		};
		tc.add(new TripleImpl(newList, RDF.first, element));
		if (index < listList.size()) {
			tc.add(new TripleImpl(newList, RDF.rest, listList.get(index)));
			tc.remove(new TripleImpl(listList.get(index - 1), RDF.rest, listList.get(index)));
		} else {
			tc.remove(new TripleImpl(listList.get(index - 1), RDF.rest, RDF_NIL));
			tc.add(new TripleImpl(newList, RDF.rest, RDF_NIL));

		}
		tc.add(new TripleImpl(listList.get(index - 1), RDF.rest, newList));
		listList.add(index, newList);
	}

	@Override
	public Resource remove(int index) {
		//keeping the first list resource
		tc.remove(new TripleImpl(listList.get(index), RDF.first, valueList.get(index)));
		if (index == (listList.size() - 1)) {
			tc.remove(new TripleImpl(listList.get(index), RDF.rest, RDF_NIL));	
			if (index > 0) {
				tc.remove(new TripleImpl(listList.get(index - 1), RDF.rest, listList.get(index)));
				tc.add(new TripleImpl(listList.get(index - 1), RDF.rest, RDF_NIL));
			} else {
				tc.add(new TripleImpl(listList.get(index), OWL.sameAs, RDF_NIL));
			}
			listList.remove(index);
		} else {
			tc.add(new TripleImpl(listList.get(index), RDF.first, valueList.get(index+1)));
			tc.remove(new TripleImpl(listList.get(index), RDF.rest, listList.get(index + 1)));
			tc.remove(new TripleImpl(listList.get(index + 1), RDF.first, valueList.get(index + 1)));
			if (index == (listList.size() - 2)) {
				tc.remove(new TripleImpl(listList.get(index + 1), RDF.rest, RDF_NIL));
				tc.add(new TripleImpl(listList.get(index), RDF.rest, RDF_NIL));
			} else {
				tc.remove(new TripleImpl(listList.get(index + 1), RDF.rest, listList.get(index + 2)));
				tc.add(new TripleImpl(listList.get(index), RDF.rest, listList.get(index + 2)));
			}
			listList.remove(index+1);
		}
		return valueList.remove(index);
	}

	private NonLiteral getRest(NonLiteral list) {
		return (NonLiteral) tc.filter(list, RDF.rest, null).next().getObject();
	}

	private Resource getFirstEntry(NonLiteral listResource) {
		return tc.filter(listResource, RDF.first, null).next().getObject();
	}
}
