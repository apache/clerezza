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
 * Typed literals have a lexical form and a data type URI being an RDF URI reference.
 * 
 * To convert java objects to typed literals use {@link LiteralFactory}
 * 
 * @author reto
 *
 */
public interface TypedLiteral extends Literal {

    /** 
     * Returns the data type which is a UriRef.
     * Note that the return value is not a node in the graph
     * 
     * @return UriRef
     */
    public UriRef getDataType();
    
    /** 
     * Two TypedLiteral nodes are equal iff they have the same lexical form and
     * the same data type
     *
     * @param obj
     * @return true if <code>obj</code> is an instance of <code>TypedLiteral</code> 
     * for which the lexical form and the data type URI are equal to the ones 
     * of this instance, false otherwise
     */
    public boolean equals(Object obj);

    /** 
     * The hash code is equal to the hash code of the lexical form 
     * plus the hash code of the dataType
     * 
     * @return hash code
     */
    public int hashCode();
}
