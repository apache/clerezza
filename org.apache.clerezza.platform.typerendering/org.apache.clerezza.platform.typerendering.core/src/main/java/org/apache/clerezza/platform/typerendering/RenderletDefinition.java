/*
 *  Copyright 2010 mir.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.apache.clerezza.platform.typerendering;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.regex.Pattern;
import javax.ws.rs.core.MediaType;
import org.apache.clerezza.platform.typerendering.ontologies.TYPERENDERING;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 *
 * @author mir
 */
public class RenderletDefinition {

	private BNode rederletDefinition;
	private String renderlet;
	private UriRef renderingSpecification;
	private UriRef rdfType;
	private String mode;
	private Pattern modePattern;
	private MediaType mediaType;
	private boolean builtIn;
	private LockableMGraph configGraph;

	/**
	 * creates A RenderletDefinition.
	 * This constructor expects configGraph to be read-locked
	 *
	 * @param renderletDefinition
	 * @param configGraph
	 */
	RenderletDefinition(BNode renderletDefinition, LockableMGraph configGraph) {
		this.configGraph = configGraph;
		this.rederletDefinition = renderletDefinition;
		renderlet = getRenderletName(renderletDefinition);
		renderingSpecification = getRenderingSpecification(renderletDefinition);
		rdfType = getRenderRdfType(renderletDefinition);
		mode = getMode(renderletDefinition);
		if (mode != null) {
			modePattern = Pattern.compile(mode);
		}
		mediaType = getMediaType(renderletDefinition);
		GraphNode node =
				new GraphNode(renderletDefinition, configGraph);
		builtIn = node.hasProperty(RDF.type,
				TYPERENDERING.BuiltInRenderletDefinition);
	}

	public BNode getNode() {
		return rederletDefinition;
	}

	public boolean isBuiltIn() {
		return builtIn;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public Pattern getModePattern() {
		return modePattern;
	}

	public String getMode() {
		return mode;
	}

	public UriRef getRdfType() {
		return rdfType;
	}

	public UriRef getRenderingSpecification() {
		return renderingSpecification;
	}

	public String getRenderlet() {
		return renderlet;
	}

	/**
	 * Returns the mode of the specified renderlet definition. Returns
	 * null if the renderlet definition has no renderlet-mode.
	 *
	 * @param contentGraph
	 * @param renderletDef
	 * @return
	 */
	private String getMode(Resource renderletDef) {
		Iterator<Triple> renderletModeIter = configGraph.filter(
				(NonLiteral) renderletDef, TYPERENDERING.renderingMode, null);
		if (renderletModeIter.hasNext()) {
			TypedLiteral renderletMode = (TypedLiteral) renderletModeIter.next().getObject();
			return LiteralFactory.getInstance().createObject(String.class,
					renderletMode);
		}
		return null;
	}

	private MediaType getMediaType(Resource renderletDef) {
		Iterator<Triple> mediaTypeIter = configGraph.filter(
				(NonLiteral) renderletDef, TYPERENDERING.mediaType, null);
		if (mediaTypeIter.hasNext()) {
			TypedLiteral renderletMode = (TypedLiteral) mediaTypeIter.next().getObject();
			String mediaTypeStr = LiteralFactory.getInstance().createObject(String.class,
					renderletMode);
			return MediaType.valueOf(mediaTypeStr);
		}
		return null;
	}

	/**
	 * Returns the rendering specification of the specified renderlet definition.
	 * Returns null if the renderlet definition has no rendering specification.

	 * @param renderletDef
	 * @return
	 */
	private UriRef getRenderingSpecification(Resource renderletDef) {
		Iterator<Triple> renderSpecIter = configGraph.filter(
				(NonLiteral) renderletDef, TYPERENDERING.renderingSpecification, null);
		if (renderSpecIter.hasNext()) {
			return (UriRef) renderSpecIter.next().getObject();
		}
		return null;
	}

	private String getRenderletName(Resource renderletDef) {

		Lock l = configGraph.getLock().readLock();
		l.lock();
		try {
			Iterator<Triple> renderletModeIter = configGraph.filter(
					(NonLiteral) renderletDef, TYPERENDERING.renderlet, null);
			if (renderletModeIter.hasNext()) {
				TypedLiteral renderletMode = (TypedLiteral) renderletModeIter.next().getObject();
				String renderletName = LiteralFactory.getInstance().createObject(String.class,
						renderletMode);
				return renderletName;
			}
		} finally {
			l.unlock();
		}
		return null;
	}

	/**
	 * Returns the renderlet rdf-type of the specified renderlet definition.
	 * Returns null if the renderlet definition has no renderlet rdf-type.
	 *
	 * @param renderletDef
	 * @return
	 */
	private UriRef getRenderRdfType(Resource renderletDef) {
		Iterator<Triple> renderedTypeIter = configGraph.filter(
				(NonLiteral) renderletDef, TYPERENDERING.renderedType, null);

		if (renderedTypeIter.hasNext()) {
			return (UriRef) renderedTypeIter.next().getObject();
		}
		return null;
	}

}
