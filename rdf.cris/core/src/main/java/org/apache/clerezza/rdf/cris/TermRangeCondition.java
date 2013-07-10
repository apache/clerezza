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
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;

/**
 * Allows searches for rangers of terms.
 * 
 * @author rbn, tio
 */
public class TermRangeCondition extends Condition {

    /**
     * The property to search for.
     */
    VirtualProperty property;
    
    /**
     * the lower limit of the search
     */
    String lowerTerm;
    
    /**
     * the upper limit of the search
     */
    String upperTerm;
    
    boolean includeUpper;
    boolean includeLower;

    /**
     * A condition for searches on {@code property} that returns property-objects 
     * (values) that lie between {@code lowerTerm} and {@code upperTerm}
     * according to {@link String#compareTo(java.lang.String)}.
     * 
     * @param property    the resource type to search for
     * @param lowerTerm    the lower limit of the range
     * @param upperTerm the upper limit of the range
     * @param includeUpper whether to include the upper limit in the results
     * @param includeLower whether to include the lower limit in the results
     */
    public TermRangeCondition(VirtualProperty property , String lowerTerm, String upperTerm,
        boolean includeUpper,  boolean includeLower)  {

        this.property = property;
        this.lowerTerm = lowerTerm;
        this.upperTerm = upperTerm;
        this.includeUpper = includeUpper;
        this.includeLower = includeLower;    
    }

    /**
     * A condition for searches on {@code property} that returns property-objects 
     * (values) that lie between {@code lowerTerm} and {@code upperTerm}
     * according to {@link String#compareTo(java.lang.String)}.
     * 
     * @param property    the resource type to search for
     * @param lowerTerm    the lower limit of the range
     * @param upperTerm the upper limit of the range
     * @param includeUpper whether to include the upper limit in the results
     * @param includeLower whether to include the lower limit in the results
     */
    public TermRangeCondition(UriRef uriRefProperty, String lowerTerm,String upperTerm,
                               boolean includeUpper, boolean includeLower) {
        this(new PropertyHolder(uriRefProperty), lowerTerm, upperTerm, includeUpper, includeLower);
    }
    
    @Override
    public Query query() {
        return new TermRangeQuery(property.stringKey, new BytesRef(lowerTerm), new BytesRef(upperTerm), includeUpper, includeLower);
    }
}
