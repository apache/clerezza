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
package org.apache.clerezza.rdf.core.sparql.update.impl;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.rdf.Iri;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.sparql.query.GroupGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.impl.SimpleDataSet;
import org.apache.clerezza.rdf.core.sparql.update.UpdateOperation;

/**
 * This ModifyOperation is a DELETE/INSERT operation.
 * @see <a href="http://www.w3.org/TR/2013/REC-sparql11-update-20130321/#deleteInsert">SPARQL 1.1 Update: 3.1.3 DELETE/INSERT</a>
 * 
 * The DELETE/INSERT operation can be used to remove or add triples from/to the ImmutableGraph Store based on bindings 
 * for a query pattern specified in a WHERE clause.
 * 
 * @author hasan
 */
public class ModifyOperation implements UpdateOperation {
    private Iri fallbackGraph = null;
    private UpdateOperationWithQuads deleteOperation = null;
    private UpdateOperationWithQuads insertOperation = null;
    private SimpleDataSet dataSet = null;
    private GroupGraphPattern queryPattern = null;

    public void setFallbackGraph(Iri fallbackGraph) {
        this.fallbackGraph = fallbackGraph;
    }

    public void setDeleteOperation(UpdateOperationWithQuads deleteOperation) {
        this.deleteOperation = deleteOperation;
    }

    public void setInsertOperation(UpdateOperationWithQuads insertOperation) {
        this.insertOperation = insertOperation;
    }

    public void setDataSet(SimpleDataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void addGraphToDataSet(Iri ImmutableGraph) {
        if (dataSet == null) {
            dataSet = new SimpleDataSet();
        }
        dataSet.addDefaultGraph(ImmutableGraph);
    }

    public void addNamedGraphToDataSet(Iri namedGraph) {
        if (dataSet == null) {
            dataSet = new SimpleDataSet();
        }
        dataSet.addNamedGraph(namedGraph);
    }

    public void setQueryPattern(GroupGraphPattern queryPattern) {
        this.queryPattern = queryPattern;
    }

    @Override
    public Set<Iri> getInputGraphs(Iri defaultGraph, TcProvider tcProvider) {
        Set<Iri> graphs = new HashSet<Iri>();
        if (dataSet != null) {
            graphs.addAll(dataSet.getDefaultGraphs());
            graphs.addAll(dataSet.getNamedGraphs());
        } else {
            if (fallbackGraph != null) {
                graphs.add(fallbackGraph);
            }
        }
        if (graphs.isEmpty()) {
            graphs.add(defaultGraph);
        }
        if (queryPattern != null) {
            graphs.addAll(queryPattern.getReferredGraphs());
        }
        return graphs;
    }

    @Override
    public Set<Iri> getDestinationGraphs(Iri defaultGraph, TcProvider tcProvider) {
        Set<Iri> graphs = new HashSet<Iri>();
        Iri dfltGraph = (fallbackGraph != null) ? fallbackGraph : defaultGraph;
        if (deleteOperation != null) {
            graphs.addAll(deleteOperation.getDestinationGraphs(dfltGraph, tcProvider));
        }
        if (insertOperation != null) {
            graphs.addAll(insertOperation.getDestinationGraphs(dfltGraph, tcProvider));
        }
        return graphs;
    }
}
