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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;
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
import org.apache.clerezza.platform.typerendering.ontologies.TYPERENDERING;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
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
	private LockableMGraph configGraph;

	private static final String RDF_TYPE_PRIO_LIST_URI =
			"http://tpf.localhost/rdfTypePriorityList";

	private Map<UriRef, RenderletDefinition[]> type2DefinitionMap = null;
	/**
	 * Mapping of service pid's of renderlets to the service objects.
	 */
	private Map<String, Renderlet> renderletMap = new HashMap<String, Renderlet>();
	private ComponentContext componentContext;
	private ReentrantReadWriteLock configLock = new ReentrantReadWriteLock();
	private final Set<ServiceReference> renderletRefStore =
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
		SortedSet<RendererImpl> configurationList =
				new TreeSet<RendererImpl>();
		for (Resource prioRdfType : rdfTypePrioList) {
			if (!rdfTypes.contains(prioRdfType)) {
				continue;
			}
			RenderletDefinition[] renderletDefs = getType2DefinitionMap().get(prioRdfType);
			if (renderletDefs == null) {
				continue;
			}
			for (RenderletDefinition renderletDef : renderletDefs) {
				MediaType mediaTypeInGraph = renderletDef.getMediaType();
				int prio = -1;
				MediaType mediaTypeRequested = null;
				for (int i = 0; i < acceptableMediaTypes.size(); i++) {
					mediaTypeRequested = acceptableMediaTypes.get(i);
					if (mediaTypeRequested.isCompatible(mediaTypeInGraph)) {
						prio = i;
						break;
					}
				}
				if (prio == -1) {
					continue;
				}
				Pattern renderingModePattern = renderletDef.getModePattern();
				if (mode == renderletDef.getMode()
						|| (mode == null && renderingModePattern != null && renderingModePattern.matcher("").matches())
						|| (mode != null && renderingModePattern != null && renderingModePattern.matcher(mode).matches())) {
					final String renderletName = renderletDef.getRenderlet();
					Renderlet renderlet = renderletMap.get(renderletName);
					if (renderlet == null) {
						throw new RenderletNotFoundException("Renderlet " + renderletName + " could not be loaded.");
					}
					configurationList.add(new RendererImpl(
							renderletDef.getRenderingSpecification(),
							renderlet,
							mode,
							getMostConcreteMediaType(mediaTypeRequested, mediaTypeInGraph),
							prio, RenderletRendererFactoryImpl.this,
							renderletDef.isBuiltIn()));
				}
			}
			if (!configurationList.isEmpty()) {
				return configurationList.first();
			}
		}
		return null;
	}

	private MediaType getMostConcreteMediaType(MediaType a, MediaType b) {
		if (a == null) {
			return b;
		}
		if (b == null) {
			return a;
		}
		if (!a.isWildcardType()) {
			return a;
		}
		if (!b.isWildcardType()) {
			return b;
		}
		if (!a.isWildcardSubtype()) {
			return a;
		}
		return b;
	}

	@Override
	public void registerRenderlet(String renderlet,
			UriRef renderingSpecification,
			UriRef rdfType,
			String mode,
			MediaType mediaType, boolean builtIn) {
		Lock l = configLock.writeLock();
		l.lock();
		try {
			removeExisting(rdfType, mode, mediaType, builtIn);
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
		} finally {
			l.unlock();
		}
		type2DefinitionMap = null;
	}

	private void removeExisting(UriRef rdfType, String mode,
			MediaType mediaType, boolean builtIn) {


		GraphNode existing = findDefinition(rdfType, mode, mediaType, builtIn);
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
			MediaType mediaType, boolean builtIn) {

		//keeping this independent of typedefinitionmap to allow better performance
		List<RenderletDefinition> definitionList = new ArrayList<RenderletDefinition>();
		Lock l = configGraph.getLock().readLock();
		l.lock();
		try {
			Iterator<Triple> renderletDefsTriple =
					configGraph.filter(null, TYPERENDERING.renderedType, rdfType);
			while (renderletDefsTriple.hasNext()) {
				definitionList.add(
						new RenderletDefinition((BNode) renderletDefsTriple.next().getSubject(),
						configGraph));
			}
		} finally {
			l.unlock();
		}
		for (RenderletDefinition renderletDef : definitionList) {

			if (builtIn && !renderletDef.isBuiltIn()) {
				continue;
			}
			if (!equals(mediaType, renderletDef.getMediaType())) {
				continue;
			}			
			if (!equals(renderletDef.getMode(), mode)) {
				continue;
			}
			return new GraphNode(renderletDef.getNode(), configGraph);
		}
		return null;
	}

	private static boolean equals(Object o1, Object o2) {
		return o1 == o2 ||
				(o1 != null) && o1.equals(o2);
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
		Lock l = configLock.writeLock();
		l.lock();
		try {
			if (!renderletRefStore.remove(renderletRef)) {
				String servicePid = (String) renderletRef.getProperty(Constants.SERVICE_PID);
				Renderlet renderlet = (Renderlet) componentContext.locateService("renderlet", renderletRef);
				unregisterRenderletService(servicePid, renderlet);
			}
		} finally {
			l.unlock();
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
		synchronized(renderletRefStore) {
			for (ServiceReference renderletRef : renderletRefStore) {
				registerRenderletService(renderletRef);
			}
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

	private Map<UriRef, RenderletDefinition[]> getType2DefinitionMap() {
		if (type2DefinitionMap == null) {
			synchronized(this) {
				if (type2DefinitionMap == null) {
					createType2DefinitionMap();
				}
			}
		}
		return type2DefinitionMap;
	}

	private void createType2DefinitionMap() {
		type2DefinitionMap = new HashMap<UriRef, RenderletDefinition[]>(50);
		Lock l = configGraph.getLock().readLock();
		for (Resource prioRdfType : rdfTypePrioList) {
			l.lock();
			try {
				Iterator<Triple> renderletDefs =
						configGraph.filter(null, TYPERENDERING.renderedType, prioRdfType);
				ArrayList<RenderletDefinition> definitionList = new ArrayList<RenderletDefinition>();
				while (renderletDefs.hasNext()) {
					definitionList.add(
							new RenderletDefinition((BNode) renderletDefs.next().getSubject(),
							configGraph));
				}
				type2DefinitionMap.put((UriRef) prioRdfType,
						definitionList.toArray(new RenderletDefinition[definitionList.size()]));
			} finally {
				l.unlock();
			}
		}
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

	protected void bindConfigGraph(LockableMGraph configGraph) {
		this.configGraph = configGraph;
	}

	protected void unbindConfigGraph(LockableMGraph configGraph) {
		this.configGraph = null;
	}
}
