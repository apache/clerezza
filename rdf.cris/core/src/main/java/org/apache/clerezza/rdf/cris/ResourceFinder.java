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

import java.util.ArrayList;
import java.util.List;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;


/**
 * A searchable index.
 *
 * @author rbn, tio, daniel
 */
 public abstract class ResourceFinder {
    
    /**
     * recreates the index
     */
    public abstract void reCreateIndex();

    /**
     * optimize the index
     */
    public abstract void optimizeIndex();


    /**
     * Find resources using conditions.
     * 
     * @param conditions
     *        a list of conditions to construct a query from.
     * @return    
     *        a list of resources that match the query.
     *
     * @throws ParseException when the resulting query is illegal.
     */
    public List<NonLiteral> findResources(List<? extends Condition> conditions)
            throws ParseException {
        return findResources(conditions, new FacetCollector[0]);
    }
    
    /**
     * Find resources using conditions and collect facets. 
     * 
     * @param conditions
     *        a list of conditions to construct a query from.
     * @param facetCollectors
     *        facet collectors to apply to the query result.
     * @return    
     *        a list of resources that match the query.
     * 
     * @throws ParseException when the resulting query is illegal.
     */
    public List<NonLiteral> findResources(List<? extends Condition> conditions, 
            FacetCollector... facetCollectors) throws ParseException {
        
        return findResources(conditions, null, facetCollectors);
    }
    
    /**
     * Find resources using conditions and collect facets and a sort order. 
     * 
     * @param conditions
     *        a list of conditions to construct a query from.
     * @param facetCollectors
     *        facet collectors to apply to the query result.
     * @param sortSpecification 
     *        specifies the sort order.
     * @return    
     *        a list of resources that match the query.
     * 
     * @throws ParseException when the resulting query is illegal.
     */
    public abstract List<NonLiteral> findResources(List<? extends Condition> conditions, 
            SortSpecification sortSpecification, FacetCollector... facetCollectors) 
            throws ParseException;

    /**
     * Find resource with given property whose value matches a pattern.
     * 
     * @param property    The property to which to apply the pattern.
     * @param pattern    The pattern from which to construct a query.
     * @return    a list of resources that match the query.
     * 
     * @throws ParseException when the resulting query is illegal.
     */
    public List<NonLiteral> findResources(UriRef property, String pattern) 
            throws ParseException {
        return findResources(property, pattern, false);
    }
    
    /**
     * Find resource with given property whose value matches a pattern.
     * 
     * @param property    The property to which to apply the pattern.
     * @param pattern    The pattern from which to construct a query.
     * @param escapePattern    whether to escape reserved characters in the pattern
     * @return  list of resources that match the query.
     * 
     * @throws ParseException when the resulting query is illegal.
     */
    public List<NonLiteral> findResources(UriRef property, String pattern, boolean escapePattern)
            throws ParseException {
        return findResources(property, pattern, escapePattern, new FacetCollector[0]);
    }
    
    /**
     * Find resource with given property whose value matches a pattern and collect facets.
     * 
     * @param property    The property to which to apply the pattern.
     * @param pattern    The pattern from which to construct a query.
     * @param escapePattern    whether to escape reserved characters in the pattern
     * @param facetCollectors facet collectors to apply to the query result.
     * @return    a list of resources that match the query.
     * 
     * @throws ParseException when the resulting query is illegal.
     */
    public List<NonLiteral> findResources(UriRef property, String pattern, 
            boolean escapePattern, FacetCollector... facetCollectors) 
            throws ParseException {
        
        List<Condition> list = new ArrayList<Condition>();
        if(escapePattern) {
            pattern = QueryParser.escape(pattern);
        }
        list.add(new WildcardCondition(new PropertyHolder(property), pattern));
        return findResources(list, facetCollectors);
    }
    
    /**
     * Find resource with given property whose value matches a pattern 
     * and sort order and collect facets.
     * 
     * @param property    The property to which to apply the pattern.
     * @param pattern    The pattern from which to construct a query.
     * @param escapePattern    whether to escape reserved characters in the pattern
     * @param sortSpecification    specifies the sort order.
     * @param facetCollectors facet collectors to apply to the query result.
     * @return    a list of resources that match the query.
     * 
     * @throws ParseException when the resulting query is illegal.
     */
    public List<NonLiteral> findResources(UriRef property, String pattern, 
            boolean escapePattern, SortSpecification sortSpecification, 
            FacetCollector... facetCollectors) throws ParseException {
        
        List<Condition> list = new ArrayList<Condition>();
        if(escapePattern) {
            pattern = QueryParser.escape(pattern);
        }
        list.add(new WildcardCondition(new PropertyHolder(property), pattern));
        return findResources(list, sortSpecification, facetCollectors);
    }
    
    /**
     * Find resource with given VirtualProperty whose value matches a pattern.
     * 
     * @param property    The property to which to apply the pattern.
     * @param pattern    The pattern from which to construct a query.
     * @return    a list of resources that match the query.
     * 
     * @throws ParseException when the resulting query is illegal.
     */
    public List<NonLiteral> findResources(VirtualProperty property, String pattern) 
            throws ParseException {
        return findResources(property, pattern, false);
    }

    /**
     * Find resource with given VirtualProperty whose value matches a pattern.
     * 
     * @param property    The property to which to apply the pattern.
     * @param pattern    The pattern from which to construct a query.
     * @param escapePattern    whether to escape reserved characters in the pattern
     * @return    a list of resources that match the query.
     * 
     * @throws ParseException when the resulting query is illegal.
     */
    public List<NonLiteral> findResources(VirtualProperty property, String pattern,
            boolean escapePattern) throws ParseException {
        
        return findResources(property, pattern, escapePattern, new FacetCollector[0]);
    }
    
    /**
     * Find resource with given VirtualProperty whose value matches a pattern and collect facets.
     * 
     * @param property    The property to which to apply the pattern.
     * @param pattern    The pattern from which to construct a query.
     * @param escapePattern    whether to escape reserved characters in the pattern
     * @param facetCollectors    facet collectors to apply to the query result.
     * @return    a list of resources that match the query.
     * 
     * @throws ParseException when the resulting query is illegal.
     */
    public List<NonLiteral> findResources(VirtualProperty property, String pattern, 
            boolean escapePattern, FacetCollector... facetCollectors) 
            throws ParseException {
        
        List<Condition> list = new ArrayList<Condition>();
        if(escapePattern) {
            pattern = QueryParser.escape(pattern);
        }
        list.add(new WildcardCondition(property, pattern));
        return findResources(list, facetCollectors);
    }
    
    /**
     * Find resource with given VirtualProperty whose value matches a pattern 
     * and sort specification and collect facets.
     * 
     * @param property    The property to which to apply the pattern.
     * @param pattern    The pattern from which to construct a query.
     * @param escapePattern    whether to escape reserved characters in the pattern
     * @param sortSpecification    specifies the sort order.
     * @param facetCollectors    facet collectors to apply to the query result.
     * @return    a list of resources that match the query.
     * 
     * @throws ParseException when the resulting query is illegal.
     */
    public List<NonLiteral> findResources(VirtualProperty property, String pattern, 
            boolean escapePattern, SortSpecification sortSpecification, 
            FacetCollector... facetCollectors) throws ParseException {
        
        List<Condition> list = new ArrayList<Condition>();
        if(escapePattern) {
            pattern = QueryParser.escape(pattern);
        }
        list.add(new WildcardCondition(property, pattern));
        return findResources(list, sortSpecification, facetCollectors);
    }
}
