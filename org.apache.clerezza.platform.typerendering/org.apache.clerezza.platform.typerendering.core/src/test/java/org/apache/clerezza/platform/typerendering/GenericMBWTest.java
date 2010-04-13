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
package org.apache.clerezza.platform.typerendering;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import junit.framework.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.triaxrs.util.MultivaluedMapImpl;

/**
 * Unit test for the GenericGraphNodeMBW
 * 
 * @author daniel, mir
 * 
 */
public class GenericMBWTest {

	private UriRef resourceUri = new UriRef(
			"http:localhost:8282/testResource");
	public static final String TEST_MODE = "edit";
	private static final UriRef TEST_RDF_TYPE = new UriRef("org.example/foo");
	public static MediaType mediaType;

	@Test
	public void testWriteNode() throws WebApplicationException, IOException {

		RendererFactory factory = getPreparedRendererFactory();
		GenericGraphNodeMBW ggnmbw = new GenericGraphNodeMBW();
		// "bind" Renderer
		ggnmbw.rendererFactory = factory;
		// "inject" HttpHeaders
		ggnmbw.setHttpHeaders(new HttpHeadersStub(mediaType));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GraphNode node = createResourceGraphNode();
		MultivaluedMap httpHeaders = new MultivaluedMapImpl();
		ggnmbw.writeTo(node, null, null, null, null, httpHeaders, baos);
		Assert.assertEquals(httpHeaders.getFirst(HttpHeaders.CONTENT_TYPE), mediaType);
		Assert.assertEquals(RendereringTest.renderletMockA.outputForRenderSpecUriA
				+ mediaType.toString(), baos.toString());
	}

	@Test
	public void testWriteNodeWithMode() throws Exception {
			RendererFactory factory = getPreparedRendererFactory();
		GenericGraphNodeMBW ggnmbw = new GenericGraphNodeMBW();
		// "bind" Renderer
		ggnmbw.rendererFactory = factory;
		// "inject" UriInfo
		ggnmbw.setUriInfo(new UriInfoStub());
		// "inject" HttpHeaders
		ggnmbw.setHttpHeaders(new HttpHeadersStub(mediaType));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GraphNode node = createResourceGraphNode();
		MultivaluedMap httpHeaders = new MultivaluedMapImpl();
		ggnmbw.writeTo(node, null, null, null, null, httpHeaders, baos);
		Assert.assertEquals(httpHeaders.getFirst(HttpHeaders.CONTENT_TYPE), mediaType);
		Assert.assertEquals(RendereringTest.renderletMockB.outputForRenderSpecUriA
				+ mediaType.toString(), baos.toString());
	}

	@Test
	public void testWithMultipleAcceptableTypes() throws Exception {
		RendereringManagerTest test = new RendereringManagerTest();
		RenderletManager manager = test.createNewRenderletManager();
		MediaType typeA = new MediaType("application", "A");
		MediaType typeB = new MediaType("application", "B");
		manager.registerRenderlet(RendereringTest.renderletMockA.pid,
				RendereringTest.renderSpecUriA, TEST_RDF_TYPE, null, typeA, true);
		manager.registerRenderlet(RendereringTest.renderletMockB.pid,
				RendereringTest.renderSpecUriB, TEST_RDF_TYPE, null, typeB, true);
		GraphNode node = createResourceGraphNode();

		GenericGraphNodeMBW ggnmbw = new GenericGraphNodeMBW();
		// "bind" Renderer
		ggnmbw.rendererFactory = test.getRendererFactory();
		
		// "inject" HttpHeaders
		ggnmbw.setHttpHeaders(new HttpHeadersStub(typeA, typeB));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		MultivaluedMap httpHeaders = new MultivaluedMapImpl();
		ggnmbw.writeTo(node, null, null, null, null, httpHeaders, baos);
		Assert.assertEquals(httpHeaders.getFirst(HttpHeaders.CONTENT_TYPE), typeA);
		Assert.assertEquals(RendereringTest.renderletMockA.outputForRenderSpecUriA
				+ typeA.toString(), baos.toString());

		// "inject" HttpHeaders
		ggnmbw.setHttpHeaders(new HttpHeadersStub(typeB, typeA));
		baos = new ByteArrayOutputStream();
		httpHeaders = new MultivaluedMapImpl();
		ggnmbw.writeTo(node, null, null, null, null, httpHeaders, baos);
		Assert.assertEquals(httpHeaders.getFirst(HttpHeaders.CONTENT_TYPE), typeB);
		Assert.assertEquals(RendereringTest.renderletMockB.outputForRenderSpecUriB
				+ typeB.toString(), baos.toString());
	}

	private GraphNode createResourceGraphNode() {
		MGraph mGraph = new SimpleMGraph();
		GraphNode node = new GraphNode(resourceUri, mGraph);
		node.addProperty(RDF.type, TEST_RDF_TYPE);
		return node;
	}

	/**
	 * Returns a prepared instance of <code>Renderer</code> for
	 * testing.
	 *
	 * @return
	 */
	private RendererFactory getPreparedRendererFactory() {
		RendereringManagerTest test = new RendereringManagerTest();
		RenderletManager manager = test.createNewRenderletManager();
		mediaType = new MediaType("application", "test");
 		manager.registerRenderlet(RendereringTest.renderletMockA.pid,
				RendereringTest.renderSpecUriA, TEST_RDF_TYPE, null, mediaType, true);
		manager.registerRenderlet(RendereringTest.renderletMockB.pid,
				RendereringTest.renderSpecUriA, TEST_RDF_TYPE, TEST_MODE, mediaType, true);
		return test.getRendererFactory();
	}
}
