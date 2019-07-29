/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor  license  agreements.  See the NOTICE file distributed
 * with this work  for  additional  information  regarding  copyright
 * ownership.  The ASF  licenses  this file to you under  the  Apache
 * License, Version 2.0 (the "License"); you may not  use  this  file
 * except in compliance with the License.  You may obtain  a copy  of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless  required  by  applicable law  or  agreed  to  in  writing,
 * software  distributed  under  the  License  is  distributed  on an
 * "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR  CONDITIONS  OF ANY KIND,
 * either  express  or implied.  See  the License  for  the  specific
 * language governing permissions and limitations under  the License.
 */

package org.apache.clerezza.implementation.graphmatching;

import org.apache.clerezza.BlankNode;
import org.apache.clerezza.BlankNodeOrIRI;
import org.apache.clerezza.Graph;
import org.apache.clerezza.IRI;
import org.apache.clerezza.implementation.TripleImpl;
import org.apache.clerezza.implementation.in_memory.SimpleMGraph;

/**
 * @author reto
 */
public class Utils4Testing {

    static Graph generateLine(int size, final BlankNodeOrIRI firstNode) {
        if (size < 1) {
            throw new IllegalArgumentException();
        }
        Graph result = new SimpleMGraph();
        BlankNodeOrIRI lastNode = firstNode;
        for (int i = 0; i < size; i++) {
            final BlankNode newNode = new BlankNode();
            result.add(new TripleImpl(lastNode, u1, newNode));
            lastNode = newNode;
        }
        return result;
    }

    final static IRI u1 = new IRI("http://example.org/u1");

}
