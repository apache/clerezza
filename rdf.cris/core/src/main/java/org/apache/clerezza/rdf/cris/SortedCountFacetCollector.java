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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;



/**
 * A FacetCollector that counts members of a facet and returns ordered facets.
 * 
 * Uses the comparable interface to determine order between facets. 
 * 
 * Note: Facets are always ordered first by value and only when 
 * values are equal according to keys.
 *
 * @author daniel
 */
public class SortedCountFacetCollector extends CountFacetCollector {

    private Map<VirtualProperty, SortedSet<Map.Entry<String, Integer>>> sortedFacetMap;
    private boolean reverseValues = false; 
    private boolean reverseKeys = false; 
    
    /**
     * Default Constructor.
     * 
     * Creates a facet collector that orders keys in ascending and values in descending order.
     */
    public SortedCountFacetCollector() {
        this(false, true);
    }

    /**
     * Creates a facet collector that orders according to the supplied parameters.
     * 
     * Note: Facets are always ordered first by value and only when 
     * values are equal according to keys.
     * 
     * @param reverseKeyOrder true = descending order, false = ascending order.
     * @param reverseValueOrder true = descending order, false = ascending order.
     */
    public SortedCountFacetCollector(boolean reverseKeyOrder, boolean reverseValueOrder) {
        super();
        sortedFacetMap = Collections.emptyMap();
        this.reverseValues = reverseValueOrder;
        this.reverseKeys = reverseKeyOrder;
    }

    /**
     * Whether the collector orders values in ascending or descending order.
     * 
     * @return true = descending order, false = ascending order.
     */
    public boolean valuesReversed() {
        return reverseValues;
    }
    
    /**
     * Whether the collector orders keys in ascending or descending order.
     * 
     * @return true = descending order, false = ascending order.
     */
    public boolean keysReversed() {
        return reverseKeys;
    }
    
    @Override
    public Set<Entry<String, Integer>> getFacets(VirtualProperty property) {
        return sortedFacetMap.get(property);
    }

    @Override
    void postProcess() {
        super.postProcess();
        
        Collection<VirtualProperty> properties = facetMap.keySet();
        sortedFacetMap = new HashMap<VirtualProperty, 
                    SortedSet<Entry<String, Integer>>>(properties.size());
        for(VirtualProperty property : properties) {
            
            TreeSet<Entry<String, Integer>> sortedSet = 
                    new TreeSet<Entry<String, Integer>>(new EntrySetComparator());
            Set<Entry<String, Integer>> entrySet = facetMap.get(property).entrySet();
            sortedSet.addAll(entrySet);
            sortedFacetMap.put(property, sortedSet);
        }
    }
    
    
    private class EntrySetComparator implements Comparator<Entry<String, Integer>> {
        @Override
        public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
            String key1, key2;
            if(reverseKeys) {
                key1 = e2.getKey();
                key2 = e1.getKey();
            } else{
                key1 = e1.getKey();
                key2 = e2.getKey();
            }
            
            Integer val1, val2;
            Entry<String, Integer> o2 = e2;
            if(reverseValues) {
                val1 = e2.getValue();
                val2 = e1.getValue();
            } else {
                val1 = e1.getValue();
                val2 = e2.getValue();
            }
            
            
            int val;
            if(key1.equals(key2)) {
                val = 0;
            } else {
                val = val1.compareTo(val2);
                if(val == 0) {
                    val = key1.compareTo(key2);
                }
            }
            
            return val;
        }
        
    }
}
