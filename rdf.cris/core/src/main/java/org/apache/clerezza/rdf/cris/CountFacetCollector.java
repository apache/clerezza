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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A FacetCollector that counts members of a facet.
 *
 * @author daniel
 */
public class CountFacetCollector extends FacetCollector<Integer> {
    final Map<VirtualProperty, Map<String, Integer>> facetMap;

    /**
     * Creates a new CountFacetCollector that collects facet data over the 
     * supplied Properties.
     * 
     * @param properties VirtualProperties over which facet data is collected. 
     */
    public CountFacetCollector(Collection<VirtualProperty> properties) {
        this();
        for(VirtualProperty property : properties) {
            facetMap.put(property, new HashMap<String, Integer>());
        }
    }
    
    /**
     * Default Constructor.
     */
    public CountFacetCollector() {
        facetMap = new HashMap<VirtualProperty, Map<String, Integer>>();
    }
    
    @Override
    public void addFacetProperty(VirtualProperty property) {
        facetMap.put(property, new HashMap<String, Integer>());
    }
    
    @Override
    public Collection<VirtualProperty> getProperties() {
        return Collections.unmodifiableCollection(facetMap.keySet());
    }

    @Override
    void addFacetValue(VirtualProperty field, String value) {
        Map<String, Integer> propertyMap = facetMap.get(field);
        Integer old = propertyMap.get(value);
        propertyMap.put(value, old == null ? 1 : old + 1);
    }

    @Override
    public Set<Map.Entry<String, Integer>> getFacets(VirtualProperty property) {
        return Collections.unmodifiableSet(facetMap.get(property).entrySet());
    }

    @Override
    public Integer getFacetValue(VirtualProperty property, String facet) {
        return facetMap.get(property).get(facet);
    }
    
    @Override
    Map<VirtualProperty, Map<String, Integer>> getFacetMap() {
        return facetMap;
    }
    
    @Override
    void postProcess() {
        //do nothing
    }
}
