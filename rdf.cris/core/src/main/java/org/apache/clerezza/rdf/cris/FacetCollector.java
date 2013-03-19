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

package org.apache.clerezza.rdf.cris;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This class is a container that defines facets that should be collected and 
 * stores them. It can be used for faceted search in CRIS.
 *
 * @author daniel
 */
public abstract class FacetCollector<T> {

    /**
     * Add a property over which facets are collected.
     * 
     * Example: FOAF:firstName --> Resulting facets may be "John", "Adam", etc.
     * 
     * @param property    A virtual property over which facets are collected. 
     */
    public abstract void addFacetProperty(VirtualProperty property);

    
    /**
     * Returns the properties for which facets are collected..
     * 
     * @return The properties.
     */
    public abstract Collection<VirtualProperty> getProperties();
    
    /**
     * Returns all facets for the given property.
     * 
     * @param property    the property.
     * @return    A set with facets and facet values.
     */
    public abstract Set<Map.Entry<String, T>> getFacets(VirtualProperty property);
    
    /**
     * Returns a facet value.
     * 
     * @param property The property.
     * @param facet    The facet.
     * @return The facet value.
     */
    public abstract T getFacetValue(VirtualProperty property, String facet);
    
    /**
     * Expert: This method returns the underlying data structure as a nested map.
     * It is intended for fast access to the underlying data, not for users.
     * 
     * @return the facetMap
     */
    abstract Map<VirtualProperty, Map<String, T>> getFacetMap();
    
    /**
     * This method adds a new data to the facet map.
     * 
     * @param property    The property.
     * @param value    The value of the property as returned by Lucene.
     */
    abstract void addFacetValue(VirtualProperty property, String value);
    
    /**
     * Expert: This method performs post processing on the FacetCollector. 
     * Users should not call it. It is intended for avoiding time consuming tasks 
     * until changes are written to the collector.
     */
    abstract void postProcess();
}
