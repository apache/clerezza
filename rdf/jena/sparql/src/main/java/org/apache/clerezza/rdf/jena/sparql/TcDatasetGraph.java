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
package org.apache.clerezza.rdf.jena.sparql;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.util.Context;
import java.util.Iterator;
import org.apache.commons.rdf.Graph;
import org.apache.commons.rdf.Iri;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.jena.facade.JenaGraph;

/**
 *
 * @author rbn
 */
class TcDatasetGraph implements DatasetGraph {

    private TcManager tcManager;
    private Graph defaultGraph;

    TcDatasetGraph(TcManager tcManager, Graph defaultGraph) {
        this.tcManager = tcManager;
        this.defaultGraph = defaultGraph;
    
    }

    @Override
    public com.hp.hpl.jena.graph.Graph getDefaultGraph() {
        final JenaGraph jenaGraph = new JenaGraph(defaultGraph);
        return jenaGraph;
    }

    @Override
    public com.hp.hpl.jena.graph.Graph getGraph(Node node) {
        final JenaGraph jenaGraph = new JenaGraph(
                tcManager.getGraph(new Iri(node.getURI())));
        return jenaGraph;
    }

    @Override
    public boolean containsGraph(Node node) {
        try {
            tcManager.getGraph(new Iri(node.getURI()));
            return true;
        } catch (NoSuchEntityException e) {
            return false;
        }
    }

    @Override
    public Iterator<Node> listGraphNodes() {
        final Iterator<Iri> graphsIter = tcManager.listGraphs().iterator();
        return new Iterator<Node>() {

            @Override
            public boolean hasNext() {
                return graphsIter.hasNext();
            }

            @Override
            public Node next() {
                Iri uriRef = graphsIter.next();
                if (uriRef == null) {
                    return null;
                }
                return new Node_URI(uriRef.getUnicodeString()) {};
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        };
    }

    @Override
    public Lock getLock() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long size() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(Quad quad) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addGraph(Node graphName, com.hp.hpl.jena.graph.Graph graph) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean contains(Node g, Node s, Node p, Node o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean contains(Quad quad) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(Quad quad) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteAny(Node g, Node s, Node p, Node o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Quad> find(Quad quad) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Quad> find(Node g, Node s, Node p, Node o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeGraph(Node graphName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setDefaultGraph(com.hp.hpl.jena.graph.Graph g) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Quad> find() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Iterator<Quad> findNG(Node node, Node node1, Node node2, Node node3) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
	
	@Override
	public void add( Node g, Node s, Node p, Node o ) {
        throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public void delete( Node g, Node s, Node p, Node o ) {
        throw new UnsupportedOperationException("Not supported yet.");
	}
}
