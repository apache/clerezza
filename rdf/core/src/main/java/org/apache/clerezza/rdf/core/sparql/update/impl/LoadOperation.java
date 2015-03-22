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

import java.util.Set;
import org.apache.commons.rdf.Iri;
import org.apache.clerezza.rdf.core.sparql.update.UpdateOperation;

/**
 * The LOAD operation reads an RDF document from a IRI and inserts its triples into the specified ImmutableGraph in the ImmutableGraph Store. 
 * If the destination ImmutableGraph already exists, then no data in that ImmutableGraph will be removed.
 * If no destination ImmutableGraph IRI is provided to load the triples into, then the data will be loaded into the default ImmutableGraph.
 * @see <a href="http://www.w3.org/TR/2013/REC-sparql11-update-20130321/#load">SPARQL 1.1 Update: 3.1.4 LOAD</a>
 * @author hasan
 */
public class LoadOperation extends SimpleUpdateOperation {

    public void setSource(Iri source) {
        setInputGraph(source);
    }

    public Iri getSource() {
        return getInputGraph(null);
    }
}
