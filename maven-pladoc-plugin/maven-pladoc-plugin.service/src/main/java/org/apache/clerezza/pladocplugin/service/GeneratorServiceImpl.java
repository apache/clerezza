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
package org.apache.clerezza.pladocplugin.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.pladocplugin.api.GeneratorService;
import org.apache.clerezza.platform.typerendering.Renderer;
import org.apache.clerezza.platform.typerendering.RendererFactory;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.ontologies.DISCOBITS;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.wymiwyg.wrhapi.Handler;

/**
 *
 * @author reto
 */
@Component(immediate = true)
@Service(GeneratorService.class)
public class GeneratorServiceImpl implements GeneratorService {

	@Reference
	Parser parser;
	@Reference
	RendererFactory rendererFactory;
	/**
	 * this is just to activate Triaxrs in order for MediaType to work
	 */
	@Reference
	Handler triaxrs;

	protected void activate(ComponentContext componentContext) {
		System.out.println("activating generator service");
	}

	@Override
	public void process(File ntFile, File outputDir) {

		try {
			System.out.println("attempt to load");
			Class clazz = Class.forName("javax.ws.rs.core.MediaType");
			System.out.println("clazz : " + clazz);
			System.out.println("clazz.getMethods().length : " + clazz.getMethods().length);
			//System.out.println("clazz : "+clazz.n);
			System.out.println("processing " + ntFile);
			InputStream in = null;
			Graph documentationGraph = null;
			try {
				in = new FileInputStream(ntFile);
				documentationGraph = parser.parse(in, SupportedFormat.N_TRIPLE);
			} finally {
				in.close();
			}
			process(documentationGraph, outputDir);
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
			throw new RuntimeException(ex);
		}

	}

	private String generateFileNameFromUri(UriRef uriRef) {
		String uriString = uriRef.getUnicodeString();
		if (uriString.endsWith("/")) {
			return "index";
		}
		String lastSection = uriString.substring(uriString.lastIndexOf('/'));
		return lastSection;
	}

	private void process(Graph documentationGraph, File outputDir) throws IOException {
		Set<NonLiteral> docRoots = new HashSet<NonLiteral>();
		Iterator<Triple> titledContentTypeTriples =
				documentationGraph.filter(null, RDF.type, DISCOBITS.TitledContent);
		while (titledContentTypeTriples.hasNext()) {
			NonLiteral titleContent = titledContentTypeTriples.next().getSubject();
			if (!documentationGraph.filter(null, DISCOBITS.holds, titleContent).hasNext()) {
				docRoots.add(titleContent);
				System.out.println("doc root: " + titleContent);
			}
		}
		for (NonLiteral docRoot : docRoots) {
			String fileName = generateFileNameFromUri((UriRef) docRoot) + ".html";
			File outFile = new File(outputDir, fileName);
			createFile(docRoot, documentationGraph, outFile);
		}
	}

	private void createFile(NonLiteral docRoot, Graph documentationGraph,
			File outFile) throws IOException {
		GraphNode docRootNode = new GraphNode(docRoot, documentationGraph);
			Renderer renderer = rendererFactory.createRenderer(docRootNode, null,
					Collections.singletonList(MediaType.APPLICATION_XHTML_XML_TYPE));
			FileOutputStream out = new FileOutputStream(outFile);
			try {
				System.out.println("writing " + outFile);
				renderer.render(docRootNode, docRootNode, null, null, null, null, new HashMap<String, Object>(), out);
			} finally {
				out.close();
			}
	}
}
