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

package org.apache.clerezza.rdf.core.impl.graphmatching;

import org.apache.commons.rdf.BlankNode;
import org.apache.commons.rdf.MGraph;
import org.apache.commons.rdf.BlankNodeOrIri;
import org.apache.commons.rdf.Iri;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;

/**
 *
 * @author reto
 */
public class Utils4Testing {

    static MGraph generateLine(int size, final BlankNodeOrIri firstNode) {
        if (size < 1) {
            throw new IllegalArgumentException();
        }
        MGraph result = new SimpleMGraph();
        BlankNodeOrIri lastNode = firstNode;
        for (int i = 0; i < size; i++) {
            final BlankNode newNode = new BlankNode();
            result.add(new TripleImpl(lastNode, u1, newNode));
            lastNode = newNode;
        }
        return result;
    }

    final static Iri u1 = new Iri("http://example.org/u1");

}
