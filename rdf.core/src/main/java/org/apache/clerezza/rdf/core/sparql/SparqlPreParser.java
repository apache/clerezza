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
package org.apache.clerezza.rdf.core.sparql;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.sparql.query.DataSet;
import org.apache.clerezza.rdf.core.sparql.query.Query;
import org.apache.clerezza.rdf.core.sparql.query.SparqlUnit;
import org.apache.clerezza.rdf.core.sparql.update.Update;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 * This class implements an OSGi service to provide a method to obtain referred Graphs in a SPARQL Query or Update.
 *
 * @author hasan
 */

@Component
@Service(SparqlPreParser.class)
public class SparqlPreParser {

    @Reference
    TcManager tcManager;

    public SparqlPreParser() {
    }

    public SparqlPreParser(TcManager tcManager) {
        this.tcManager = tcManager;
    }

    /**
     * This returns the graphs targeted by the queryString. This are the the 
     * triple collections explicitely refreded in FROM and FROM NAMED clauses, 
     * and if the queryString contains no FROM clause the defaultGraph.
     * 
     * For queries that are not limited to specified set of graphs null is returned.
     * 
     * 
     * @param queryString
     * @param defaultGraph
     * @return 
     * @throws ParseException 
     */
    public Set<UriRef> getReferredGraphs(String queryString, UriRef defaultGraph) throws ParseException {
        Set<UriRef> referredGraphs;
        JavaCCGeneratedSparqlPreParser parser = new JavaCCGeneratedSparqlPreParser(new StringReader(queryString));
        SparqlUnit sparqlUnit;
        sparqlUnit = parser.parse();
        if (sparqlUnit.isQuery()) {
            Query q = sparqlUnit.getQuery();
            DataSet dataSet = q.getDataSet();
            if (dataSet != null) {
                referredGraphs = dataSet.getDefaultGraphs();
                referredGraphs.addAll(dataSet.getNamedGraphs());
            } else {
                referredGraphs = new HashSet<UriRef>();
            }
        } else {
            Update u = sparqlUnit.getUpdate();
            referredGraphs = u.getReferredGraphs(defaultGraph, tcManager);
        }
        if (referredGraphs.isEmpty()) {
            return null;
//            referredGraphs.add(defaultGraph);
        }
        return referredGraphs;
    }
}
