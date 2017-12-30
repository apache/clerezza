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
package org.apache.clerezza.commons.rdf;

/**
 * A graph, modeled as a set of triples.
 * This interface does not extend java.util.Set because of the different 
 * identity constraints, i.e. two <code>Graph</code>s may be equal (isomorphic) 
 * even if the set of triples are not.
 * 
 * Implementations MUST be immutable and throw respective exceptions, when 
 * add/remove-methods are called.
 * 
 * @see org.apache.clerezza.rdf.core.impl.AbstractGraph
 * @author reto
 *
 */
public interface ImmutableGraph extends Graph {

    /** 
     * Returns true if two graphs are isomorphic
     * 
     * @return true if two graphs are isomorphic
     */
    @Override
    public boolean equals(Object obj);

    /** 
     * Return the sum of the blank-nodes independent hashes of the triples. 
     * More precisely the hash of the triple is calculated as follows:
     * (hash(subject) >> 1) ^  hash(hashCode) ^ (hash(hashCode) << 1)
     * Where the hash-fucntion return the hashCode of the argument 
     * for grounded arguments and 0 otherwise. 
     * 
     * @return hash code
     */
    @Override
    public int hashCode();
}