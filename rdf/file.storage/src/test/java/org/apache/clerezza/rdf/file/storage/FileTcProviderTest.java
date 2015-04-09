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
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Iri;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;

import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
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
    protected Iri generateUri(String name) {
            String prefix = testDir.startsWith("/") ? FILE_PROTOCOL : FILE_PROTOCOL +"/";
        String path =  prefix + testDir.replace('\\', '/') + "/";
        return new Iri(path + name + ".rdf");
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
    public void testCreateImmutableGraph() {
        super.testCreateImmutableGraph();
    }
    
    @Test(expected=UnsupportedOperationException.class)
    @Override
    public void testCreateImmutableGraphExtended() throws Exception {
        super.testCreateImmutableGraphExtended();
    }

    @Test(expected=UnsupportedOperationException.class)
    @Override
    public void testGetImmutableGraph() {
        super.testGetImmutableGraph();
    }

    
    @Test
    @Override
    public void testGetTriples() {
        TcProvider fileTcProvider = getInstance();
        // add Graphs
        Graph mGraph = new SimpleGraph();
        // add Graphs
        mGraph = fileTcProvider.createGraph(uriRefA1);
        mGraph.add(new TripleImpl(uriRefA1, uriRefA1, uriRefA1));
        mGraph = fileTcProvider.createGraph(uriRefB1);
        mGraph.add(new TripleImpl(uriRefB1, uriRefB1, uriRefB1));

        // get a Graph
        Graph tripleCollection2 = fileTcProvider.getGraph(uriRefB1);

        Iterator<Triple> iterator = tripleCollection2.iterator();
        assertEquals(new TripleImpl(uriRefB1, uriRefB1, uriRefB1), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    @Override
    public void testDeleteEntity() {
        TcProvider fileTcProvider = getInstance();
        Graph mGraph = fileTcProvider.createGraph(uriRefA);
        mGraph.add(new TripleImpl(uriRefA, uriRefA, uriRefA));
        fileTcProvider.deleteGraph(uriRefA);
        try {
            fileTcProvider.getGraph(uriRefA);
            assertTrue(false);
        } catch (NoSuchEntityException e) {
            assertTrue(true);
        }
        fileTcProvider = getInstance();
        try {
            fileTcProvider.getGraph(uriRefA);
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
        getInstance().getNames(new SimpleGraph().getImmutableGraph());
    }

    
    /*@Test(expected=UnsupportedOperationException.class)
    @Override
    public void testCreateGraphExtended() throws Exception {
        super.testCreateGraphExtended();
    }*/

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
        provider.createGraph(uriRefA);
        File dataFile = new File("data");
        assertTrue(dataFile.exists());

        Set<String> expected = new HashSet<String>();
        expected.add(uriRefA.getUnicodeString());
        assertTrue(expected.equals(getLinesFromFile(dataFile)));

        provider.createGraph(uriRefB);
        expected.add(uriRefB.getUnicodeString());
        assertTrue(expected.equals(getLinesFromFile(dataFile)));
    
        provider.deleteGraph(uriRefA);
        expected.remove(uriRefA.getUnicodeString());
        assertTrue(expected.equals(getLinesFromFile(dataFile)));
        
        provider.deleteGraph(uriRefB);
        expected.remove(uriRefB.getUnicodeString());
        assertTrue(expected.equals(getLinesFromFile(dataFile)));
    }

    @Test
    public void testAutoGraphCreationFromExistingFile() throws Exception{
        FileGraphTest.setup();
        TcProvider provider = getInstance();
        Graph mGraph = provider.getGraph(new Iri(
                FileGraphTest.getTempFileUri(FileGraphTest.RDF_FILE_NAME)));
        assertEquals(2 ,mGraph.size());
        FileGraphTest.cleanUp();
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
