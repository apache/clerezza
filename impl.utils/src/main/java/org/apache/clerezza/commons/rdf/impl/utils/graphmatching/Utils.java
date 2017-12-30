package org.apache.clerezza.commons.rdf.impl.utils.graphmatching;
/*
 *
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
 *
*/


import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.Triple;

public class Utils {

    static Set<BlankNode> getBNodes(Collection<Triple> s) {
        Set<BlankNode> result = new HashSet<BlankNode>();
        for (Triple triple : s) {
            if (triple.getSubject() instanceof BlankNode) {
                result.add((BlankNode) triple.getSubject());
            }
            if (triple.getObject() instanceof BlankNode) {
                result.add((BlankNode) triple.getObject());
            }
        }
        return result;
    }

    /**
     * removes the common grounded triples from s1 and s2. returns false if
     * a grounded triple is not in both sets, true otherwise
     */
    static boolean removeGrounded(Collection<Triple> s1, Collection<Triple> s2) {
        Iterator<Triple> triplesIter = s1.iterator();
        while (triplesIter.hasNext()) {
            Triple triple = triplesIter.next();
            if (!isGrounded(triple)) {
                continue;
            }
            if (!s2.remove(triple)) {
                return false;
            }
            triplesIter.remove();
        }
        //for efficiency we might skip this (redefine method)
        for (Triple triple : s2) {
            if (isGrounded(triple)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isGrounded(Triple triple) {
        if (triple.getSubject() instanceof BlankNode) {
            return false;
        }
        if (triple.getObject() instanceof BlankNode) {
            return false;
        }
        return true;
    }

}
