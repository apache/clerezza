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
import java.util.HashSet;
import java.util.Iterator;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests backup functionality.
 *
 * @author hasan
 */
public class BackupTest {

	private static String testGraphFileName = "test.graph";

	private static MGraph testGraph0 = new SimpleMGraph();
	private static UriRef testGraphUri0 = // the URI of testGraph0
			new UriRef("http://localhost/test0/"+testGraphFileName);
	// a resource in testGraph0
	private	static UriRef uri0 = new UriRef("http://localhost/test0/testuri");

	private static MGraph testGraph1 = new SimpleMGraph();
	private static UriRef testGraphUri1 = // the URI of testGraph1
			new UriRef("http://localhost/test1/"+testGraphFileName);
	// a resource in testGraph1
	private	static UriRef uri1 = new UriRef("http://localhost/test1/testuri");

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
	}

	@Test
	public void testBackup() throws IOException {
		Graph downloadedTestGraphX = null;
		Graph downloadedTestGraphY = null;
		Graph downloadedBackupContentsGraph = null;

		byte[] download = backup.createBackup();
		ByteArrayInputStream bais = new ByteArrayInputStream(download);
		ZipInputStream compressedTcs = new ZipInputStream(bais);

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
				if (entryName.equals(folder+testGraphFileName + ".nt")) {
					downloadedTestGraphX = parser.parse(serializedGraph,
							SupportedFormat.N_TRIPLE, null);
				} else if (entryName.startsWith(folder+testGraphFileName)) {
					downloadedTestGraphY = parser.parse(serializedGraph,
							SupportedFormat.N_TRIPLE, null);
				}
				if (entryName.equals(backupContentFileName)) {
					downloadedBackupContentsGraph = parser.parse(serializedGraph,
							SupportedFormat.N_TRIPLE, null);
				}
				baos.flush();
				baos.close();
			}
		}
		compressedTcs.close();
		checkDownloadedGraphs(downloadedTestGraphX, downloadedTestGraphY,
				downloadedBackupContentsGraph, folder);
	}

	private void checkDownloadedGraphs(Graph downloadedTestGraphX,
			Graph downloadedTestGraphY, Graph downloadedBackupContentsGraph,
			String folder) {
		Assert.assertNotNull(downloadedTestGraphX);
		Assert.assertNotNull(downloadedTestGraphY);
		Assert.assertNotNull(downloadedBackupContentsGraph);

		Assert.assertTrue(downloadedBackupContentsGraph.contains(new TripleImpl(
				testGraphUri0, RDF.type, BACKUP.Graph)));

		Iterator<Triple> triples = downloadedBackupContentsGraph.filter(
				testGraphUri0, BACKUP.file, null);
		Assert.assertTrue(triples.hasNext());

		String fileNameA = ((TypedLiteral) triples.next().getObject()).getLexicalForm();
		Assert.assertTrue(fileNameA.startsWith(folder+testGraphFileName));

		if (fileNameA.equals(folder+testGraphFileName+".nt")) {
			Assert.assertTrue(downloadedTestGraphX.filter(uri0, uri0, uri0).hasNext());
		} else {
			Assert.assertTrue(downloadedTestGraphY.filter(uri0, uri0, uri0).hasNext());
		}

		Assert.assertTrue(downloadedBackupContentsGraph.contains(new TripleImpl(
				testGraphUri1, RDF.type, BACKUP.Graph)));

		triples = downloadedBackupContentsGraph.filter(
				testGraphUri1, BACKUP.file, null);
		Assert.assertTrue(triples.hasNext());

		String fileNameB = ((TypedLiteral) triples.next().getObject()).getLexicalForm();
		Assert.assertTrue(fileNameB.startsWith(folder+testGraphFileName));

		if (fileNameB.equals(folder+testGraphFileName+".nt")) {
			Assert.assertTrue(downloadedTestGraphX.filter(uri1, uri1, uri1).hasNext());
		} else {
			Assert.assertTrue(downloadedTestGraphY.filter(uri1, uri1, uri1).hasNext());
		}

		Assert.assertFalse(fileNameA.equals(fileNameB));
	}

	private class TestTcManager extends TcManager {

		// Associates testGraphUri0 with testGraph0 and testGraphUri1 with testGraph1
		@Override
		public TripleCollection getTriples(UriRef name) throws NoSuchEntityException {
			if (name.equals(testGraphUri0)) {
				return testGraph0;
			} else if (name.equals(testGraphUri1)) {
				return testGraph1;
			}
			return null;
		}

		@Override
		public Set<UriRef> listTripleCollections() {
			Set<UriRef> result = new HashSet<UriRef>();
			result.add(testGraphUri0);
			result.add(testGraphUri1);
			return result;
		}
	}
}