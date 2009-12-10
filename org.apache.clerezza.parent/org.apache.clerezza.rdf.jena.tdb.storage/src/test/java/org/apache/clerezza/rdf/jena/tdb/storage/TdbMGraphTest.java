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
package org.apache.clerezza.rdf.jena.tdb.storage;

import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.test.MGraphTest;

/**
 *
 * @author reto
 */
public class TdbMGraphTest extends MGraphTest {
	private static File tempFile;
	final private UriRef MGRAPHNAME = new UriRef("http://text.example.org/");
	static private TdbTcProvider tdbTcProvider;

	@BeforeClass
	public static void setupDirectory() throws IOException {
		tempFile = File.createTempFile("tdbtest", null);
		tempFile.delete();
		tempFile.mkdirs();
		tdbTcProvider = new TdbTcProvider(tempFile);
	}

	@AfterClass
	public static void cleanUpDirectory() {
		TdbTcProvider.delete(tempFile);
	}

	@After
	public void cleanUpGraph() {
		tdbTcProvider.deleteTripleCollection(MGRAPHNAME);
	}

	@Override
	protected MGraph getEmptyMGraph() {
		return tdbTcProvider.createMGraph(MGRAPHNAME);
	}
	

}
