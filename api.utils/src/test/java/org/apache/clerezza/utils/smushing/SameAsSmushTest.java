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
package org.apache.clerezza.utils.smushing;

import org.apache.clerezza.Graph;
import org.apache.clerezza.IRI;
import org.apache.clerezza.Literal;
import org.apache.clerezza.Triple;
import org.apache.clerezza.implementation.TripleImpl;
import org.apache.clerezza.implementation.in_memory.SimpleGraph;
import org.apache.clerezza.implementation.literal.PlainLiteralImpl;
import org.apache.clerezza.ontologies.FOAF;
import org.apache.clerezza.ontologies.OWL;
import org.apache.clerezza.ontologies.RDFS;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Iterator;
import java.util.Set;

/**
 * @author reto
 */
@RunWith(JUnitPlatform.class)
public class SameAsSmushTest {

    private final IRI uriA = new IRI("http://example.org/A");
    private final IRI uriB = new IRI("http://example.org/B");
    private final IRI uriC = new IRI("http://example.org/C");

    private final Literal lit = new PlainLiteralImpl("That's me (and you)");

    private Graph sameAsStatements = new SimpleGraph();

    {
        sameAsStatements.add(new TripleImpl(uriA, OWL.sameAs, uriB));
    }

    private Graph dataGraph = new SimpleGraph();

    {
        dataGraph.add(new TripleImpl(uriA, FOAF.knows, uriB));
        dataGraph.add(new TripleImpl(uriB, RDFS.label, lit));
        dataGraph.add(new TripleImpl(uriA, RDFS.label, lit));
    }

    @Test
    public void simple() {
        SameAsSmusher smusher = new SameAsSmusher() {

            @Override
            protected IRI getPreferedIRI(Set<IRI> uriRefs) {
                if (!uriRefs.contains(uriA)) throw new RuntimeException("not the set we excpect");
                if (!uriRefs.contains(uriB)) throw new RuntimeException("not the set we excpect");
                return uriC;
            }

        };
        Assertions.assertEquals(3, dataGraph.size());
        smusher.smush(dataGraph, sameAsStatements, true);
        Assertions.assertEquals(4, dataGraph.size());
        Assertions.assertTrue(dataGraph.filter(null, OWL.sameAs, null).hasNext());
        //exactly one statement with literal 
        Iterator<Triple> litStmts = dataGraph.filter(null, null, lit);
        Assertions.assertTrue(litStmts.hasNext());
        Triple litStmt = litStmts.next();
        Assertions.assertFalse(litStmts.hasNext());
        Iterator<Triple> knowsStmts = dataGraph.filter(null, FOAF.knows, null);
        Assertions.assertTrue(knowsStmts.hasNext());
        Triple knowStmt = knowsStmts.next();
        Assertions.assertEquals(knowStmt.getSubject(), knowStmt.getObject());
        Assertions.assertEquals(litStmt.getSubject(), knowStmt.getObject());
        Assertions.assertEquals(litStmt.getSubject(), dataGraph.filter(null, OWL.sameAs, null).next().getObject());
        Assertions.assertEquals(knowStmt.getSubject(), uriC);
    }
}
