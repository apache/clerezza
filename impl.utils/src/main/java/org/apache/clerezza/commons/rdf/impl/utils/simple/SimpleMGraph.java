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
package org.apache.clerezza.commons.rdf.impl.utils.simple;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Triple;

/**
 *
 * @deprecated Use SimpleGraph
 * @author reto
 */
@Deprecated
public class SimpleMGraph extends SimpleGraph implements Graph {

    /**
     * Creates an empty SimpleMGraph
     */
    public SimpleMGraph() {
    }

    public SimpleMGraph(Set<Triple> baseSet) {
        super(baseSet);
    }

    public SimpleMGraph(Collection<Triple> baseCollection) {
        super(baseCollection);
    }

    public SimpleMGraph(Iterator<Triple> iterator) {
        super(iterator);
    }

}

    