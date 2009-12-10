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
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;

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
		MGraph mGraph = new SimpleMGraph();
		UriRef uriRef = new UriRef(UriMutatorIterator.BASE_URI_PLACEHOLDER +
				REFERENCED_BUNDLE_NAME + "/bla#Test");
		UriRef expectedUriRef = new UriRef(HOST + 
				"bundle-doc/"+ REFERENCED_BUNDLE_NAME +"/bla#Test");
		mGraph.add(new TripleImpl(uriRef, uriRef, uriRef));
		Iterator<Triple> it = new UriMutatorIterator(mGraph.iterator(), HOST,
				ORIGIN_BUNDLE_NAME);
		Triple expectedTriple = new TripleImpl(expectedUriRef, expectedUriRef,
				expectedUriRef);
		Assert.assertEquals(expectedTriple, it.next());		
	}

	@Test
	public void testMutatorNoSymbolicName() {
		MGraph mGraph = new SimpleMGraph();
		UriRef uriRef = new UriRef(UriMutatorIterator.BASE_URI_PLACEHOLDER +
				"/bla#Test");
		UriRef expectedUriRef = new UriRef(HOST +
				"bundle-doc/"+ ORIGIN_BUNDLE_NAME +"/bla#Test");
		mGraph.add(new TripleImpl(uriRef, uriRef, uriRef));
		Iterator<Triple> it = new UriMutatorIterator(mGraph.iterator(), HOST,
				ORIGIN_BUNDLE_NAME);
		Triple expectedTriple = new TripleImpl(expectedUriRef, expectedUriRef,
				expectedUriRef);
		Assert.assertEquals(expectedTriple, it.next());
	}

	@Test
	public void baseUriTransformation() {
		MGraph mGraph = new SimpleMGraph();
		String xml = "<a href=\"" + UriMutatorIterator.BASE_URI_PLACEHOLDER +
			REFERENCED_BUNDLE_NAME + "/bla\"/>";
		Literal literal = new TypedLiteralImpl(xml,
					UriMutatorIterator.XML_LITERAL);
		String expectedXml = "<a href=\"" + HOST + 
			"bundle-doc/"+ REFERENCED_BUNDLE_NAME +"/bla\"/>";
		Literal expectedLiteral = new TypedLiteralImpl(expectedXml,
					UriMutatorIterator.XML_LITERAL);
		UriRef uriRef = new UriRef("bla");
			mGraph.add(new TripleImpl(uriRef, uriRef, literal));
		Iterator<Triple> it = new UriMutatorIterator(mGraph.iterator(), HOST,
				ORIGIN_BUNDLE_NAME);
		Triple expectedTriple = new TripleImpl(uriRef, uriRef,
				expectedLiteral);
		Assert.assertEquals(expectedTriple, it.next());
	}

	@Test
	public void baseUriTransformationNoSymbolicName() {
		MGraph mGraph = new SimpleMGraph();
		String xml = "<a href=\"" + UriMutatorIterator.BASE_URI_PLACEHOLDER +
			"/bla\"/>";
		Literal literal = new TypedLiteralImpl(xml,
					UriMutatorIterator.XML_LITERAL);
		String expectedXml = "<a href=\"" + HOST +
			"bundle-doc/"+ ORIGIN_BUNDLE_NAME +"/bla\"/>";
		Literal expectedLiteral = new TypedLiteralImpl(expectedXml,
					UriMutatorIterator.XML_LITERAL);
		UriRef uriRef = new UriRef("bla");
			mGraph.add(new TripleImpl(uriRef, uriRef, literal));
		Iterator<Triple> it = new UriMutatorIterator(mGraph.iterator(), HOST,
				ORIGIN_BUNDLE_NAME);
		Triple expectedTriple = new TripleImpl(uriRef, uriRef,
				expectedLiteral);
		Assert.assertEquals(expectedTriple, it.next());
	}
}
