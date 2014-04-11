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
package org.apache.clerezza.rdf.virtuoso.storage.access;

import java.sql.SQLException;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.virtuoso.storage.TestUtils;
import org.apache.clerezza.rdf.virtuoso.storage.access.DataAccess;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DataAccessTest {

	private static DataAccess da = null;
	private final static String testGraphName = "urn:x-test:DataAccessTest";
	static Logger log = LoggerFactory.getLogger(DataAccessTest.class);
	
	@BeforeClass
	public static void assume(){
		org.junit.Assume.assumeTrue(!TestUtils.SKIP);
	}
	
	@Before
	public void before() throws ClassNotFoundException, SQLException {
		da = TestUtils.getProvider().createDataAccess();
		da.clearGraph( testGraphName );
	}

	@After
	public void after() {
		da.clearGraph( testGraphName );
		da.close();
		da = null;
	}

	private void testTriple(Triple t){
		String g = testGraphName;
		da.insertQuad(g, t);
		
		Assert.assertTrue(da.filter(g, null, null, null).hasNext());

		Assert.assertTrue(da.filter(g, t.getSubject(), null, null).hasNext());
		Assert.assertTrue(da.filter(g, null, t.getPredicate(), null).hasNext());
		Assert.assertTrue(da.filter(g, null, null, t.getObject()).hasNext());
		
		Assert.assertTrue(da.filter(g, null, t.getPredicate(), t.getObject()).hasNext());
		Assert.assertTrue(da.filter(g, t.getSubject(), null, t.getObject()).hasNext());
		Assert.assertTrue(da.filter(g, t.getSubject(), t.getPredicate(), null).hasNext());
		Assert.assertTrue(da.filter(g, t.getSubject(), null, t.getObject()).hasNext());

		Assert.assertTrue(da.filter(g, t.getSubject(), t.getPredicate(), t.getObject()).hasNext());

		Assert.assertTrue(da.size(g) == 1);
		
		da.deleteQuad(g, t);
		
		Assert.assertTrue(da.size(g) == 0);
	}

	@Test
	public void test_Uri_Uri_Uri(){
		Triple t = new TripleImpl(new UriRef("urn:subject"), new UriRef("urn:predicate"), new UriRef("urn:object"));
		testTriple(t);
	}

	@Test
	public void test_Uri_Uri_PlainLiteral(){
		Triple t = new TripleImpl(new UriRef("urn:subject"), new UriRef("urn:predicate"), new PlainLiteralImpl("Lorem Ipsum"));
		testTriple(t);
	}
	
	@Test
	public void test_Uri_Uri_BNode(){
		Triple t = new TripleImpl(new UriRef("urn:subject"), new UriRef("urn:predicate"), new BNode());
		testTriple(t);
	}

	@Test
	public void testSparqlSelect(){
		Triple t = new TripleImpl(new UriRef("urn:subject"), new UriRef("urn:predicate"), new UriRef("urn:object"));
		da.insertQuad(testGraphName, t);
		String select = "SELECT ?s ?p ?o WHERE { ?s ?p ?o } LIMIT 1";
		da.executeSparqlQuery(select, new UriRef(testGraphName));
		da.executeSparqlQuery(select, null);
	}
	
	@Test
	public void testSparqlConstruct(){
		Triple t = new TripleImpl(new UriRef("urn:subject"), new UriRef("urn:predicate"), new UriRef("urn:object"));
		da.insertQuad(testGraphName, t);
		String select = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } LIMIT 1";
		da.executeSparqlQuery(select, new UriRef(testGraphName));
		da.executeSparqlQuery(select, null);
	}
	
	@Test
	public void testSparqlAsk(){
		Triple t = new TripleImpl(new UriRef("urn:subject"), new UriRef("urn:predicate"), new UriRef("urn:object"));
		da.insertQuad(testGraphName, t);
		String ask = "ASK { [] [] [] }";
		da.executeSparqlQuery(ask, new UriRef(testGraphName));
		da.executeSparqlQuery(ask, null);
	}
	
	@Test
	public void testSparqlDescribe(){
		Triple t = new TripleImpl(new UriRef("urn:subject"), new UriRef("urn:predicate"), new UriRef("urn:object"));
		da.insertQuad(testGraphName, t);
		String describe = "DESCRIBE <urn:subject> ";
		da.executeSparqlQuery(describe, new UriRef(testGraphName));
		da.executeSparqlQuery(describe, null);
	}
	
//	@Test
//	public void testRenew(){
//		int i = 100;
//		while(i>0){
//			test_Uri_Uri_Uri();
//			test_Uri_Uri_PlainLiteral();
//			i--;
//		}
//		da.renew();
//		i = 100;
//		while(i>0){
//			test_Uri_Uri_Uri();
//			test_Uri_Uri_PlainLiteral();
//			i--;
//		}
//		da.renew();
//		i = 100;
//		while(i>0){
//			test_Uri_Uri_Uri();
//			test_Uri_Uri_PlainLiteral();
//			i--;
//		}
//	}

}
