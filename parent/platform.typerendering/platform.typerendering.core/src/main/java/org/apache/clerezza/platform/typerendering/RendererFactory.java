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


import java.util.*;
import javax.ws.rs.core.MediaType;
import org.apache.clerezza.platform.graphnodeprovider.GraphNodeProvider;
import org.apache.clerezza.platform.typepriority.TypePrioritizer;
import org.apache.clerezza.platform.typerendering.utils.MediaTypeMap;
import org.apache.clerezza.platform.typerendering.utils.RegexMap;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.startlevel.StartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a <code>Renderer</code> which can used to render a <code>GraphNode</code>.
 *
 * @author mir, reto
 */
@Component
@Services({
	@Service(RendererFactory.class)
})
@Reference(name = "typeRenderlet",
cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
policy = ReferencePolicy.DYNAMIC,
referenceInterface = TypeRenderlet.class)
public class RendererFactory {

	private Logger logger = LoggerFactory.getLogger(RendererFactory.class);
	
	@Reference
	private TypePrioritizer typePrioritizer;

	@Reference
	private StartLevel startLevelService;

	@Reference
	private GraphNodeProvider graphNodeProvider;

	/**
	 * A Tuple Type-Renderler Startlevel, for identity only the renderlet is relevan
	 */
	private static class TypeRenderletStartLevel {
		TypeRenderlet renderlet;
		int startLevel;

		private TypeRenderletStartLevel(TypeRenderlet renderlet, int startLevel) {
			this.startLevel = startLevel;
			this.renderlet = renderlet;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			TypeRenderletStartLevel that = (TypeRenderletStartLevel) o;
			return renderlet.equals(that.renderlet);
		}

		@Override
		public int hashCode() {
			return renderlet.hashCode();
		}
	}

	private Map<UriRef, RegexMap<MediaTypeMap<TypeRenderletStartLevel>>> typeRenderletMap =
			Collections.synchronizedMap(new HashMap<UriRef, RegexMap<MediaTypeMap<TypeRenderletStartLevel>>>());

	private BundleContext bundleContext;

	private Set<ServiceReference> pendingRenderletRegistrations = new HashSet<ServiceReference>();

	protected void activate(ComponentContext componentContext) {
		bundleContext = componentContext.getBundleContext();
		for (ServiceReference r : pendingRenderletRegistrations) {
			registerTypeRenderlet(r);
		}
	}

	protected void deactivate(ComponentContext componentContext) {
		bundleContext = null;
	}

	/**
	 * Creates a <code>Renderer</code> for the specified mode, acceptable 
	 * media-types as well as the types of <code>GraphNode</code>.
	 * The <code>acceptableMediaTypes</code> list represent the media
	 * types that are acceptable for the rendered output. The list has a
	 * order where the most desirable media type is a the beginning of the list.
	 * The media type of the rendered output will be compatible to at least one
	 * media type in the list.
	 *
	 * @param resource The <code>GraphNode</code> to be rendered
	 * @param mode mode
	 * @param acceptableMediaTypes acceptable media types for the rendered output
	 * @return the Renderer or null if no renderer could be created for the specified parameters
	 */
	public Renderer createRenderer(GraphNode resource, String mode,
			List<MediaType> acceptableMediaTypes) {
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

	private Renderer getRenderer(Set<UriRef> types, String mode,
			List<MediaType> acceptableMediaTypes) {
		Iterator<UriRef> sortedTypes = typePrioritizer.iterate(types);
		while (sortedTypes.hasNext()) {
			final UriRef currentType = sortedTypes.next();
			final RegexMap<MediaTypeMap<TypeRenderletStartLevel>> regexMap = typeRenderletMap.get(currentType);
			if (regexMap != null) {
				Iterator<MediaTypeMap<TypeRenderletStartLevel>> mediaTypeMapIter = regexMap.getMatching(mode);
				while (mediaTypeMapIter.hasNext()) {
					MediaTypeMap<TypeRenderletStartLevel> mediaTypeMap = mediaTypeMapIter.next();
					for (MediaType acceptableType : acceptableMediaTypes) {
						Iterator<TypeRenderletStartLevel> renderlets = mediaTypeMap.getMatching(acceptableType);
						if (renderlets.hasNext()) {
							TypeRenderlet bestRenderlet = null;
							int highestStartLevel = 0;
							while (renderlets.hasNext()) {
								TypeRenderletStartLevel typeRenderletStartLevel = renderlets.next();
								if (typeRenderletStartLevel.startLevel > highestStartLevel) {
									highestStartLevel = typeRenderletStartLevel.startLevel;
									bestRenderlet = typeRenderletStartLevel.renderlet;
								}
							}
							return new TypeRenderletRendererImpl(
								bestRenderlet,
								acceptableType,
								this, graphNodeProvider,
								bundleContext);
						}
					}
				}
			}
		}
		return null;
	}

	protected void bindTypeRenderlet(ServiceReference serviceReference) {
		if (bundleContext == null) {
			pendingRenderletRegistrations.add(serviceReference);
		} else {
			registerTypeRenderlet(serviceReference);
		}
	}

	private void registerTypeRenderlet(ServiceReference serviceReference) {
		int startLevel = startLevelService.getBundleStartLevel(serviceReference.getBundle());
		TypeRenderlet renderlet = (TypeRenderlet) bundleContext.getService(serviceReference);
		registerRenderlet(renderlet, startLevel);
	}

	private void registerRenderlet(TypeRenderlet typeRenderlet, int startLevel) {
		final UriRef rdfType = typeRenderlet.getRdfType();
		RegexMap<MediaTypeMap<TypeRenderletStartLevel>> regexMap = typeRenderletMap.get(rdfType);
		if (regexMap == null) {
			regexMap = new RegexMap<MediaTypeMap<TypeRenderletStartLevel>>();
			typeRenderletMap.put(rdfType, regexMap);
		}
		final String mode = typeRenderlet.getModePattern();
		MediaTypeMap<TypeRenderletStartLevel> mediaTypeMap = regexMap.getFirstExactMatch(mode);
		if (mediaTypeMap == null) {
			mediaTypeMap = new MediaTypeMap<TypeRenderletStartLevel>();
			regexMap.addEntry(mode, mediaTypeMap);
		}
		final MediaType mediaType = typeRenderlet.getMediaType();
		mediaTypeMap.addEntry(mediaType, new TypeRenderletStartLevel(typeRenderlet, startLevel));
	}

	protected void unbindTypeRenderlet(TypeRenderlet typeRenderlet) {
		TypeRenderletStartLevel typeRenderletStartLevel = new TypeRenderletStartLevel(typeRenderlet, 0);
		for (Map.Entry<UriRef, RegexMap<MediaTypeMap<TypeRenderletStartLevel>>> typeEntry: typeRenderletMap.entrySet()) {
			final RegexMap<MediaTypeMap<TypeRenderletStartLevel>> regexMap = typeEntry.getValue();
			for (Map.Entry<String, MediaTypeMap<TypeRenderletStartLevel>> regexEntry: regexMap.entrySet()) {
				final MediaTypeMap<TypeRenderletStartLevel> mediaTypeMap = regexEntry.getValue();
				if (mediaTypeMap.remove(typeRenderletStartLevel)) {
					//for now we just leave the potentially empty mediaTypeMap there
					//IMPROVEMENT remove without entries
					return;
				}
			}
		}
	}

}
