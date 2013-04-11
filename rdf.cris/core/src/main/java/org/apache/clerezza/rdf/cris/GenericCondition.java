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
import java.util.Iterator;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

/**
 * A generic condition using the Apache Lucene {@link QueryParser}.
 *
 * This supports full Lucene query syntax.
 *
 * @author daniel
 */
public class GenericCondition extends Condition {

    private final MultiFieldQueryParser queryParser;
    private final String searchQuery;
    private final String[] fields;

    /**
     * Creates a new GenericCondition.
     * 
     * Special characters in the query string are not escaped.
     *
     * @param properties    the properties to search for.
     * @param searchQuery    the search string (pattern).
     */
    public GenericCondition(Collection<VirtualProperty> properties, String searchQuery) {
        this(properties, searchQuery, false);
    }
    
    /**
     * Creates a new GenericCondition.
     *
     * @param properties    the properties to search for.
     * @param searchQuery    the search string (pattern).
     * @param escapeQuery    whether to escape special characters in the query string or not.
     */
    public GenericCondition(Collection<VirtualProperty> properties, String searchQuery, boolean escapeQuery) {
        fields = new String[properties.size()];
        Iterator<VirtualProperty> it = properties.iterator();
        for(int i = 0; i < properties.size(); ++i) {
            fields[i] = it.next().stringKey;
        }
        
        if(escapeQuery) {
            searchQuery = QueryParser.escape(searchQuery);
        }
        this.searchQuery = searchQuery;

        this.queryParser = new MultiFieldQueryParser(Version.LUCENE_41,
                fields,
                new StandardAnalyzer(Version.LUCENE_41));
        queryParser.setAllowLeadingWildcard(true);
    }

    @Override
    public Query query() {
        try {
            Query q = queryParser.parse(searchQuery);
            return q;
        } catch (ParseException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

}
