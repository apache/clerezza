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
package org.apache.clerezza.rdf.utils;

import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.rdf.utils.smushing.IfpSmusher;
import org.apache.clerezza.rdf.utils.smushing.SameAsSmusher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility to smush equivalent resources. For greater flexibility use the 
 * classes in the smushing package.
 * 
 * @author reto
 */
public class Smusher {

    static final Logger log = LoggerFactory.getLogger(Smusher.class);

    /**
     * smush mGaph given the ontological facts. Currently it does only one step
     * ifp smushin, i.e. only ifps are taken in account and only nodes that have
     * the same node as ifp object in the orignal graph are equates. (calling
     * the method a second time might lead to additional smushings.)
     *
     * @param mGraph
     * @param tBox
     */
    public static void smush(Graph mGraph, Graph tBox) {
        new IfpSmusher().smush(mGraph, tBox);
    }

    /**
     * Smushes the specified graph adding owl:sameAs statements pointing to the new canonical IRI
     * 
     * @param mGraph
     * @param owlSameStatements 
     */
    public static void sameAsSmush(Graph mGraph, Graph owlSameStatements) {
        new SameAsSmusher().smush(mGraph, owlSameStatements, true);
    }


}
