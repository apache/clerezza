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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.Serializer;
import static org.junit.Assert.*;

/**
 *
 * @author mir
 */
public class FileMGraphTest {

	private static String tempDir = System.getProperty("java.io.tmpdir");
	private static final String FILE_PROTOCOL = "file://";

	protected static final String NT_FILE_NAME = "test-04.nt";
	protected static final String RDF_FILE_NAME = "test-04.rdf";
	protected static final String TURTLE_FILE_NAME = "test-04.ttl";

	private static final UriRef uriA = new UriRef("http://example.com/a");
	private static final UriRef uriB = new UriRef("http://example.com/b");
	private static final UriRef uriC = new UriRef("http://example.com/c");


	@BeforeClass
	public static void setup() throws Exception {
		createTempFileFromResource(NT_FILE_NAME);
		createTempFileFromResource(RDF_FILE_NAME);
		createTempFileFromResource(TURTLE_FILE_NAME);
	}

	@AfterClass
	public static void cleanUp() throws Exception {
		deleteTempFile(NT_FILE_NAME);
		deleteTempFile(RDF_FILE_NAME);
		deleteTempFile(TURTLE_FILE_NAME);
	}

	private static void createTempFileFromResource(String resourceName) 
			throws FileNotFoundException, IOException {
		InputStream in = FileTcProviderTest.class.getResourceAsStream(resourceName);
		File file = new File(URI.create(getTempFileUri(resourceName)));
		FileOutputStream fout = new FileOutputStream(file);
		int inByte;
		while ((inByte = in.read()) != -1) {
			fout.write(inByte);
		}
	}
	
	protected static String getTempFileUri(String name) {
		return FILE_PROTOCOL + tempDir + "/" + name;
	}

	private static void deleteTempFile(String name)
			throws FileNotFoundException, IOException {
		File file = new File(tempDir + "/" + name);
		file.delete();
	}
	
	@Test
	public void testReadingFromFile() {
		FileMGraph mGraph = new FileMGraph(new UriRef(getTempFileUri(RDF_FILE_NAME)),
				Parser.getInstance(), Serializer.getInstance());
		assertEquals(2, mGraph.size());

		mGraph = new FileMGraph(new UriRef(getTempFileUri(TURTLE_FILE_NAME)),
				Parser.getInstance(), Serializer.getInstance());
		assertEquals(2, mGraph.size());

		mGraph = new FileMGraph(new UriRef(getTempFileUri(NT_FILE_NAME)),
				Parser.getInstance(), Serializer.getInstance());
		assertEquals(2, mGraph.size());
	}
	
	@Test
	public void testFilter() throws IOException {
		String fileName = "filter.rdf";
		FileMGraph mGraph = new FileMGraph(new UriRef(getTempFileUri(fileName)),
				Parser.getInstance(), Serializer.getInstance());

		mGraph.add(new TripleImpl(uriA, uriB, uriC));
		mGraph.add(new TripleImpl(uriC, uriB, uriA));

		mGraph = new FileMGraph(new UriRef(getTempFileUri(fileName)),
				Parser.getInstance(), Serializer.getInstance());
		
		
		assertEquals(2, mGraph.size());
		Iterator<Triple> iterator = mGraph.filter(null, null, null);

		iterator.next();
		iterator.remove();
		assertEquals(1, mGraph.size());

		mGraph = new FileMGraph(new UriRef(getTempFileUri(fileName)),
				Parser.getInstance(), Serializer.getInstance());
		assertEquals(1, mGraph.size());
		deleteTempFile(fileName);
	}
}
