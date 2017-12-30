/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.clerezza.commons.rdf.impl.sparql;

import java.util.Collection;
import java.util.Objects;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;

/**
 *
 * @author developer
 */
class SparqlBNode extends BlankNode {
    
    final static IRI internalBNodeId = new IRI("urn:x-internalid:fdmpoihdfw");
    
    final ImmutableGraph context;
    private final int isoDistinguisher;

    SparqlBNode(BlankNode node, Collection<Triple> context, int isoDistinguisher) {
        this.isoDistinguisher = isoDistinguisher;
        final SimpleGraph contextBuider = new SimpleGraph();
        for (Triple triple : context) {
            BlankNodeOrIRI subject = triple.getSubject();
            RDFTerm object = triple.getObject();
            contextBuider.add(new TripleImpl(subject.equals(node) ? internalBNodeId : subject, 
                    triple.getPredicate(), 
                    object.equals(node) ? internalBNodeId : object));
        }
        this.context = contextBuider.getImmutableGraph();
    }

    @Override
    public int hashCode() {
        int hash = 7+isoDistinguisher;
        hash = 61 * hash + Objects.hashCode(this.context);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SparqlBNode other = (SparqlBNode) obj;
        if (isoDistinguisher != other.isoDistinguisher) {
            return false;
        }
        return Objects.equals(this.context, other.context);
    }
}
