/*
 *
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
 *
*/

package skeleton

import javax.ws.rs._
import org.apache.clerezza.rdf.core.BNode
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.rdf.ontologies.{DC, RDF}
import org.apache.clerezza.rdf.utils.GraphNode
import org.osgi.framework.BundleContext
import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider

/**
 * The classical Hello World root resource
 */
@Path("hello-world")
class HelloWorld(context: BundleContext) {
	val servicesDsl = new ServicesDsl(context)
	import servicesDsl._
	@GET def get() = {
		val resultMGraph = new SimpleMGraph();
		val graphNode = new GraphNode(new BNode(), resultMGraph);
		graphNode.addProperty(RDF.`type` , Ontology.HelloWordMessageType);
		val cgp: ContentGraphProvider = $[ContentGraphProvider]
		graphNode.addPropertyValue(DC.description,"Hello world of "+cgp.getContentGraph.size);
		graphNode;

	}
}
