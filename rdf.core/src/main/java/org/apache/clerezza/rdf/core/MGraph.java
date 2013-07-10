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
package org.apache.clerezza.rdf.core;

/** 
 * A mutable graph 
 *
 * @author reto
 *
 */
public interface MGraph extends TripleCollection {

    /**
     * Returns true if <code>other</code> represents the same mutable graph as
     * this instance, false otherwise. It returns true if this == other or if it
     * is otherwise guaranteed that changes to one of the instances are
     * immediately reflected in the other.
     *
     * @param other
     * @return true if other == this
     */
    @Override
    public boolean equals(Object other);

    /**
     * Returns the graph
     *
     * @return graph
     */
    public Graph getGraph();
}