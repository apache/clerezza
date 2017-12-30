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
 * A structure containing a subject, a predicate, and an object. 
 * Also known as a statement.
 *
 * @author reto
 */
public interface Triple {

    BlankNodeOrIRI getSubject();

    IRI getPredicate();

    RDFTerm getObject();

    /**
     * 
     * @param obj
     * @return true iff subject, predicate, and object of both triples are equal
     */
    @Override
    boolean equals(Object obj);

    /**
     * The hash code is computed as follow
     * (subject.hashCode() >> 1) ^  predicate.hashCode() ^ object.hashCode() << 1)
     * 
     * Note that the hash returned is computed including the hash of BNodes, so 
     * it is not blank-node blind as in Graph.
     * 
     * This would have to change if triple should extend Graph
     * 
     * @return hash code
     */
    @Override
    int hashCode();

}
