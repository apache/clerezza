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
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.ParsingProvider;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.jena.parser.JenaParserProvider;
import org.apache.clerezza.rdf.jena.serializer.JenaSerializerProvider;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.web.ontologies.BACKUP;
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

    private static MGraph testMGraph0 = new SimpleMGraph();
    private static UriRef testMGraphUri0 = // the URI of testMGraph0
            new UriRef("http://localhost/test0/"+testGraphFileName);
    // a resource in testMGraph0
    private    static UriRef uri0 = new UriRef("http://localhost/test0/testuri");

    private static MGraph testMGraph1 = new SimpleMGraph();
    private static UriRef testMGraphUri1 = // the URI of testMGraph1
            new UriRef("http://localhost/test1/"+testGraphFileName);

    // a resource in testMGraph1
    private    static UriRef uri1 = new UriRef("http://localhost/test1/testuri");

    private static Graph testGraphA;
    private static UriRef testGraphUriA = // the URI of testGraphA
            new UriRef("http://localhost/testA/"+testGraphFileName);

    // a resource in testGraphA
    private    static UriRef uriA = new UriRef("http://localhost/testA/testuri");
    

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
        testMGraph0.add(new TripleImpl(uri0, uri0, uri0));
        testMGraph1.add(new TripleImpl(uri1, uri1, uri1));
        MGraph graphBuilder = new SimpleMGraph();
        graphBuilder.add(new TripleImpl(uriA, uriA, uriA));
        testGraphA = graphBuilder.getGraph();
    }

    @Test
    public void testBackup() throws IOException {
        //Graph downloadedTestGraphX = null;
        //Graph downloadedTestGraphY = null;
        Graph downloadedBackupContentsGraph = null;

        byte[] download = backup.createBackup();
        ByteArrayInputStream bais = new ByteArrayInputStream(download);
        ZipInputStream compressedTcs = new ZipInputStream(bais);

        Map<String, TripleCollection> extractedTc = new HashMap<String, TripleCollection>();
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
                    Graph deserializedGraph = parser.parse(serializedGraph,
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
        EasyMock.expect(tcProvider.getMGraph(testMGraphUri0)).andReturn(
                EasyMock.createNiceMock(MGraph.class));
        EasyMock.expect(tcProvider.getMGraph(testMGraphUri1)).andReturn(
                EasyMock.createNiceMock(MGraph.class));
        tcProvider.deleteTripleCollection(testGraphUriA);
        EasyMock.expect(tcProvider.createGraph(EasyMock.eq(testGraphUriA),
                EasyMock.notNull(TripleCollection.class))).andReturn(new SimpleMGraph().getGraph());
        EasyMock.replay(tcProvider);
        Restorer restore = new Restorer();
        restore.parser = Parser.getInstance();
        restore.restore(new ByteArrayInputStream(backupData), tcProvider);
        EasyMock.verify(tcProvider);
    }

    private void checkDownloadedGraphs(Map<String, TripleCollection> extractedTc,
            Graph downloadedBackupContentsGraph, String folder) {
        Assert.assertFalse(extractedTc.isEmpty());
        Assert.assertNotNull(downloadedBackupContentsGraph);

        Assert.assertTrue(downloadedBackupContentsGraph.contains(new TripleImpl(
                testMGraphUri0, RDF.type, BACKUP.MGraph)));

        Iterator<Triple> triples = downloadedBackupContentsGraph.filter(
                testMGraphUri0, BACKUP.file, null);
        Assert.assertTrue(triples.hasNext());

        String fileName0 = ((TypedLiteral) triples.next().getObject()).getLexicalForm();
        Assert.assertTrue(fileName0.startsWith(folder+testGraphFileName));

        TripleCollection extracted0 = extractedTc.get(fileName0);
        Assert.assertNotNull(extracted0);
        Assert.assertTrue(extracted0.filter(uri0, uri0, uri0).hasNext());

        Assert.assertTrue(downloadedBackupContentsGraph.contains(new TripleImpl(
                testMGraphUri1, RDF.type, BACKUP.MGraph)));

        triples = downloadedBackupContentsGraph.filter(
                testMGraphUri1, BACKUP.file, null);
        Assert.assertTrue(triples.hasNext());

        String fileName1 = ((TypedLiteral) triples.next().getObject()).getLexicalForm();
        Assert.assertTrue(fileName1.startsWith(folder+testGraphFileName));

        TripleCollection extracted1 = extractedTc.get(fileName1);
        Assert.assertNotNull(extracted1);

        Assert.assertTrue(extracted1.filter(uri1, uri1, uri1).hasNext());
    


        Assert.assertTrue(downloadedBackupContentsGraph.contains(new TripleImpl(
                testGraphUriA, RDF.type, BACKUP.Graph)));

        triples = downloadedBackupContentsGraph.filter(
                testGraphUriA, BACKUP.file, null);
        Assert.assertTrue(triples.hasNext());

        String fileNameA = ((TypedLiteral) triples.next().getObject()).getLexicalForm();
        Assert.assertTrue(fileNameA.startsWith(folder+testGraphFileName));
        TripleCollection extractedA = extractedTc.get(fileNameA);
        Assert.assertNotNull(extractedA);

        Assert.assertTrue(extractedA.filter(uriA, uriA, uriA).hasNext());

    }

    private class TestTcManager extends TcManager {

        // Associates testGraphUri0 with testMGraph0 and testGraphUri1 with testGraph1
        @Override
        public TripleCollection getTriples(UriRef name) throws NoSuchEntityException {
            if (name.equals(testMGraphUri0)) {
                return testMGraph0;
            } else if (name.equals(testMGraphUri1)) {
                return testMGraph1;
            } else if (name.equals(testGraphUriA)) {
                return testGraphA;
            }
            return null;
        }

        @Override
        public Set<UriRef> listTripleCollections() {
            Set<UriRef> result = new HashSet<UriRef>();
            result.add(testMGraphUri0);
            result.add(testMGraphUri1);
            result.add(testGraphUriA);
            return result;
        }
    }
}