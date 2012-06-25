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

package org.apache.clerezza.rdf.storage.externalizer;

import java.io.File;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class ExternalizingMGraphTest {

	@Test
	public void replaceLiteral() throws Exception {
		File dataDir = File.createTempFile("test", "externalizer");
		dataDir.delete();
		dataDir.mkdir();
		ExternalizingMGraph graph = new ExternalizingMGraph(new SimpleMGraph(), dataDir);
		TypedLiteral lit = new TypedLiteralImpl("jkjkj", ExternalizingMGraph.base64Uri);
		UriRef replacement = graph.replace(lit);
		TypedLiteral reconstructed = graph.getLiteralForUri(replacement.getUnicodeString());
		Assert.assertEquals(replacement, graph.replace(reconstructed));
	}

	@Test
	public void base16Ints() throws Exception {
		File dataDir = File.createTempFile("test", "externalizer");
		dataDir.delete();
		dataDir.mkdir();
		ExternalizingMGraph graph = new ExternalizingMGraph(new SimpleMGraph(), dataDir);
		//int value = -1291264412;
		int value = -0x10;
		byte[] bytes = new byte[4];
		bytes[0] = (byte) (0xFF & (value >>> 24));
        bytes[1] = (byte) (0xFF & (value >>> 16));
        bytes[2] = (byte) (0xFF & (value >>> 8));
        bytes[3] = (byte) (0xFF & value);
		Assert.assertEquals(value, graph.parseHexInt(graph.toBase16(bytes)));
	}

}
