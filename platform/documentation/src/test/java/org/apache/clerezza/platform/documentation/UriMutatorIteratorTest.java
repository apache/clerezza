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
package org.apache.clerezza.platform.documentation;

import java.util.Iterator;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.commons.rdf.Literal;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.commons.rdf.impl.utils.TypedLiteralImpl;

/**
 *
 * @author mir
 */
public class UriMutatorIteratorTest {

    private static final String HOST = "http://localhost:8282/";

    private static String ORIGIN_BUNDLE_NAME = "my.symbolic.name";

    private static String REFERENCED_BUNDLE_NAME = "your.symbolic.name";

    @Test
    public void testMutator() {
        Graph mGraph = new SimpleGraph();
        IRI uriRef = new IRI(UriMutatorIterator.BASE_URI_PLACEHOLDER +
                REFERENCED_BUNDLE_NAME + "/bla#Test");
        IRI expectedIRI = new IRI(HOST + 
                "bundle-doc/"+ REFERENCED_BUNDLE_NAME +"/bla#Test");
        mGraph.add(new TripleImpl(uriRef, uriRef, uriRef));
        Iterator<Triple> it = new UriMutatorIterator(mGraph.iterator(), HOST,
                ORIGIN_BUNDLE_NAME);
        Triple expectedTriple = new TripleImpl(expectedIRI, expectedIRI,
                expectedIRI);
        Assert.assertEquals(expectedTriple, it.next());        
    }

    @Test
    public void testMutatorNoSymbolicName() {
        Graph mGraph = new SimpleGraph();
        IRI uriRef = new IRI(UriMutatorIterator.BASE_URI_PLACEHOLDER +
                "/bla#Test");
        IRI expectedIRI = new IRI(HOST +
                "bundle-doc/"+ ORIGIN_BUNDLE_NAME +"/bla#Test");
        mGraph.add(new TripleImpl(uriRef, uriRef, uriRef));
        Iterator<Triple> it = new UriMutatorIterator(mGraph.iterator(), HOST,
                ORIGIN_BUNDLE_NAME);
        Triple expectedTriple = new TripleImpl(expectedIRI, expectedIRI,
                expectedIRI);
        Assert.assertEquals(expectedTriple, it.next());
    }

    @Test
    public void baseUriTransformation() {
        Graph mGraph = new SimpleGraph();
        String xml = "<a href=\"" + UriMutatorIterator.BASE_URI_PLACEHOLDER +
            REFERENCED_BUNDLE_NAME + "/bla\"/>";
        Literal literal = new TypedLiteralImpl(xml,
                    UriMutatorIterator.XML_LITERAL);
        String expectedXml = "<a href=\"" + HOST + 
            "bundle-doc/"+ REFERENCED_BUNDLE_NAME +"/bla\"/>";
        Literal expectedLiteral = new TypedLiteralImpl(expectedXml,
                    UriMutatorIterator.XML_LITERAL);
        IRI uriRef = new IRI("bla");
            mGraph.add(new TripleImpl(uriRef, uriRef, literal));
        Iterator<Triple> it = new UriMutatorIterator(mGraph.iterator(), HOST,
                ORIGIN_BUNDLE_NAME);
        Triple expectedTriple = new TripleImpl(uriRef, uriRef,
                expectedLiteral);
        Assert.assertEquals(expectedTriple, it.next());
    }

    @Test
    public void baseUriTransformationNoSymbolicName() {
        Graph mGraph = new SimpleGraph();
        String xml = "<a href=\"" + UriMutatorIterator.BASE_URI_PLACEHOLDER +
            "/bla\"/>";
        Literal literal = new TypedLiteralImpl(xml,
                    UriMutatorIterator.XML_LITERAL);
        String expectedXml = "<a href=\"" + HOST +
            "bundle-doc/"+ ORIGIN_BUNDLE_NAME +"/bla\"/>";
        Literal expectedLiteral = new TypedLiteralImpl(expectedXml,
                    UriMutatorIterator.XML_LITERAL);
        IRI uriRef = new IRI("bla");
            mGraph.add(new TripleImpl(uriRef, uriRef, literal));
        Iterator<Triple> it = new UriMutatorIterator(mGraph.iterator(), HOST,
                ORIGIN_BUNDLE_NAME);
        Triple expectedTriple = new TripleImpl(uriRef, uriRef,
                expectedLiteral);
        Assert.assertEquals(expectedTriple, it.next());
    }
}
