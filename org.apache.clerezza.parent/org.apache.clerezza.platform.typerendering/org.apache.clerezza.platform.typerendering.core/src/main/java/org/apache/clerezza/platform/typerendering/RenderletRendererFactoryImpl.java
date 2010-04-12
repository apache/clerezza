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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.ws.rs.core.MediaType;
import org.apache.clerezza.platform.config.PlatformConfig;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.apache.clerezza.rdf.core.event.GraphEvent;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.typerendering.ontologies.TYPERENDERING;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphListener;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.ontologies.RDFS;

/**
 *
 * @author mir
 */
@Component
@Services({
	@Service(RenderletManager.class),
	@Service(RendererFactory.class)
})
@Reference(name = "renderlet",
cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
policy = ReferencePolicy.DYNAMIC,
referenceInterface = Renderlet.class)
public class RenderletRendererFactoryImpl implements RenderletManager, RendererFactory,
		GraphListener {

	private Logger logger = LoggerFactory.getLogger(RenderletRendererFactoryImpl.class);
	
	@Reference(target = PlatformConfig.CONFIG_GRAPH_FILTER)
	private MGraph configGraph;

	private static final String RDF_TYPE_PRIO_LIST_URI =
			"http://tpf.localhost/rdfTypePriorityList";
	/**
	 * Mapping of service pid's of renderlets to the service objects.
	 */
	private Map<String, Renderlet> renderletMap = new HashMap<String, Renderlet>();
	private ComponentContext componentContext;
	private ReentrantReadWriteLock configLock = new ReentrantReadWriteLock();
	private Set<ServiceReference> renderletRefStore =
			Collections.synchronizedSet(new HashSet<ServiceReference>());
	List<Resource> rdfTypePrioList;

	@Override
	public Renderer createRenderer(GraphNode resource, String mode, List<MediaType> acceptableMediaTypes) {
		Set<UriRef> types = new HashSet<UriRef>();
		if (resource.getNode() instanceof TypedLiteral) {
			types.add(((TypedLiteral) resource.getNode()).getDataType());
		} else {
			// extract rdf types
			Iterator<UriRef> it = resource.getUriRefObjects(RDF.type);
			while (it.hasNext()) {
				final UriRef rdfType = it.next();
				types.add(rdfType);
			}
			types.add(RDFS.Resource);
		}
		return getRenderer(types, mode, acceptableMediaTypes);
	}

	private RendererImpl getRenderer(final Set<UriRef> rdfTypes,
			final String mode, final List<MediaType> acceptableMediaTypes) {
		//this is done as priviledged as the user need not have right to read from
		//the content graph for the locating the template (this is needed e.g.
		//to show a login page)
		return AccessController.doPrivileged(new PrivilegedAction<RendererImpl>() {
			@Override
			public RendererImpl run() {
				SortedSet<RendererImpl> configurationList =
						new TreeSet<RendererImpl>();
				for (Resource prioRdfType : rdfTypePrioList) {
					if (!rdfTypes.contains(prioRdfType)) {
						continue;
					}
					Iterator<Triple> renderletDefs =
							configGraph.filter(null, TYPERENDERING.renderedType, prioRdfType);
					while (renderletDefs.hasNext()) {
						NonLiteral renderletDef = renderletDefs.next().getSubject();
						GraphNode renderletDefNode = new GraphNode(renderletDef,
								configGraph);
						String renderingModeStr = getMode(configGraph,
								renderletDef);
						MediaType mediaTypeInGraph = getMediaType(configGraph, renderletDef);
						int prio = -1;
						for (int i = 0; i < acceptableMediaTypes.size(); i++) {
							MediaType acceptableMediaType = acceptableMediaTypes.get(i);
							if (acceptableMediaType.isCompatible(mediaTypeInGraph)) {
								prio = i;
								break;
							}
						}
						if (prio == -1) {
							continue;
						}
						if (RenderletRendererFactoryImpl.equals(renderingModeStr, mode)) {
							final String renderletName = getRenderletName(configGraph, renderletDef);
							Renderlet renderlet = renderletMap.get(renderletName);
							if (renderlet == null) {
								throw new RenderletNotFoundException("Renderlet " + renderletName + " could not be loaded.");
							}
							configurationList.add(new RendererImpl(
									getRenderingSpecification(configGraph, renderletDef),
									renderlet,
									mode,
									mediaTypeInGraph,
									prio, new CallbackRendererImpl(RenderletRendererFactoryImpl.this, mediaTypeInGraph),
									renderletDefNode.hasProperty(RDF.type,
									TYPERENDERING.BuiltInRenderletDefinition)));
						}

					}
					if (!configurationList.isEmpty()) {
						return configurationList.first();
					}
				}
				return null;
			}
		});

	}

	/**
	 * Returns the mode of the specified renderlet definition. Returns
	 * null if the renderlet definition has no renderlet-mode.
	 *
	 * @param contentGraph
	 * @param renderletDef
	 * @return
	 */
	private String getMode(MGraph contentGraph, Resource renderletDef) {
		Iterator<Triple> renderletModeIter = contentGraph.filter(
				(NonLiteral) renderletDef, TYPERENDERING.renderingMode, null);
		if (renderletModeIter.hasNext()) {
			TypedLiteral renderletMode = (TypedLiteral) renderletModeIter.next().getObject();
			return LiteralFactory.getInstance().createObject(String.class,
					renderletMode);
		}
		return null;
	}

	/**
	 * Returns the renderlet rdf-type of the specified renderlet definition.
	 * Returns null if the renderlet definition has no renderlet rdf-type.
	 *
	 * @param contentGraph
	 * @param renderletDef
	 * @return
	 */
	private UriRef getRenderRdfType(MGraph contentGraph, Resource renderletDef) {
		Iterator<Triple> renderedTypeIter = contentGraph.filter(
				(NonLiteral) renderletDef, TYPERENDERING.renderedType, null);

		if (renderedTypeIter.hasNext()) {
			return (UriRef) renderedTypeIter.next().getObject();
		}
		return null;
	}

	/**
	 * Returns the rendering specification of the specified renderlet definition.
	 * Returns null if the renderlet definition has no rendering specification.
	 *
	 * @param contentGraph
	 * @param renderletDef
	 * @return
	 */
	private UriRef getRenderingSpecification(MGraph contentGraph,
			Resource renderletDef) {
		Iterator<Triple> renderSpecIter = contentGraph.filter(
				(NonLiteral) renderletDef, TYPERENDERING.renderingSpecification, null);
		if (renderSpecIter.hasNext()) {
			return (UriRef) renderSpecIter.next().getObject();
		}
		return null;
	}

	private String getRenderletName(MGraph contentGraph, Resource renderletDef) {

		Iterator<Triple> renderletModeIter = contentGraph.filter(
				(NonLiteral) renderletDef, TYPERENDERING.renderlet, null);
		if (renderletModeIter.hasNext()) {
			TypedLiteral renderletMode = (TypedLiteral) renderletModeIter.next().getObject();
			String renderletName = LiteralFactory.getInstance().createObject(String.class,
					renderletMode);
			return renderletName;
		}
		return null;
	}

	private MediaType getMediaType(MGraph contentGraph, Resource renderletDef) {
		Iterator<Triple> mediaTypeIter = contentGraph.filter(
				(NonLiteral) renderletDef, TYPERENDERING.mediaType, null);
		if (mediaTypeIter.hasNext()) {
			TypedLiteral renderletMode = (TypedLiteral) mediaTypeIter.next().getObject();
			String mediaTypeStr = LiteralFactory.getInstance().createObject(String.class,
					renderletMode);
			return MediaType.valueOf(mediaTypeStr);
		}
		return null;
	}

	@Override
	public void registerRenderlet(String renderlet,
			UriRef renderingSpecification,
			UriRef rdfType,
			String mode,
			MediaType mediaType, boolean builtIn) {
		removeExisting(rdfType, mode, mediaType, builtIn, configGraph);
		BNode renderletDefinition = new BNode();
		GraphNode renderletDefinitionNode = new GraphNode(renderletDefinition, configGraph);
		configGraph.add(new TripleImpl(renderletDefinition,
				TYPERENDERING.renderlet, LiteralFactory.getInstance().createTypedLiteral(renderlet)));
		if (renderingSpecification != null) {
			configGraph.add(new TripleImpl(renderletDefinition,
					TYPERENDERING.renderingSpecification, renderingSpecification));
		}
		configGraph.add(new TripleImpl(renderletDefinition,
				TYPERENDERING.renderedType, rdfType));

		configGraph.add(new TripleImpl(renderletDefinition,
				TYPERENDERING.mediaType, LiteralFactory.getInstance().createTypedLiteral(mediaType.toString())));
		renderletDefinitionNode.addProperty(RDF.type, TYPERENDERING.RenderletDefinition);

		if (builtIn) {
			renderletDefinitionNode.addProperty(RDF.type,
					TYPERENDERING.BuiltInRenderletDefinition);
		} else {
			renderletDefinitionNode.addProperty(RDF.type,
					TYPERENDERING.CustomRenderletDefinition);
		}

		if (mode != null) {
			configGraph.add(new TripleImpl(renderletDefinition,
					TYPERENDERING.renderingMode, LiteralFactory.getInstance().createTypedLiteral(mode)));
		}

		synchronized(this) {
			if (!rdfTypePrioList.contains(rdfType)) {
				if (rdfType.equals(RDFS.Resource)) {
					rdfTypePrioList.add(RDFS.Resource);
				} else {
					rdfTypePrioList.add(0, rdfType);
				}
			}
		}
	}

	private void removeExisting(UriRef rdfType, String mode,
			MediaType mediaType, boolean builtIn, MGraph contentGraph) {


		GraphNode existing = findDefinition(rdfType, mode, mediaType, builtIn,
				contentGraph);
		if (existing == null) {
			return;
		}
		existing.deleteProperties(RDF.type);
		existing.deleteProperties(TYPERENDERING.mediaType);
		existing.deleteProperties(TYPERENDERING.renderedType);
		existing.deleteProperties(TYPERENDERING.renderingMode);
		existing.deleteProperties(TYPERENDERING.renderingSpecification);
		existing.deleteProperties(TYPERENDERING.renderlet);


	}

	private GraphNode findDefinition(UriRef rdfType, String mode,
			MediaType mediaType, boolean builtIn, MGraph contentGraph) {
		Iterator<Triple> renderletDefs =
				contentGraph.filter(null, TYPERENDERING.renderedType, rdfType);



		while (renderletDefs.hasNext()) {
			NonLiteral renderletDef = renderletDefs.next().getSubject();
			GraphNode node = new GraphNode(renderletDef, contentGraph);
			if (!rdfType.equals(getRenderRdfType(contentGraph, renderletDef))) {
				continue;
			}
			if (!equals(mediaType, getMediaType(contentGraph, renderletDef))) {
				continue;
			}
			String modeInGraph = getMode(contentGraph,
					renderletDef);
			if (!equals(modeInGraph, mode)) {
				continue;
			}
			if (builtIn && !node.hasProperty(RDF.type,
					TYPERENDERING.BuiltInRenderletDefinition)) {
				continue;
			}
			if (!builtIn && !node.hasProperty(RDF.type,
					TYPERENDERING.CustomRenderletDefinition)) {
				continue;
			}
			return node;
		}
		return null;
	}

	private static boolean equals(Object o1, Object o2) {
		return o1 == o2 ||
				(o1 != null) && o1.equals(o2);
	}

	protected void bindConfigGraph(MGraph configGraph) {
		this.configGraph = configGraph;
	}

	protected void unbindContentGraphProvider(ContentGraphProvider contentGraphProvider) {
		this.configGraph = null;
	}

	protected void bindRenderlet(ServiceReference renderletRef) {
		logger.info("Bind renderlet of bundle {}", renderletRef.getBundle().getSymbolicName());
		if (componentContext != null) {
			registerRenderletService(renderletRef);
		} else {
			renderletRefStore.add(renderletRef);
		}
	}

	private void registerRenderletService(ServiceReference renderletRef) {
		String servicePid = (String) renderletRef.getProperty(Constants.SERVICE_PID);
		Renderlet renderlet = (Renderlet) componentContext.locateService("renderlet", renderletRef);
		registerRenderletService(servicePid, renderlet);
	}

	protected void registerRenderletService(String servicePid, Renderlet renderlet) {
		configLock.writeLock().lock();
		try {
			if (renderletMap.get(servicePid) == null) {
				renderletMap.put(servicePid, renderlet);
			}
		} finally {
			configLock.writeLock().unlock();
		}
	}

	protected void unbindRenderlet(ServiceReference renderletRef) {
		logger.info("Unbind renderlet of bundle {}", renderletRef.getBundle().getSymbolicName());
		configLock.writeLock().lock();
		try {
			if (!renderletRefStore.remove(renderletRef)) {
				String servicePid = (String) renderletRef.getProperty(Constants.SERVICE_PID);
				Renderlet renderlet = (Renderlet) componentContext.locateService("renderlet", renderletRef);
				unregisterRenderletService(servicePid, renderlet);
			}
		} finally {
			configLock.writeLock().unlock();
		}
	}

	protected void unregisterRenderletService(String servicePid, Renderlet renderlet) {
		logger.debug("unregistering {} with pid {}", renderlet, servicePid);
		configLock.writeLock().lock();
		try {
			renderletMap.remove(servicePid);
		} finally {
			configLock.writeLock().unlock();
		}
	}

	private void registerRenderletsFromStore() {
		for (ServiceReference renderletRef : renderletRefStore) {
			registerRenderletService(renderletRef);
		}
		renderletRefStore.clear();
	}

	/**
	 * The activate method is called when SCR activates the component
	 * configuration
	 *
	 * @param componentContext
	 */
	protected void activate(ComponentContext componentContext) {
		graphChanged(null);
		configGraph.addGraphListener(this,
				new FilterTriple(null, RDF.first, null), 1000);
		this.componentContext = componentContext;
		registerRenderletsFromStore();
	}

	/**
	 * The deactivate method is called when SCR deactivates the component
	 * configuration
	 *
	 * @param componentContext
	 */
	protected void deactivate(ComponentContext componentContext) {
		configGraph.removeGraphListener(this);
	}

	@Override
	public void graphChanged(List<GraphEvent> events) {
		synchronized(this) {
			rdfTypePrioList = Collections.synchronizedList(
					new RdfList(new UriRef(RDF_TYPE_PRIO_LIST_URI),	configGraph));
		}
	}
}
