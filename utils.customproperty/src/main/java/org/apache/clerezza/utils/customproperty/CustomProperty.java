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
package org.apache.clerezza.utils.customproperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;

import org.apache.clerezza.utils.customproperty.ontology.CUSTOMPROPERTY;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.ontologies.RDF;

/**
 * 
 * @scr.service interface="org.apache.clerezza.utils.customproperty.CustomProperty"
 * @scr.component
 * @scr.reference name="cgProvider" cardinality="1..1" policy="static"
 *                interface=
 *                "org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider"
 * 
 * @author mkn
 */

public class CustomProperty {

	private ContentGraphProvider cgProvider;

	
	/**
	 * Adding a single customfield to customize any item of a dependency
	 * 
	 * @param dependency		type UriRef defining the dependency item of the customfield
	 * @param dependencyValue	String to specify the property within the dependency type
	 * @param label				label of the customfield
	 * @param property			UriRef defining the meaning of the customfield
	 * @param length			
	 * @param cardinality
	 * 
	 * @return true if field could be added
	 */
	public boolean addSingleCustomField(UriRef dependency,
		String dependencyValue, String label, UriRef property, int length,
		int cardinality) {
			return addSingleCustomField(dependency, dependencyValue, label, null,
					null, property, length, cardinality);
	}

	/**
	 * Adding a single customfield to customize any item of a dependency
	 *
	 * @param dependency		type UriRef defining the dependency item of the customfield
	 * @param dependencyValue	String to specify the property within the dependency type
	 * @param label				label of the customfield
	 * @param description		the description about the usage of this custom field
	 * @param config			configuration parameter for this customfield
	 * @param property			UriRef defining the meaning of the customfield
	 * @param length
	 * @param cardinality
	 *
	 * @return true if field could be added
	 */
	public boolean addSingleCustomField(UriRef dependency,
		String dependencyValue, String label, String description, String config, 
			UriRef property, int length, int cardinality) {
		LockableMGraph contentGraph = cgProvider.getContentGraph();

		NonLiteral customfield = addBasicCustomField(contentGraph,
			property, cardinality, label, description, config, getCustomPropertyCollection(
				dependency, dependencyValue));
		Lock lock = contentGraph.getLock().writeLock();
		lock.lock();
		try {
			if(customfield != null){
				contentGraph.add(new TripleImpl( customfield , CUSTOMPROPERTY.length,
					LiteralFactory.getInstance().createTypedLiteral(length)));
				return true;
			} else {
				return false;
			}
		} finally {
			lock.unlock();
		}
	}


	/**
	 * Adding a customfield to select one or more values within some predefined ones
	 * 
	 * @param dependency		type UriRef defining the dependency item of the customfield
	 * @param dependencyValue	String to specify the property within the dependency type
	 * @param label				label of the customfield
	 * @param property			UriRef defining the meaning of the customfield
	 * @param multiselect		defines if one or more values can be selected
	 * @param selectableValues	the values as a string seperated with a ","
	 * @param cardinality
	 * 
	 * @return true if field could be added
	 */
	public boolean addMultipleCustomField(UriRef dependency,
			String dependencyValue, String label, UriRef property,
			String multiselect, String selectableValues, int cardinality) {
		
		return addMultipleCustomField(dependency, dependencyValue, label, null, null, property,
				multiselect, selectableValues, cardinality);
	}

	/**
	 * Adding a customfield to select one or more values within some predefined ones
	 *
	 * @param dependency		type UriRef defining the dependency item of the customfield
	 * @param dependencyValue	String to specify the property within the dependency type
	 * @param label				label of the customfield
	 * @param description		the description about the usage of this custom field
	 * @param config			configuration parameter for this customfield
	 * @param property			UriRef defining the meaning of the customfield
	 * @param multiselect		defines if one or more values can be selected
	 * @param selectableValues	the values as a string seperated with a ","
	 * @param cardinality
	 *
	 * @return true if field could be added
	 */
	public boolean addMultipleCustomField(UriRef dependency,
			String dependencyValue, String label, String description, String config, UriRef property,
			String multiselect, String selectableValues, int cardinality) {
		LockableMGraph contentGraph = cgProvider.getContentGraph();

		NonLiteral customfield = addBasicCustomField(contentGraph, property,
			cardinality, label, description, config, getCustomPropertyCollection(dependency,
				dependencyValue));
		Lock lock = contentGraph.getLock().writeLock();
		lock.lock();
		try {
			if(customfield != null){
				Collection<Triple> tripleArray = new ArrayList<Triple>();
				String[] values = selectableValues.split(",");
				for (int i = 0; i < values.length; i++) {
					tripleArray
							.add(new TripleImpl(customfield, CUSTOMPROPERTY.value,
									LiteralFactory.getInstance().createTypedLiteral(
											values[i])));
				}
				tripleArray.add(new TripleImpl(customfield,
						CUSTOMPROPERTY.multiselectable, LiteralFactory.getInstance()
								.createTypedLiteral(multiselect)));
				contentGraph.addAll(tripleArray);
				return true;
			} else {
				return false;
			}
		} finally {
			lock.unlock();
		}
	}
	
	protected NonLiteral addBasicCustomField(LockableMGraph contentGraph,
			UriRef property, int cardinality, String label, String description,
			String config, NonLiteral propertyCollection) {
		Lock lock = contentGraph.getLock().writeLock();
		lock.lock();
		try {
			if(contentGraph.filter(null, CUSTOMPROPERTY.property, property).hasNext()) {
				return null;
			}
			Collection<Triple> tripleArray = new ArrayList<Triple>();
			NonLiteral customField = new BNode();
			contentGraph.add(new TripleImpl(customField,
					CUSTOMPROPERTY.property, property));
			tripleArray.add(new TripleImpl(propertyCollection,
					CUSTOMPROPERTY.customfield, customField));
			tripleArray.add(new TripleImpl(customField, RDF.type,
					CUSTOMPROPERTY.CustomField));
			tripleArray.add(new TripleImpl(customField, CUSTOMPROPERTY.cardinality,
					LiteralFactory.getInstance().createTypedLiteral(cardinality)));
			tripleArray.add(new TripleImpl(customField,
					CUSTOMPROPERTY.presentationlabel, new PlainLiteralImpl(label)));
			if(description != null && !description.isEmpty()) {
				tripleArray.add(new TripleImpl(customField,
					DCTERMS.description, new PlainLiteralImpl(description)));
			}
			if(config != null && !config.isEmpty()) {
				tripleArray.add(new TripleImpl(customField,
					CUSTOMPROPERTY.configuration, new PlainLiteralImpl(config)));
			}
			if(contentGraph.addAll(tripleArray)){
				return customField;
			} else {
				return null;
			}
		} finally {
			lock.unlock();
		}

		
	}

	/**
	 * Get back the Resource of the collection for a specific dependency
	 * 
	 * @param dependency		the type of items the collection is defining customproperties for
	 * @param dependencyValue	a specification string to select the right collection within all dependency collections
	 * 
	 * @return the resource of the found collection
	 */
	public NonLiteral getCustomPropertyCollection(UriRef dependency,
			String dependencyValue) {
		LockableMGraph contentGraph = cgProvider.getContentGraph();
		Lock lock = contentGraph.getLock().readLock();
		lock.lock();
		try {
			Iterator<Triple> collections = contentGraph.filter(null, RDF.type,
					CUSTOMPROPERTY.CustomFieldCollection);
			while (collections.hasNext()) {
				Iterator<Triple> collections2 = contentGraph
						.filter(collections.next().getSubject(),
								CUSTOMPROPERTY.dependency, dependency);
				while (collections2.hasNext()) {
					Iterator<Triple> collections3 = contentGraph.filter(
							collections2.next().getSubject(),
							CUSTOMPROPERTY.dependencyvalue, LiteralFactory
									.getInstance().createTypedLiteral(
											dependencyValue));
					if (collections3.hasNext()) {
						return collections3.next().getSubject();
					}
				}
			}
		} finally {
			lock.unlock();
		}
		lock = contentGraph.getLock().writeLock();
		lock.lock();
		try {
			Collection<Triple> tripleArray = new ArrayList<Triple>();
			NonLiteral cfc = new BNode();
			tripleArray.add(new TripleImpl(cfc, RDF.type,
					CUSTOMPROPERTY.CustomFieldCollection));
			tripleArray.add(new TripleImpl(cfc, CUSTOMPROPERTY.dependency,
					dependency));
			tripleArray.add(new TripleImpl(cfc, CUSTOMPROPERTY.dependencyvalue,
					LiteralFactory.getInstance()
							.createTypedLiteral(dependencyValue)));
			contentGraph.addAll(tripleArray);
			return getCustomPropertyCollection(dependency, dependencyValue);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * @param collection
	 * @return all customfields of the collection
	 */
	public ArrayList<NonLiteral>getCustomfieldsOfCollection(NonLiteral collection){
		ArrayList<NonLiteral> customfields = new ArrayList<NonLiteral>();
		LockableMGraph contentGraph = cgProvider.getContentGraph();
		Lock lock = contentGraph.getLock().readLock();
		lock.lock();
		try {
			Iterator <Triple> result= contentGraph.filter(collection, CUSTOMPROPERTY.customfield, null);
			while(result.hasNext()){
				customfields.add((NonLiteral) result.next().getObject());
			}
			return customfields;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * @param collection
	 * @return all properties of the collection
	 */
	public ArrayList<UriRef>getPropertiesOfCollection(NonLiteral collection){
		ArrayList<UriRef> customproperties = new ArrayList<UriRef>();
		LockableMGraph contentGraph = cgProvider.getContentGraph();
		Lock lock = contentGraph.getLock().readLock();
		lock.lock();
		try {
			Iterator <Triple> result= contentGraph.filter(collection, CUSTOMPROPERTY.customfield, null);
			while(result.hasNext()){
				Iterator <Triple> property= contentGraph.filter(
						(NonLiteral)result.next().getObject(), CUSTOMPROPERTY.property, null);
				if(property.hasNext()){
					customproperties.add((UriRef)property.next().getObject());
				}
			}
			return customproperties;
		} finally {
			lock.unlock();
		}
	}

	
	/**
	 * Delete a customfield according to its collection and its property
	 * 
	 * @param dependency
	 * @param dependencyValue
	 * @param property
	 * 
	 * @return	returns whether the customfield could be deleted or not
	 */
	public boolean deleteCustomField(UriRef dependency, String dependencyValue,
			UriRef property) {
		LockableMGraph contentGraph = cgProvider.getContentGraph();
		Collection<Triple> allCustomFieldTriples = new ArrayList<Triple>();
		Iterator<Triple> customfields = contentGraph.filter(
				getCustomPropertyCollection(dependency, dependencyValue),
				CUSTOMPROPERTY.customfield, null);
		boolean customFieldDelete = false;
		Lock lock = contentGraph.getLock().writeLock();
		lock.lock();
		try {
			while (customfields.hasNext()) {
				Iterator<Triple> customfields2 = contentGraph.filter(
						(NonLiteral) customfields.next().getObject(),
						CUSTOMPROPERTY.property, property);
				if (customfields2.hasNext()) {
					NonLiteral customField = customfields2.next().getSubject();
					Iterator<Triple> someCustomfieldTriples = contentGraph.filter(
							customField, null, null);
					while (someCustomfieldTriples.hasNext()) {
						allCustomFieldTriples.add(someCustomfieldTriples.next());
					}
					Iterator<Triple> otherCustomFieldTriples = contentGraph.filter(
							null, null, customField);
					while (otherCustomFieldTriples.hasNext()) {
						allCustomFieldTriples.add(otherCustomFieldTriples.next());
					}
					customFieldDelete =  true;
				}
			}
			contentGraph.removeAll(allCustomFieldTriples);
		} finally {
			lock.unlock();
		}		
		return customFieldDelete;

	}
	
	/**
	 * 
	 * @param customfield
	 * @return the property UriRef of the customfield
	 */
	public UriRef getCustomFieldProperty(NonLiteral customfield){
		MGraph contentGraph = cgProvider.getContentGraph();
		return (UriRef)contentGraph.filter(customfield, CUSTOMPROPERTY.property,
				null).next().getObject();
	}
	
	protected void bindCgProvider(ContentGraphProvider cgProvider) {
		this.cgProvider = cgProvider;
	}
	
	protected void unbindCgProvider(ContentGraphProvider cgProvider) {
		this.cgProvider = null;
	}

}
