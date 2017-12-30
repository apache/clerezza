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
 * A Blank Node represents a resource, 
 * but does not indicate a URI for the resource. Blank nodes act like 
 * existentially qualified variables in first order logic. 
 *
 * An <a href= "http://www.w3.org/TR/rdf11-concepts/#dfn-blank-node" >RDF-1.1
 * Blank Node</a>, as defined by <a href=
 * "http://www.w3.org/TR/rdf11-concepts/#section-blank-nodes" >RDF-1.1 Concepts
 * and Abstract Syntax</a>, a W3C Recommendation published on 25 February 2014.<br>
 *
 * Note that: Blank nodes are disjoint from IRIs and literals. Otherwise,
 * the set of possible blank nodes is arbitrary. RDF makes no reference to any
 * internal structure of blank nodes.
 *
 *
 * @see <a href= "http://www.w3.org/TR/rdf11-concepts/#dfn-blank-node">RDF-1.1
 * Blank Node</a>
 */
public class BlankNode implements BlankNodeOrIRI {

}
