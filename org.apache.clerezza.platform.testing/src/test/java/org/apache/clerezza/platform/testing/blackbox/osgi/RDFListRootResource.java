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
package org.apache.clerezza.platform.testing.blackbox.osgi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;

/**
 *
 * @author reto
 */
@Path("/list")
public class RDFListRootResource {

	static final public UriRef testType = new UriRef("http://example.org/foo");

	
	@GET
	public GraphNode sayHello() {
		TripleCollection resultGraph = new SimpleMGraph();
		GraphNode result = new GraphNode(new BNode(), resultGraph);
		result.addProperty(RDF.type, testType);
		RdfList list = new RdfList(result);
		for (int i = 0; i < 10 ; i++) {
			list.add(new PlainLiteralImpl("number "+i));
		}
		return result;
	}
}
