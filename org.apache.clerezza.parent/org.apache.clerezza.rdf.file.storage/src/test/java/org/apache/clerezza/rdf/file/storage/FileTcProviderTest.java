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
package org.apache.clerezza.rdf.file.storage;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;

import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;

import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.test.TcProviderTest;
import static org.junit.Assert.*;

/**
 *
 * @author mir
 */
public class FileTcProviderTest extends TcProviderTest {
	private static final String FILE_PROTOCOL = "file://";
	private static String tempDir = System.getProperty("java.io.tmpdir");
	private static String testDir = tempDir + "/FileTcProviderTest";
	
	
	@Override
	protected UriRef generateUri(String name) {
		String path = FILE_PROTOCOL + testDir + "/";
		return new UriRef(path + name + ".rdf");
	}

	@Before
	public void setupDirectory() {
		FileUtil.setUpEmptyDirectory(testDir);
	}

	@After
	public void deleteDataFile() {
		FileUtil.delete(FileTcProvider.dataFile);
	}
	
	@Test(expected=UnsupportedOperationException.class)
	@Override
	public void testCreateGraph() {
		super.testCreateGraph();
	}

	@Test(expected=UnsupportedOperationException.class)
	@Override
	public void testGetGraph() {
		super.testGetGraph();
	}

	
	@Test
	@Override
	public void testGetTriples() {
		TcProvider fileTcProvider = getInstance();
		// add Graphs
		MGraph mGraph = new SimpleMGraph();
		// add MGraphs
		mGraph = fileTcProvider.createMGraph(uriRefA1);
		mGraph.add(new TripleImpl(uriRefA1, uriRefA1, uriRefA1));
		mGraph = fileTcProvider.createMGraph(uriRefB1);
		mGraph.add(new TripleImpl(uriRefB1, uriRefB1, uriRefB1));

		// get a MGraph
		TripleCollection tripleCollection2 = fileTcProvider.getTriples(uriRefB1);

		Iterator<Triple> iterator = tripleCollection2.iterator();
		assertEquals(new TripleImpl(uriRefB1, uriRefB1, uriRefB1), iterator.next());
		assertFalse(iterator.hasNext());
	}

	@Test
	@Override
	public void testDeleteEntity() {
		TcProvider fileTcProvider = getInstance();
		MGraph mGraph = fileTcProvider.createMGraph(uriRefA);
		mGraph.add(new TripleImpl(uriRefA, uriRefA, uriRefA));
		fileTcProvider.deleteTripleCollection(uriRefA);
		try {
			fileTcProvider.getMGraph(uriRefA);
			assertTrue(false);
		} catch (NoSuchEntityException e) {
			assertTrue(true);
		}
		fileTcProvider = getInstance();
		try {
			fileTcProvider.getMGraph(uriRefA);
			assertTrue(false);
		} catch (NoSuchEntityException e) {
			assertTrue(true);
		}
	}

	@Override
	protected TcProvider getInstance() {
		FileTcProvider fileTcProvider = new FileTcProvider();
		return fileTcProvider;
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testGetNames() {
		//super.testGetNames();
		getInstance().getNames(new SimpleMGraph().getGraph());
	}

	
	@Test(expected=UnsupportedOperationException.class)
	@Override
	public void testCreateGraphExtended() throws Exception {
		super.testCreateGraphExtended();
	}

	@Test(expected=UnsupportedOperationException.class)
	@Override
	public void testCreateGraphNoDuplicateNames() throws Exception {
		super.testCreateGraphNoDuplicateNames();
	}

	@Test(expected=UnsupportedOperationException.class)
	@Override
	public void testCreateGraphWithInitialCollection() throws Exception {
		super.testCreateGraphWithInitialCollection();
	}

	@Test(expected=UnsupportedOperationException.class)
	@Override
	public void testGraphIsNotMutable() throws Exception {
		super.testGraphIsNotMutable();
	}

	@Test(expected=UnsupportedOperationException.class)
	@Override
	public void testGraphDeletion() throws Exception {
		super.testGraphDeletion();
	}

	@Test(expected=UnsupportedOperationException.class)
	@Override
	public void testGetTriplesGraph() throws Exception {
		super.testGetTriples();
	}

	@Test
	public void testDataFile() {
		TcProvider provider = getInstance();
		provider.createMGraph(uriRefA);
		File dataFile = new File("data");
		assertTrue(dataFile.exists());

		Set<String> expected = new HashSet<String>();
		expected.add(uriRefA.getUnicodeString());
		assertTrue(expected.equals(getLinesFromFile(dataFile)));

		provider.createMGraph(uriRefB);
		expected.add(uriRefB.getUnicodeString());
		assertTrue(expected.equals(getLinesFromFile(dataFile)));
	
		provider.deleteTripleCollection(uriRefA);
		expected.remove(uriRefA.getUnicodeString());
		assertTrue(expected.equals(getLinesFromFile(dataFile)));
		
		provider.deleteTripleCollection(uriRefB);
		expected.remove(uriRefB.getUnicodeString());
		assertTrue(expected.equals(getLinesFromFile(dataFile)));
	}

	@Test
	public void testAutoMGraphCreationFromExistingFile() throws Exception{
		FileMGraphTest.setup();
		TcProvider provider = getInstance();
		MGraph mGraph = provider.getMGraph(new UriRef(
				FileMGraphTest.getTempFileUri(FileMGraphTest.RDF_FILE_NAME)));
		assertEquals(2 ,mGraph.size());
		FileMGraphTest.cleanUp();
	}
	
	private Set<String> getLinesFromFile(File file) throws RuntimeException {
		try {
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			Set<String> lines = new HashSet<String>();
			while ((strLine = br.readLine()) != null) {
				lines.add(strLine);
			}
			in.close();
			return lines;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
