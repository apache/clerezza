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

package org.apache.clerezza.rdf.core.impl.graphmatching;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;

/**
 *
 * @author reto
 */
public class Utils4Testing {

	static MGraph generateLine(int size, final NonLiteral firstNode) {
		if (size < 1) {
			throw new IllegalArgumentException();
		}
		MGraph result = new SimpleMGraph();
		NonLiteral lastNode = firstNode;
		for (int i = 0; i < size; i++) {
			final BNode newNode = new BNode();
			result.add(new TripleImpl(lastNode, u1, newNode));
			lastNode = newNode;
		}
		return result;
	}

	final static UriRef u1 = new UriRef("http://example.org/u1");

}
