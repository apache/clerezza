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

package org.apache.clerezza.platform.content.collections;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.rdf.ontologies.HIERARCHY;
import org.apache.clerezza.rdf.ontologies.RDF;

/**
 * Creates the collections containing a resource in an underlying Graph
 *
 * @author reto
 */
public class CollectionCreator {

    private Graph mGraph;

    public CollectionCreator(Graph mGraph) {
        this.mGraph = mGraph;
    }

    public void createContainingCollections(IRI uriRef) {
        try {
            URI uri = new URI(uriRef.getUnicodeString());
            if (uri.getHost() == null) {
                throw new IllegalArgumentException("Host name missing in " + uriRef);
            }
            String[] pathSections = uri.getRawPath().split("/");
            for (int i = pathSections.length - 1; i >= 0 ; i--) {
                String section = pathSections[i];
                if (section.length() == 0) {
                    if (i == 0) {
                        return;
                    } else {
                        throw new IllegalArgumentException(
                                uriRef+" contains consequtive slashes in path section");
                    }
                }
                final String unicodeString = uriRef.getUnicodeString();
                int lastIndexOf = unicodeString.lastIndexOf(section);
                IRI parentIRI = new IRI(unicodeString.substring(0, lastIndexOf));
                mGraph.add(new TripleImpl(uriRef, HIERARCHY.parent, parentIRI));
                mGraph.add(new TripleImpl(parentIRI, RDF.type, HIERARCHY.Collection));
                uriRef = parentIRI;

            }
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


}
