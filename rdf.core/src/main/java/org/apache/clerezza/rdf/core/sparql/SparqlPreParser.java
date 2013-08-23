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
import java.util.List;
import java.util.Set;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.sparql.query.AlternativeGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.DataSet;
import org.apache.clerezza.rdf.core.sparql.query.GraphGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.GraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.GroupGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.MinusGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.OptionalGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.Query;
import org.apache.clerezza.rdf.core.sparql.query.SparqlUnit;
import org.apache.clerezza.rdf.core.sparql.update.Update;

/**
 * This class implements an OSGi service to provide a method to obtain referred Graphs in a SPARQL Query or Update.
 *
 * @author hasan
 */


public class SparqlPreParser {

    TcProvider tcProvider;

    public SparqlPreParser() {
    }

    public SparqlPreParser(TcProvider tcProvider) {
        this.tcProvider = tcProvider;
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
        boolean referringVariableNamedGraph = false;
        if (sparqlUnit.isQuery()) {
            Query q = sparqlUnit.getQuery();
            DataSet dataSet = q.getDataSet();
            if (dataSet != null) {
                referredGraphs = dataSet.getDefaultGraphs();
                referredGraphs.addAll(dataSet.getNamedGraphs());
            } else {
                referredGraphs = new HashSet<UriRef>();
            }
            GroupGraphPattern queryPattern = q.getQueryPattern();
            Set<GraphPattern> graphPatterns = queryPattern.getGraphPatterns();
            for (GraphPattern graphPattern : graphPatterns) {
            }
//            referringVariableNamedGraph = q.referringVariableNamedGraph();
            referringVariableNamedGraph = referringVariableNamedGraph(q);
        } else {
            Update u = sparqlUnit.getUpdate();
            referredGraphs = u.getReferredGraphs(defaultGraph, tcProvider);
        }
        if (referredGraphs.isEmpty()) {
            if (referringVariableNamedGraph) {
                return null;
            }
            referredGraphs.add(defaultGraph);
        }
        return referredGraphs;
    }

    private boolean referringVariableNamedGraph(Query query) {
        GroupGraphPattern queryPattern = query.getQueryPattern();
        Set<GraphPattern> graphPatterns = queryPattern.getGraphPatterns();
        return referringVariableNamedGraph(graphPatterns);
    }

    private boolean referringVariableNamedGraph(Set<GraphPattern> graphPatterns) {
        boolean referringVariableNamedGraph = false;
        for (GraphPattern graphPattern : graphPatterns) {
            if (referringVariableNamedGraph(graphPattern)) {
                referringVariableNamedGraph = true;
                break;
            }
        }
        return referringVariableNamedGraph;
    }

    private boolean referringVariableNamedGraph(GraphPattern graphPattern) {
        if (graphPattern instanceof GraphGraphPattern) {
            return ((GraphGraphPattern) graphPattern).getGraph().isVariable();
        }
        if (graphPattern instanceof AlternativeGraphPattern) {
            List<GroupGraphPattern> alternativeGraphPatterns =
                    ((AlternativeGraphPattern) graphPattern).getAlternativeGraphPatterns();
            boolean referringVariableNamedGraph = false;
            for (GroupGraphPattern groupGraphPattern : alternativeGraphPatterns) {
                if (referringVariableNamedGraph(groupGraphPattern)) {
                    referringVariableNamedGraph = true;
                    break;
                }
            }
            return referringVariableNamedGraph;
        }
        if (graphPattern instanceof OptionalGraphPattern) {
            GraphPattern mainGraphPattern = ((OptionalGraphPattern) graphPattern).getMainGraphPattern();
            if (referringVariableNamedGraph(mainGraphPattern)) {
                return true;
            }
            GroupGraphPattern optionalGraphPattern = ((OptionalGraphPattern) graphPattern).getOptionalGraphPattern();
            return referringVariableNamedGraph(optionalGraphPattern);
        }
        if (graphPattern instanceof MinusGraphPattern) {
            GraphPattern minuendGraphPattern = ((MinusGraphPattern) graphPattern).getMinuendGraphPattern();
            if (referringVariableNamedGraph(minuendGraphPattern)) {
                return true;
            }
            GroupGraphPattern subtrahendGraphPattern = ((MinusGraphPattern) graphPattern).getSubtrahendGraphPattern();
            return referringVariableNamedGraph(subtrahendGraphPattern);
        }
        if (graphPattern instanceof GroupGraphPattern) {
            GroupGraphPattern groupGraphPattern = (GroupGraphPattern) graphPattern;
            if (groupGraphPattern.isSubSelect()) {
                Query query = ((GroupGraphPattern) graphPattern).getSubSelect();
                return referringVariableNamedGraph(query);
            } else {
                Set<GraphPattern> graphPatterns = ((GroupGraphPattern) graphPattern).getGraphPatterns();
                return referringVariableNamedGraph(graphPatterns);
            }
        }
        return false;
    }
}
