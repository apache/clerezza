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
package org.apache.clerezza.rdf.web.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.jena.parser.JenaParserProvider;
import org.apache.clerezza.rdf.jena.serializer.JenaSerializerProvider;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.web.ontologies.BACKUP;
import org.apache.clerezza.commons.rdf.Literal;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests backup functionality.
 *
 * @author hasan
 */
public class BackupAndRestoreTest {

    private static String testGraphFileName = "test.graph";

    private static Graph testGraph0 = new SimpleGraph();
    private static IRI testGraphUri0 = // the URI of testGraph0
            new IRI("http://localhost/test0/"+testGraphFileName);
    // a resource in testGraph0
    private    static IRI uri0 = new IRI("http://localhost/test0/testuri");

    private static Graph testGraph1 = new SimpleGraph();
    private static IRI testGraphUri1 = // the URI of testGraph1
            new IRI("http://localhost/test1/"+testGraphFileName);

    // a resource in testGraph1
    private    static IRI uri1 = new IRI("http://localhost/test1/testuri");

    private static ImmutableGraph testGraphA;
    private static IRI testGraphUriA = // the URI of testGraphA
            new IRI("http://localhost/testA/"+testGraphFileName);

    // a resource in testGraphA
    private    static IRI uriA = new IRI("http://localhost/testA/testuri");
    

    private static String backupContentFileName = "triplecollections.nt";
    private static BackupMessageBodyWriter backup;
    private static Parser parser = Parser.getInstance();

    @Before
    public void setUp() {
        backup = new BackupMessageBodyWriter();
        backup.tcManager = new TestTcManager();
        backup.serializer = Serializer.getInstance();
        backup.serializer.bindSerializingProvider(
                new JenaSerializerProvider());
        testGraph0.add(new TripleImpl(uri0, uri0, uri0));
        testGraph1.add(new TripleImpl(uri1, uri1, uri1));
        Graph graphBuilder = new SimpleGraph();
        graphBuilder.add(new TripleImpl(uriA, uriA, uriA));
        testGraphA = graphBuilder.getImmutableGraph();
    }

    @Test
    public void testBackup() throws IOException {
        //ImmutableGraph downloadedTestGraphX = null;
        //ImmutableGraph downloadedTestGraphY = null;
        ImmutableGraph downloadedBackupContentsGraph = null;

        byte[] download = backup.createBackup();
        ByteArrayInputStream bais = new ByteArrayInputStream(download);
        ZipInputStream compressedTcs = new ZipInputStream(bais);

        Map<String, Graph> extractedTc = new HashMap<String, Graph>();
        String folder = "";
        ZipEntry entry;
        while ((entry = compressedTcs.getNextEntry()) != null) {
            String entryName = entry.getName();
            if (entry.isDirectory()) {
                folder = entryName;
            } else {
                Assert.assertTrue(entryName.startsWith(folder+testGraphFileName)
                        || entryName.equals(backupContentFileName));
                ByteArrayOutputStream baos = new ByteArrayOutputStream(download.length);
                int count;
                byte buffer[] = new byte[2048];
                while ((count = compressedTcs.read(buffer, 0, 2048)) != -1) {
                    baos.write(buffer, 0, count);
                }
                ByteArrayInputStream serializedGraph = new ByteArrayInputStream(
                        baos.toByteArray());
                /*if (entryName.equals(folder+testGraphFileName + ".nt")) {
                    downloadedTestGraphX = parser.parse(serializedGraph,
                            SupportedFormat.N_TRIPLE, null);
                } else if (entryName.startsWith(folder+testGraphFileName)) {
                    downloadedTestGraphY = parser.parse(serializedGraph,
                            SupportedFormat.N_TRIPLE, null);
                }*/
                if (entryName.equals(backupContentFileName)) {
                    downloadedBackupContentsGraph = parser.parse(serializedGraph,
                            SupportedFormat.N_TRIPLE, null);
                } else {
                    ImmutableGraph deserializedGraph = parser.parse(serializedGraph,
                            SupportedFormat.N_TRIPLE, null);
                    extractedTc.put(entryName, deserializedGraph);
                }
                baos.flush();
                baos.close();
            }
        }
        compressedTcs.close();
        checkDownloadedGraphs(extractedTc,
                downloadedBackupContentsGraph, folder);
    }

    @Test
    public void restoreFromBackup() throws IOException {
        byte[] backupData = backup.createBackup();
        TcProvider tcProvider = EasyMock.createMock(TcProvider.class);
        EasyMock.expect(tcProvider.getMGraph(testGraphUri0)).andReturn(
                EasyMock.createNiceMock(Graph.class));
        EasyMock.expect(tcProvider.getMGraph(testGraphUri1)).andReturn(
                EasyMock.createNiceMock(Graph.class));
        EasyMock.expect(tcProvider.getMGraph(testGraphUriA)).andReturn(
                EasyMock.createNiceMock(Graph.class));
        tcProvider.deleteGraph(testGraphUriA);
        tcProvider.deleteGraph(testGraphUri0);
        tcProvider.deleteGraph(testGraphUri1);
        EasyMock.expect(tcProvider.createImmutableGraph(EasyMock.eq(testGraphUriA),
                EasyMock.notNull(Graph.class))).andReturn(new SimpleGraph().getImmutableGraph());
        EasyMock.expect(tcProvider.createImmutableGraph(EasyMock.eq(testGraphUri0),
                EasyMock.notNull(Graph.class))).andReturn(new SimpleGraph().getImmutableGraph());
        EasyMock.expect(tcProvider.createImmutableGraph(EasyMock.eq(testGraphUri1),
                EasyMock.notNull(Graph.class))).andReturn(new SimpleGraph().getImmutableGraph());
        EasyMock.replay(tcProvider);
        Restorer restore = new Restorer();
        restore.parser = Parser.getInstance();
        restore.restore(new ByteArrayInputStream(backupData), tcProvider);
        EasyMock.verify(tcProvider);
    }

    private void checkDownloadedGraphs(Map<String, Graph> extractedTc,
            ImmutableGraph downloadedBackupContentsGraph, String folder) {
        Assert.assertFalse(extractedTc.isEmpty());
        Assert.assertNotNull(downloadedBackupContentsGraph);

        Assert.assertTrue(downloadedBackupContentsGraph.contains(new TripleImpl(
                testGraphUri0, RDF.type, BACKUP.Graph)));

        Iterator<Triple> triples = downloadedBackupContentsGraph.filter(
                testGraphUri0, BACKUP.file, null);
        Assert.assertTrue(triples.hasNext());

        String fileName0 = ((Literal) triples.next().getObject()).getLexicalForm();
        Assert.assertTrue(fileName0.startsWith(folder+testGraphFileName));

        Graph extracted0 = extractedTc.get(fileName0);
        Assert.assertNotNull(extracted0);
        Assert.assertTrue(extracted0.filter(uri0, uri0, uri0).hasNext());

        Assert.assertTrue(downloadedBackupContentsGraph.contains(new TripleImpl(
                testGraphUri1, RDF.type, BACKUP.Graph)));

        triples = downloadedBackupContentsGraph.filter(
                testGraphUri1, BACKUP.file, null);
        Assert.assertTrue(triples.hasNext());

        String fileName1 = ((Literal) triples.next().getObject()).getLexicalForm();
        Assert.assertTrue(fileName1.startsWith(folder+testGraphFileName));

        Graph extracted1 = extractedTc.get(fileName1);
        Assert.assertNotNull(extracted1);

        Assert.assertTrue(extracted1.filter(uri1, uri1, uri1).hasNext());
    


        Assert.assertTrue(downloadedBackupContentsGraph.contains(new TripleImpl(
                testGraphUriA, RDF.type, BACKUP.Graph)));

        triples = downloadedBackupContentsGraph.filter(
                testGraphUriA, BACKUP.file, null);
        Assert.assertTrue(triples.hasNext());

        String fileNameA = ((Literal) triples.next().getObject()).getLexicalForm();
        Assert.assertTrue(fileNameA.startsWith(folder+testGraphFileName));
        Graph extractedA = extractedTc.get(fileNameA);
        Assert.assertNotNull(extractedA);

        Assert.assertTrue(extractedA.filter(uriA, uriA, uriA).hasNext());

    }

    private class TestTcManager extends TcManager {

        // Associates testGraphUri0 with testGraph0 and testGraphUri1 with testGraph1
        @Override
        public Graph getGraph(IRI name) throws NoSuchEntityException {
            if (name.equals(testGraphUri0)) {
                return testGraph0;
            } else if (name.equals(testGraphUri1)) {
                return testGraph1;
            } else if (name.equals(testGraphUriA)) {
                return testGraphA;
            }
            return null;
        }

        @Override
        public Set<IRI> listGraphs() {
            Set<IRI> result = new HashSet<IRI>();
            result.add(testGraphUri0);
            result.add(testGraphUri1);
            result.add(testGraphUriA);
            return result;
        }
    }
}