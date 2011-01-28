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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;

import javax.ws.rs.core.MediaType;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.impl.TypedLiteralImpl;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * @author mir
 * 
 */
public abstract class RendereringTest {

	static UriRef renderSpecUriA = new UriRef("http://example.org/renderSpecA");
	static UriRef renderSpecUriB = new UriRef("http://example.org/renderSpecB");
	private final String mode1 = "mode1";
	private final String mode2 = "mode2";
	private final UriRef rdfTypeX = new UriRef("org.example/MyTypeX");
	private final UriRef rdfTypeY = new UriRef("org.example/MyTypeY");
	private UriRef resourceUri = new UriRef(
			"http://localhost:8282/testResource");

	public static RenderletMock renderletMockA = new RenderletMock("renderletMockA.pid", "test", "example");
	public static RenderletMock renderletMockB = new RenderletMock("renderletMockB.pid", "foo", "bar");
	
	public static class RenderletMock implements Renderlet {
		
		public String outputForRenderSpecUriA = "test";
		public String outputForRenderSpecUriB = "example";
		public String pid = "org.apache.clerezza.typerendering.renderletMockA";

		public RenderletMock(String pid, String outputA, String outputB) {
			this.outputForRenderSpecUriA = outputA;
			this.outputForRenderSpecUriB = outputB;
			this.pid = pid;
		}

		@Override
		public void render(GraphNode res, GraphNode context, Map<String, Object> sharedRenderingValues,
				CallbackRenderer callbackRenderer, URI renderingSpecification,
				String mode, MediaType mediaType, 
				Renderlet.RequestProperties requestProperties, OutputStream os) {
			try {
				String engineOutput = "";

				if (renderingSpecification == null) {
					engineOutput = "no rendering specification" + mediaType.toString();

				} else {
					if (renderingSpecification.equals(new URI(renderSpecUriA.getUnicodeString()))) {
						engineOutput = outputForRenderSpecUriA + mediaType.toString();
					} else if (renderingSpecification.equals(new URI(renderSpecUriB.getUnicodeString()))) {
						engineOutput = outputForRenderSpecUriB + mediaType.toString();
					}
				}
				os.write(engineOutput.getBytes());

			} catch (URISyntaxException ex) {
				Logger.getLogger(RendereringTest.class.getName()).log(Level.SEVERE,
						null, ex);
			} catch (MalformedURLException ex) {
				Logger.getLogger(GenericMBWTest.class.getName()).log(Level.SEVERE,
						null, ex);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Subclasses implement this method to provide implementation instances of
	 * <code>RenderletManager</code>. This method may be called an arbitrary
	 * amount of time, independently whether previously returned
	 * <code>RenderletManager</code>s are still in use or not. The implementation
	 * must bind the <code>Renderlet</code>s <code>Renderer.renderletMockA</code>
	 * and <code>Renderer.renderletMockB</code> with the service pids
	 * <code>Renderer.renderletMockA.pid</code> and
	 * <code>Renderer.renderletMockB.pid</code>.
	 * 
	 * @return a new RenderletManager of the implementation to be tested
	 */
	protected abstract RenderletManager createNewRenderletManager();

	/**
	 * Subclasses implement this method to provide implementation instances of
	 * <code>RendererFactory</code>. The returned <code>RendererFactory</code>
	 * uses the registered renderlets of the last <code>RenderletManager</code>
	 * returned by createNewRenderletManager.
	 *
	 * @return a RendererFactory of the implementation to be tested.
	 */
	protected abstract RendererFactory getRendererFactory();

	@Test
	public void testRendering() throws Exception {
		
		RenderletManager renderletManager = createNewRenderletManager();
		MediaType mediaType = new MediaType ("application", "test");
		renderletManager = createNewRenderletManager();
		String result;
		// register renderlet and rendering specification with no mode.
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, null, mediaType, true);
		result = renderWithSetting( null, mediaType, rdfTypeX);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriA + mediaType.toString(), result);
		try {
			renderWithSetting(mode1, mediaType, rdfTypeX);
			Assert.assertTrue(false);
		} catch (WebApplicationException ex) {}
		try {
			renderWithSetting( null, mediaType,rdfTypeY);
			Assert.assertTrue(false);
		} catch (WebApplicationException ex) {}
	

		renderletManager = createNewRenderletManager();
		
		// register one renderlet and rendering specification with mode
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, mode1, mediaType, true);
		result = renderWithSetting(mode1,mediaType, rdfTypeX);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriA + mediaType.toString(), result);
		try{
			result = renderWithSetting( null,mediaType,rdfTypeX);
			Assert.assertTrue(false);
		} catch (WebApplicationException ex) {}
		try{
			result = renderWithSetting(mode1,mediaType, rdfTypeY);
			Assert.assertTrue(false);
		} catch (WebApplicationException ex) {}

		renderletManager = createNewRenderletManager();
		// register a renderlet with no rendering specifications and with no mode.
		renderletManager.registerRenderlet(renderletMockA.pid, null, rdfTypeX, null, mediaType, true);
		result = renderWithSetting(null, mediaType,rdfTypeX);
		Assert.assertEquals("no rendering specification" + mediaType.toString(), result);

		renderletManager = createNewRenderletManager();
		// register a renderlet with two rendering specifications and with different mode for same rdfType.
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, mode1, mediaType, true);
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriB, rdfTypeX, mode2, mediaType, true);
		result = renderWithSetting(mode1,mediaType, rdfTypeX);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriA + mediaType.toString(), result);
		result = renderWithSetting(mode2,mediaType, rdfTypeX);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriB + mediaType.toString(), result);

		renderletManager = createNewRenderletManager();
		// register a renderlet with two rendering specifications and with same mode for different rdfType.
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, mode1, mediaType, true);
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriB, rdfTypeY, mode1, mediaType, true);
		result = renderWithSetting(mode1,mediaType, rdfTypeX);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriA + mediaType.toString(), result);
		result = renderWithSetting(mode1,mediaType, rdfTypeY);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriB + mediaType.toString(), result);

		renderletManager = createNewRenderletManager();
		// register a renderlet with two rendering specifications and with no mode for different rdfType.
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, null, mediaType, true);
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriB, rdfTypeY, null, mediaType, true);
		result = renderWithSetting( null,mediaType,rdfTypeX);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriA + mediaType.toString(), result);
		result = renderWithSetting( null,mediaType,rdfTypeY);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriB + mediaType.toString(), result);

		renderletManager = createNewRenderletManager();
		// register a renderlet with two rendering specifications and with one with and one without mode for
		// same rdfType.
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, mode1, mediaType, true);
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriB, rdfTypeX, null, mediaType, true);
		result = renderWithSetting(mode1,mediaType, rdfTypeX);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriA + mediaType.toString(), result);
		result = renderWithSetting( null, mediaType,rdfTypeX);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriB + mediaType.toString(), result);

		renderletManager = createNewRenderletManager();
		// Same as before, but reversed registration order
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriB, rdfTypeX, null, mediaType, true);
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, mode1, mediaType, true);
		result = renderWithSetting(mode1,mediaType, rdfTypeX);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriA + mediaType.toString(), result);
		result = renderWithSetting( null, mediaType,rdfTypeX);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriB + mediaType.toString(), result);

		renderletManager = createNewRenderletManager();
		// register a renderlet with two rendering specifications and with no mode for different rdfType. Types-set
		// with both rdfTypes in it.
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, null, mediaType, true);
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriB, rdfTypeY, null, mediaType, true);
		result = renderWithSetting( null, mediaType, rdfTypeX,rdfTypeY);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriB + mediaType.toString(), result);

		renderletManager = createNewRenderletManager();
		// register a renderlet with two rendering specifications and with mode for different rdfType. Types-set
		// with both rdfTypes in it.
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, mode1, mediaType, true);
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriB, rdfTypeY, mode1, mediaType, true);
		result = renderWithSetting(mode1,mediaType, rdfTypeX, rdfTypeY);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriB + mediaType.toString(), result);
		
		renderletManager = createNewRenderletManager();
		// register a renderlet with two rendering specifications and with mode for different rdfType. The second 
		// renderlet has the rdf type "resource". Types-set with both rdfTypes in it.
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, mode1, mediaType, true);
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriB, RDFS.Resource, mode1, mediaType, true);
		result = renderWithSetting(mode1,mediaType, rdfTypeX, RDFS.Resource);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriA + mediaType.toString(), result);

		renderletManager = createNewRenderletManager();
		// register two renderlets with same rendering specification, no mode, but different
		// rdfTypes. Types-set with both rdfTypes in it
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, null, mediaType, true);
		renderletManager.registerRenderlet(renderletMockB.pid, renderSpecUriA, rdfTypeY, null, mediaType, true);
		result = renderWithSetting( null, mediaType, rdfTypeX,rdfTypeY);
		Assert.assertEquals(renderletMockB.outputForRenderSpecUriA + mediaType.toString(), result);

		renderletManager = createNewRenderletManager();
		// register with a non-existent renderlet with no mode.
		renderletManager.registerRenderlet("non existent renderlet", renderSpecUriA, rdfTypeX, null, mediaType, true);
		try {
			renderWithSetting( null, mediaType,rdfTypeX);
			Assert.assertTrue(false);
		} catch (RenderletNotFoundException ex) {}
	}

	@Test
	public void typedLiteralRenderingTest() throws Exception{
		MediaType mediaType = new MediaType ("application", "test");
		RenderletManager renderletManager = createNewRenderletManager();
		renderletManager.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, mode1, mediaType, true);
		GraphNode typedLiteral = new GraphNode(new TypedLiteralImpl("bla", rdfTypeX), new SimpleMGraph());
		String result = renderWithSetting(typedLiteral, mode1, mediaType);
		Assert.assertEquals(renderletMockA.outputForRenderSpecUriA + mediaType.toString(), result);
	}
	
	@Test
	public void testGetMediaType() {
		MediaType typeA = new MediaType("application", "A");
		MediaType typeB = new MediaType("application", "B");
		MediaType typeC = new MediaType("application", "C");

		RenderletManager renderer = createNewRenderletManager();
		renderer.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, null, typeB, true);
		renderer.registerRenderlet(renderletMockA.pid, renderSpecUriA, rdfTypeX, null, typeA, true);

		GraphNode resource = createGraphNode(rdfTypeX);
		MediaType resultMediaType = getRendererFactory().createRenderer(resource, null,
				createMediaTypeList(typeA, typeB, typeC)).getMediaType();
		Assert.assertEquals(typeA, resultMediaType);

		resultMediaType = getRendererFactory().createRenderer(resource, null,
				createMediaTypeList(typeB, typeA, typeC)).getMediaType();
		Assert.assertEquals(typeB, resultMediaType);

		renderer.registerRenderlet(renderletMockB.pid, renderSpecUriB, rdfTypeY, null, typeB, true);
		renderer.registerRenderlet(renderletMockB.pid, renderSpecUriB, rdfTypeX, null, typeC, true);

		resultMediaType = getRendererFactory().createRenderer(resource, null,
				createMediaTypeList(typeC, typeA, typeB)).getMediaType();
		Assert.assertEquals(typeC, resultMediaType);

		resultMediaType = getRendererFactory().createRenderer(resource, null,
				createMediaTypeList(typeB, typeA, typeC)).getMediaType();
		Assert.assertEquals(typeB, resultMediaType);

		resultMediaType = getRendererFactory().createRenderer(resource, null,
				createMediaTypeList(typeB, typeA, typeC, MediaType.WILDCARD_TYPE)).getMediaType();
		Assert.assertEquals(typeB, resultMediaType);
	}
	
	private List<MediaType> createMediaTypeList(MediaType... mediaTypes) {
		return Arrays.asList(mediaTypes);
	}

	private String renderWithSetting(String mode, MediaType mediaType,
			UriRef... types) throws IOException {
		GraphNode resource = createGraphNode(types);
		return renderWithSetting(resource, mode, mediaType);
	}
	
	private String renderWithSetting(GraphNode resource, String mode, MediaType mediaType) throws IOException {
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Renderer renderer = getRendererFactory().createRenderer(resource, mode,
				Collections.singletonList(mediaType));
		if (renderer == null) {
			throw new WebApplicationException();
		}
		renderer.render(resource, null, null, null, null, baos);
		return baos.toString();
	}	
	
	private GraphNode createGraphNode(UriRef... types) {
		SimpleMGraph mGraph = new SimpleMGraph();
		for (UriRef type : types) {
			Triple triple = new TripleImpl(resourceUri, RDF.type, type);
			mGraph.add(triple);
		}
		
		GraphNode node = new GraphNode(resourceUri, mGraph);
		return node;
	}
}