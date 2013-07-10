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

import org.apache.clerezza.rdf.core.UriRef;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.index.Term;

/**
 * Allows searches with wildcards.
 * 
 * @author rbn, tio
 */
public class WildcardCondition extends Condition{

    /**
     * The property to search for.
     */
    VirtualProperty property;
    
    /**
     * The search-query
     */
    String pattern;

    /**
     * A condition for searches with wildcard (? = any single character, 
     * * = any number of any character)
     * 
     * @param property    the resource type to search for
     * @param pattern    the search query
     */
    public WildcardCondition(VirtualProperty property, String pattern) {
            this.pattern = pattern;
            this.property = property;
    }
    
    /**
     * A condition for searches with wildcard (? = any single character, 
     * * = any number of any character)
     * 
     * @param property    the resource type to search for
     * @param pattern    the search query
     */
    public WildcardCondition(UriRef uriRefProperty,String pattern) {
        this(new PropertyHolder(uriRefProperty), pattern);

    }
    
    @Override
    public Query query() {
        return new WildcardQuery(new Term(property.stringKey, pattern));
    }
}
