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
package rdf.virtuoso.storage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.jena.parser.JenaParserProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rdf.virtuoso.storage.access.VirtuosoWeightedProvider;
import virtuoso.jdbc4.VirtuosoException;

public class RdfIOTest {
	static VirtuosoMGraph mgraph = null;
	static final String TEST_GRAPH_NAME = "RdfIOTest";
	static final String XSD = "http://www.w3.org/2001/XMLSchema#";
	static Logger log = LoggerFactory.getLogger(RdfIOTest.class);
	static VirtuosoWeightedProvider wp;

	/**
	 * Clean before any test
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	@Before
	public void before() throws ClassNotFoundException, SQLException {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		wp = TestUtils.getProvider();
		mgraph = new VirtuosoMGraph(TEST_GRAPH_NAME, wp);
		mgraph.clear();
		log.debug("Clearing graph <{}>", TEST_GRAPH_NAME);
	}

	/**
	 * Clean after a test
	 * 
	 * @throws VirtuosoException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	@After
	public void clear() throws VirtuosoException, ClassNotFoundException,
			SQLException {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		log.debug("Clearing graph <{}> of size {}", TEST_GRAPH_NAME,
				mgraph.size());
		log.debug("Removing graph <{}>", TEST_GRAPH_NAME);
		Statement st = TestUtils.getConnection().createStatement();
		st.execute("SPARQL CLEAR GRAPH <" + TEST_GRAPH_NAME + ">");
	}

	@Test
	public void xsdString() throws ClassNotFoundException, SQLException {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		log.info("Text an xsd:string");
		TypedLiteral object = new TypedLiteralImpl("lorem ipsum", new UriRef(
				XSD + "string"));
		UriRef subject = new UriRef("urn:io-test:reto");
		UriRef predicate = new UriRef("urn:io-test:hasText");

		Triple read = writeAndRead(subject, predicate, object);
		Assert.assertTrue(read
				.equals(new TripleImpl(subject, predicate, object)));
		Assert.assertTrue(read.getObject() instanceof TypedLiteral);
		TypedLiteral l = (TypedLiteral) read.getObject();
		Assert.assertEquals(l.getLexicalForm(), "lorem ipsum");
		Assert.assertEquals(l.getDataType(), new UriRef(XSD + "string"));

	}

	@Test
	public void longString() throws ClassNotFoundException, SQLException {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		log.info("Test a long xsd:string");
		StringBuilder longStr = new StringBuilder();
		int c = 1000;
		while (c > 0) {
			longStr.append(" another piece of string ");
			c--;
		}
		int size = longStr.length();
		TypedLiteral object = new TypedLiteralImpl(longStr.toString(),
				new UriRef(XSD + "string"));
		UriRef subject = new UriRef("urn:io-test:reto");
		UriRef predicate = new UriRef("urn:io-test:hasText");
		Triple read = writeAndRead(subject, predicate, object);
		Assert.assertTrue(read
				.equals(new TripleImpl(subject, predicate, object)));
		Assert.assertTrue(read.getObject() instanceof TypedLiteral);
		TypedLiteral l = (TypedLiteral) read.getObject();
		Assert.assertEquals(l.getDataType(), new UriRef(XSD + "string"));
		Assert.assertTrue(l.getLexicalForm().length() == size);
	}

	private Triple writeAndRead(NonLiteral subject, UriRef predicate,
			Resource object) throws ClassNotFoundException, SQLException {
		VirtuosoMGraph graph = new VirtuosoMGraph(TEST_GRAPH_NAME,
				TestUtils.getProvider());
		Triple t = new TripleImpl(subject, predicate, object);
		graph.add(t);
		Triple read = graph.getGraph().iterator().next();
		return read;
	}

	@Test
	public void subjectAsUriTest() throws ClassNotFoundException, SQLException {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		log.info("Test subject as UriRef");

		NonLiteral subject;
		UriRef predicate = new UriRef("urn:io-test:predicate");
		UriRef object = new UriRef("urn:io-test:object");

		// subject may be UriRef
		subject = new UriRef("urn:io-test:enridaga");
		Triple read = writeAndRead(subject, predicate, object);
		Assert.assertTrue(read.getSubject().equals(subject));
		Assert.assertEquals(read.getSubject(), new UriRef(
				"urn:io-test:enridaga"));
		Assert.assertNotSame(read.getSubject(), new UriRef(
				"urn:io-test:alexdma"));
		Assert.assertEquals(read, new TripleImpl(subject, predicate, object));

	}

	@Test
	public void subjectAsBnodeTest() throws ClassNotFoundException,
			SQLException {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		log.info("Test subject as BNode");

		NonLiteral subject;
		UriRef predicate = new UriRef("urn:io-test:predicate");
		UriRef object = new UriRef("urn:io-test:object");

		// subject may be BNode
		subject = new BNode();
		Triple read = writeAndRead(subject, predicate, object);
		// bnodes cannot be equals!
		Assert.assertFalse(read.getSubject().equals(subject));
		Assert.assertTrue(read.getSubject() instanceof BNode);
		Assert.assertNotSame(read.getSubject(), new UriRef(
				"urn:io-test:enridaga"));
		Assert.assertNotSame(read.getSubject(), new UriRef(
				"urn:io-test:alexdma"));
		// bnodes cannot be equals!
		Assert.assertNotSame(read, new TripleImpl(subject, predicate, object));
	}

	@Test
	public void objectAsUriTest() throws ClassNotFoundException, SQLException {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		log.info("Test object as UriRef");

		NonLiteral subject = new UriRef("urn:io-test:enridaga");
		UriRef predicate = new UriRef("urn:io-test:predicate");
		UriRef object = new UriRef("urn:io-test:object");

		Triple read = writeAndRead(subject, predicate, object);
		//
		Assert.assertTrue(read.getObject().equals(object));
		Assert.assertTrue(read.getObject() instanceof UriRef);
		Assert.assertEquals(read.getObject(), new UriRef("urn:io-test:object"));
		Assert.assertNotSame(read.getSubject(), new UriRef(
				"urn:io-test:alexdma"));
		Assert.assertEquals(read, new TripleImpl(subject, predicate, object));
	}

	@Test
	public void objectAsBnodeTest() throws ClassNotFoundException, SQLException {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		log.info("Test object as Bnode");

		NonLiteral subject = new UriRef("urn:io-test:subject");
		UriRef predicate = new UriRef("urn:io-test:predicate");
		Resource object;

		// subject may be BNode
		object = new BNode();
		Triple read = writeAndRead(subject, predicate, object);
		// bnodes cannot be equals!
		Assert.assertFalse(read.getObject().equals(object));
		Assert.assertTrue(read.getSubject().equals(subject));
		
		Assert.assertTrue(read.getObject() instanceof BNode);
		Assert.assertTrue(read.getObject() instanceof VirtuosoBNode);
		
		Assert.assertNotSame(read.getObject(), new UriRef(
				"urn:io-test:enridaga"));
		Assert.assertNotSame(read.getObject(),
				new UriRef("urn:io-test:alexdma"));
		// these bnodes cannot be equals!
		Assert.assertNotSame(read, new TripleImpl(subject, predicate, object));
	}

	@Test
	public void bnodesTest() throws ClassNotFoundException, SQLException {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		log.info("Test iterations and filter with bnode");

		NonLiteral s1 = new BNode();
		NonLiteral s2 = new BNode();
		NonLiteral s3 = new BNode();
		NonLiteral s4 = new BNode();

		UriRef p1 = new UriRef("p1");
		UriRef p2 = new UriRef("p2");
		UriRef p3 = new UriRef("p3");

		VirtuosoMGraph graph = new VirtuosoMGraph(TEST_GRAPH_NAME,
				TestUtils.getProvider());

		graph.add(new TripleImpl(s1, p1, s2));
		// Get the bnode of s1
		Triple first = graph.filter(null, p1, null).next();

		Assert.assertTrue(first.getSubject() instanceof VirtuosoBNode);
		Assert.assertTrue(first.getObject() instanceof VirtuosoBNode);
		
		BNode s1intern = (BNode) first.getSubject();
		BNode s2intern = (BNode) first.getObject();
		
		graph.add(new TripleImpl(s2intern, p1, s3));
		Triple second = graph.filter(s2intern, p1, null).next();
		Assert.assertTrue(second.getObject() instanceof VirtuosoBNode);
		
		graph.add(new TripleImpl(s1intern, p2, s4));
		Triple third = graph.filter(s1intern, p2, null).next();
		Assert.assertTrue(third.getObject() instanceof VirtuosoBNode);
		BNode s4intern = (BNode) third.getObject();
		
		graph.add(new TripleImpl(s1intern, p2, s4intern));
		graph.add(new TripleImpl(s4intern, p3, s1intern));

		Iterator<Triple> all = graph.iterator();
		while(all.hasNext()){
			Triple l = all.next();
			log.info("{} {} {}",new Object[]{ l.getSubject(), l.getPredicate(), l.getObject()});
		}
		Iterator<Triple> i = graph.filter(null, p2, null);
		int n = 0;
		while (i.hasNext()) {
			n++;
			Triple s1t = i.next();
			Iterator<Triple> s1i = graph.filter(s1t.getSubject(), null, null);
			boolean found = false;
			while (s1i.hasNext()) {
				Triple s1it = s1i.next();
				found = true;
				log.info("{} {}",s1it.getSubject(), s1t.getSubject());
				Assert.assertTrue(s1it.getSubject().equals(s1t.getSubject()));
				Assert.assertTrue(s1it.getPredicate().equals(p1)
						|| s1it.getPredicate().equals(p2));

			}
			Assert.assertTrue(found);
			Assert.assertTrue(s1t.getObject() instanceof VirtuosoBNode);
			Assert.assertTrue(s1t.getSubject() instanceof VirtuosoBNode);
			Iterator<Triple> s4i = graph.filter((NonLiteral) s1t.getObject(),
					null, null);
			log.info("s4 {} ",s1t.getObject());
			while (s4i.hasNext()) {
				Triple s4it = s4i.next();
				log.info("{} {}",s4it.getSubject(), s1t.getObject());
				Assert.assertTrue(s4it.getSubject().equals(s1t.getObject()));
				Assert.assertTrue(s4it.getPredicate().equals(p3));
			}
		}
		Assert.assertEquals(n, 1);

	}

	@Test
	public void sysconfigTest(){
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		
		SystemConfig sc = new SystemConfig();
		MGraph systemGraph = mgraph;
		URL config = sc.getClass().getResource(SystemConfig.CONFIG_FILE);
        if (config == null) {
            throw new RuntimeException("no config file found");
        }
        ParsingProvider parser = new JenaParserProvider();
        try {
            parser.parse(systemGraph, config.openStream(),
                    SupportedFormat.RDF_XML, null);
        } catch (IOException ex) {
            log.warn("Cannot parse coniguration at URL: {}", config);
            throw new RuntimeException(ex);
        }
	}
	
	@Test
	public void testUnicodeChars() throws ClassNotFoundException, SQLException {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		log.info("Text an xsd:string");
		
		String s = "lorem ipsum è é £ ò ç à ù β ät ü ä";
		TypedLiteral object = new TypedLiteralImpl(s, new UriRef(
				XSD + "string"));
		UriRef subject = new UriRef("urn:io-test:reto");
		UriRef predicate = new UriRef("urn:io-test:hasText");

		Triple read = writeAndRead(subject, predicate, object);
		log.info("o: {} :: {}", object, read.getObject());
		Assert.assertTrue(read
				.equals(new TripleImpl(subject, predicate, object)));
		Assert.assertTrue(read.getObject() instanceof TypedLiteral);
		TypedLiteral l = (TypedLiteral) read.getObject();
		Assert.assertEquals(l.getLexicalForm(), s);
		Assert.assertEquals(l.getDataType(), new UriRef(XSD + "string"));

	}
}
