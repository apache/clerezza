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
package org.apache.clerezza.rdf.virtuoso.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.apache.clerezza.rdf.virtuoso.storage.VirtuosoMGraph;
import org.apache.clerezza.rdf.virtuoso.storage.access.DataAccess;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jdbc4.VirtuosoException;

public class VirtuosoMGraphTest {
	static VirtuosoMGraph mgraph = null;
	static DataAccess da = null;
	static final String TEST_GRAPH_NAME = "VirtuosoMGraphTest";

	static Logger log = LoggerFactory.getLogger(VirtuosoMGraphTest.class);
	
	@BeforeClass
	public static void assume(){
		org.junit.Assume.assumeTrue(!TestUtils.SKIP);
	}
	
	/**
	 * Clean before any test
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	@Before
	public void before() throws ClassNotFoundException, SQLException {
		
		da = TestUtils.getProvider().createDataAccess();
		mgraph = new VirtuosoMGraph(TEST_GRAPH_NAME, da);
		mgraph.clear();
		log.debug("Clearing graph <{}>", TEST_GRAPH_NAME);
	}

	final UriRef enridaga = new UriRef("enridaga");
	final UriRef alexdma = new UriRef("alexdma");
	final UriRef anuzzolese = new UriRef("anuzzolese");
	final UriRef predicate = new UriRef("http://property/name");
	final PlainLiteral object = new PlainLiteralImpl("Enrico Daga", new Language("it"));
	final TypedLiteral objectTyped = new TypedLiteralImpl("Enrico Daga", new UriRef("http://www.w3.org/2001/XMLSchema#string"));
	final TypedLiteral objectXml = new TypedLiteralImpl("<div>Enrico Daga</div>" , 
			new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral"));
	final UriRef knows = new UriRef(TestUtils.FOAF_NS + "knows");

	@Test
	public void testAddSingle() {
		log.info("testAddSingle()");
		Triple triple = new Triple() {

			@Override
			public NonLiteral getSubject() {
				return enridaga;
			}

			@Override
			public UriRef getPredicate() {
				return predicate;
			}

			@Override
			public Resource getObject() {
				return object;
			}
		};
		boolean success = mgraph.add(triple);
		assertTrue(success);
		assertTrue(mgraph.size() == 1);
		assertTrue(mgraph.filter(enridaga, predicate, object).hasNext());
		assertTrue(mgraph.filter(enridaga, predicate, object).next().equals(triple));
	}
	
	@Test
	public void testAddSingleTypedLiteral() {
		log.info("testAddSingleTypedLiteral()");
		Triple triple = new Triple() {

			@Override
			public NonLiteral getSubject() {
				return enridaga;
			}

			@Override
			public UriRef getPredicate() {
				return predicate;
			}

			@Override
			public Resource getObject() {
				return objectTyped;
			}
		};
		boolean success = mgraph.add(triple);
		assertTrue(success);
		assertTrue(mgraph.size() == 1);
		assertTrue(mgraph.filter(enridaga, predicate, objectTyped).hasNext());
		assertTrue(mgraph.filter(enridaga, predicate, objectTyped).next().equals(triple));
	}

//	@Ignore
	@Test
	public void testAddSingleXMLLiteral() {
		log.info("testAddSingleXMLLiteral()");
		Triple triple = new Triple() {

			@Override
			public NonLiteral getSubject() {
				return enridaga;
			}

			@Override
			public UriRef getPredicate() {
				return predicate;
			}

			@Override
			public Resource getObject() {
				return objectXml;
			}
		};
		boolean success = mgraph.add(triple);
		assertTrue(success);
		assertTrue(mgraph.size() == 1);
		Triple rt = mgraph.filter(enridaga, predicate, null).next();
		log.info(" > s: {} ", rt.getSubject());
		log.info(" > p: {} ", rt.getPredicate());
		log.info(" > o: {} ", rt.getObject());
		log.info(" > tl?: {} ", rt.getObject() instanceof TypedLiteral);
		assertTrue(mgraph.filter(enridaga, predicate, objectXml).hasNext());
		Triple tr = mgraph.filter(enridaga, predicate, objectXml).next();
		log.info("!! {} {} {} !!", new Object[]{tr.getSubject(), tr.getPredicate(), tr.getObject()});
		assertTrue(tr.equals(triple));
	}


	@Test
	public void testFilter() {
		log.info("testFilter(); Test filter(s,p,o)");
		// We use testAdd to prepare this
		testAddSingle();
		
		Iterator<Triple> its = mgraph.filter(null, null, null);
		while (its.hasNext()) {
			Triple t = its.next();
			log.info("Found --- triple: {}", t);
			log.info("Found --- s: {} {}", t.getSubject(), t.getSubject().getClass());
			log.info("Found --- p: {} {}", t.getPredicate(), t.getPredicate().getClass());
			log.info("Found --- o: {} {}", t.getObject(), t.getObject().getClass());
			assertEquals(t.getSubject(), enridaga);
			assertEquals(t.getPredicate(), predicate);
			assertEquals(t.getObject(), object);
		}

		Iterator<Triple> it = mgraph.filter(enridaga, predicate, object);
		boolean found = false;
		while (it.hasNext()) {
			found = true;
			Triple t = it.next();
			log.debug("Found matching triple: {}", t);
			assertEquals(t.getSubject(), enridaga);
			assertEquals(t.getPredicate(), predicate);
			assertEquals(t.getObject(), object);
		}
		assertTrue(found);
	}

	@Test
	public void testFilterSubject() {
		log.info("testFilterSubject(); Test filter(s,null,null)");
		// We use testAdd to prepare this
		testAddSingle();
		Iterator<Triple> it = mgraph.filter(enridaga, null, null);
		boolean found = false;
		while (it.hasNext()) {
			found = true;
			Triple t = it.next();
			if (log.isDebugEnabled()) {
				log.debug("Found matching triple: {}", t);
				TestUtils.stamp(t);
			}
			assertEquals(t.getSubject(), enridaga);
		}
		assertTrue(found);
	}


	@Test
	public void testFilterSubjectBnode() throws VirtuosoException, SQLException, ClassNotFoundException {
		log.info("testFilterSubjectBnode(); Test filter(s,null,null)");
		final BNode bn = new BNode();
		// We use testAdd to prepare this
		Triple triple = new Triple() {

			@Override
			public NonLiteral getSubject() {
				return bn;
			}

			@Override
			public UriRef getPredicate() {
				return predicate;
			}

			@Override
			public Resource getObject() {
				return new BNode();
			}
		};

		boolean success = mgraph.add(triple);
		assertTrue(success);
		Iterator<Triple> it = mgraph.filter(bn, predicate, null);
		boolean found = false;
		Triple t = null; // we will use it to make a further query
		while (it.hasNext()) {
			found = true;
			 t = it.next();
			if (log.isDebugEnabled()) {
				log.debug("Found matching triple: {}", t);
				TestUtils.stamp(t);
			}
			assertEquals(t.getPredicate(), predicate);
		}
		assertTrue(found);
		
		assertNotNull(t);
		
		log.info("{}",t.getSubject());
		it = mgraph.filter(t.getSubject(), predicate, t.getObject());
		found = false;
		while (it.hasNext()) {
			found = true;
			 t = it.next();
			if (log.isDebugEnabled()) {
				log.debug("Found matching triple: {}", t);
				TestUtils.stamp(t);
			}
			assertEquals(t.getPredicate(), predicate);
		}
		assertTrue(found);
	}

	@Test
	public void testFilterPredicate() {
		log.info("testFilterPredicate(); Test filter(null,p,null)");
		// We use testAdd to prepare this
		testAddSingle();
		Iterator<Triple> it = mgraph.filter(null, predicate, null);
		boolean found = false;
		while (it.hasNext()) {
			found = true;
			Triple t = it.next();
			if (log.isDebugEnabled()) {
				log.debug("Found matching triple: {}", t);
				TestUtils.stamp(t);
			}
			assertEquals(t.getPredicate(), predicate);
		}
		assertTrue(found);
	}

	@Test
	public void testFilterObject() {
		log.info("testFilterObject(); Test filter(null,null,o)");
		// We use testAdd to prepare this
		testAddSingle();
		Iterator<Triple> it = mgraph.filter(null, null, object);
		boolean found = false;
		while (it.hasNext()) {
			found = true;
			Triple t = it.next();
			if (log.isDebugEnabled()) {
				log.debug("Found matching triple: {}", t);
				TestUtils.stamp(t);
			}
			assertEquals(t.getObject(), object);
		}
		assertTrue(found);
	}

	@Test
	public void testFilterObjectTyped() {
		log.info("testFilterObjectTyped(); Test filter(null,null,o)");
		// We use testAdd to prepare this
		testAddSingleTypedLiteral();
		Iterator<Triple> it = mgraph.filter(null, null, objectTyped);
		boolean found = false;
		while (it.hasNext()) {
			found = true;
			Triple t = it.next();
			if (log.isDebugEnabled()) {
				log.debug("Found matching triple: {}", t);
				TestUtils.stamp(t);
			}
			assertEquals(t.getObject(), objectTyped);
		}
		assertTrue(found);
	}

//	@Ignore
	@Test
	public void testFilterObjectXml() {
		log.info("testFilterObjectXml(); Test filter(null,null,o)");
		// We use testAdd to prepare this
		testAddSingleXMLLiteral();
		Iterator<Triple> it = mgraph.filter(null, null, objectXml);
		boolean found = false;
		while (it.hasNext()) {
			found = true;
			Triple t = it.next();
			if (log.isDebugEnabled()) {
				log.debug("Found matching triple: {}", t);
				TestUtils.stamp(t);
			}
			assertEquals(t.getObject(), objectXml);
		}
		assertTrue(found);
	}

	@Test
	public void testSize() {
		log.info("testSize()");
		// We use testAdd to prepare this
		testAddSingle();
		// Should be 1 at this time
		log.debug("How many triples are in graph <{}>? {}", TEST_GRAPH_NAME,
				mgraph.size());
		assertTrue(mgraph.size() > 0);
	}

	@Test
	public void testIncreaseSize() {
		log.info("testIncreaseSize()");
		int beforeSize = mgraph.size();
		Triple t = new Triple() {

			@Override
			public NonLiteral getSubject() {
				return enridaga;
			}

			@Override
			public UriRef getPredicate() {
				return knows;
			}

			@Override
			public Resource getObject() {
				return alexdma;
			}
		};
		assertTrue(mgraph.add(t));
		int afterSize = mgraph.size();
		assertEquals(beforeSize + 1, afterSize);
	}

	@Test
	public void testAddRemoveSize() {
		log.info("testAddRemoveSize()");
		int beforeSize = mgraph.size();
		Triple t = new Triple() {

			@Override
			public NonLiteral getSubject() {
				return enridaga;
			}

			@Override
			public UriRef getPredicate() {
				return knows;
			}

			@Override
			public Resource getObject() {
				return alexdma;
			}
		};
		assertTrue(mgraph.add(t));
		assertTrue(mgraph.remove(t));
		int afterSize = mgraph.size();
		assertEquals(beforeSize, afterSize);
	}

	@Test
	public void testGetGraphReadOnly() {
		log.info("testGetGraphReadOnly()");
		Graph g = mgraph.getGraph();
		Triple t = new Triple() {

			@Override
			public NonLiteral getSubject() {
				return enridaga;
			}

			@Override
			public UriRef getPredicate() {
				return knows;
			}

			@Override
			public Resource getObject() {
				return anuzzolese;
			}
		};
		// This should not be allowed
		boolean success;
		try {
			success = g.add(t);
		} catch (UnsupportedOperationException e) {
			success = false;
		}
		assertFalse(success);
	}

	@Test
	public void testContains() {
		log.info("testContains()");
		Triple t = new Triple() {

			@Override
			public NonLiteral getSubject() {
				return enridaga;
			}

			@Override
			public UriRef getPredicate() {
				return knows;
			}

			@Override
			public Resource getObject() {
				return anuzzolese;
			}
		};
		boolean addWorks = mgraph.add(t);
		assertTrue(addWorks);

		// This second triple is equivalent
		Triple t2 = new Triple() {

			@Override
			public NonLiteral getSubject() {
				return enridaga;
			}

			@Override
			public UriRef getPredicate() {
				return knows;
			}

			@Override
			public Resource getObject() {
				return anuzzolese;
			}
		};
		Iterator<Triple> it = mgraph.filter(t2.getSubject(), t2.getPredicate(),
				t2.getObject());
		while (it.hasNext()) {
			if (log.isDebugEnabled()) {
				log.debug("Found matching triple");
				TestUtils.stamp(it.next());
			}else{
				it.next();
			}
		}
		assertTrue(mgraph.contains(t2));
		// Also the related read-only graph
		assertTrue(mgraph.getGraph().contains(t2));
	}

	@After
	public void clear() throws VirtuosoException, ClassNotFoundException,
			SQLException {
		log.info("Clearing graph <{}> of size {}", TEST_GRAPH_NAME,
				mgraph.size());
		log.debug("Removing graph <{}>", TEST_GRAPH_NAME);
		da.close();
		da = null;
		mgraph = null;
		Statement st = TestUtils.getConnection().createStatement();
		st.execute("SPARQL CLEAR GRAPH <" + TEST_GRAPH_NAME + ">");
		st.close();
	}
}
