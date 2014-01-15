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

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
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
	static VirtuosoWeightedProvider wp ;
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
	public void xsdString() throws ClassNotFoundException, SQLException{
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		log.info("Text an xsd:string");
		TypedLiteral object = new TypedLiteralImpl("lorem ipsum", new UriRef(XSD + "string"));
		UriRef subject = new UriRef("urn:io-test:reto");
		UriRef predicate = new UriRef("urn:io-test:hasText");
		
		Triple read = writeAndRead(subject, predicate, object);
		Assert.assertTrue(read.equals(new TripleImpl(subject, predicate, object)));
		Assert.assertTrue(read.getObject() instanceof TypedLiteral);
		TypedLiteral l = (TypedLiteral) read.getObject();
		Assert.assertEquals(l.getDataType(), new UriRef(XSD + "string"));
		
	}
	
	@Test
	public void longString() throws ClassNotFoundException, SQLException{
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		log.info("Test a long xsd:string");	
		StringBuilder longStr = new StringBuilder();
		int c = 1000;
		while (c>0){
			longStr.append(" another piece of string ");
			c--;
		}
		int size = longStr.length();
		TypedLiteral object = new TypedLiteralImpl(longStr.toString(), new UriRef(XSD + "string"));
		UriRef subject = new UriRef("urn:io-test:reto");
		UriRef predicate = new UriRef("urn:io-test:hasText");
		Triple read = writeAndRead(subject, predicate, object);
		Assert.assertTrue(read.equals(new TripleImpl(subject, predicate, object)));
		Assert.assertTrue(read.getObject() instanceof TypedLiteral);
		TypedLiteral l = (TypedLiteral) read.getObject();
		Assert.assertEquals(l.getDataType(), new UriRef(XSD + "string"));
		Assert.assertTrue(l.getLexicalForm().length() == size);
	}
	
	private Triple writeAndRead(NonLiteral subject, UriRef predicate, Resource object) throws ClassNotFoundException, SQLException{
		VirtuosoMGraph graph = new VirtuosoMGraph(TEST_GRAPH_NAME, TestUtils.getProvider());
		Triple t = new TripleImpl(subject, predicate, object);
		graph.add(t);
		Triple read = graph.getGraph().iterator().next();
		
		return read;
	}
	
	@Test
	public void subjectAsUriTest() throws ClassNotFoundException, SQLException{
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
		Assert.assertEquals(read.getSubject(), new UriRef("urn:io-test:enridaga"));
		Assert.assertNotSame(read.getSubject(), new UriRef("urn:io-test:alexdma"));
		Assert.assertEquals(read, new TripleImpl(subject, predicate, object));
		
	}
	
	@Test
	public void subjectAsBnodeTest() throws ClassNotFoundException, SQLException{
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
		Assert.assertNotSame(read.getSubject(), new UriRef("urn:io-test:enridaga"));
		Assert.assertNotSame(read.getSubject(), new UriRef("urn:io-test:alexdma"));
		// bnodes cannot be equals!
		Assert.assertNotSame(read, new TripleImpl(subject, predicate, object));
	}
	
	@Test
	public void objectAsUriTest() throws ClassNotFoundException, SQLException{
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
		Assert.assertNotSame(read.getSubject(), new UriRef("urn:io-test:alexdma"));
		Assert.assertEquals(read, new TripleImpl(subject, predicate, object));
	}
	

	@Test
	public void objectAsBnodeTest() throws ClassNotFoundException, SQLException{
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
		Assert.assertNotSame(read.getObject(), object);
		Assert.assertNotSame(read.getObject(), new UriRef("urn:io-test:enridaga"));
		Assert.assertNotSame(read.getObject(), new UriRef("urn:io-test:alexdma"));
		// bnodes cannot be equals!
		Assert.assertNotSame(read, new TripleImpl(subject, predicate, object));
	}
}
