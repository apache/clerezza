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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.ws.rs.core.MediaType;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Services({
	@Service(RenderletManager.class)
})
@Reference(name = "renderlet",
cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
policy = ReferencePolicy.DYNAMIC,
referenceInterface = Renderlet.class)
@Deprecated
public class RenderletManagerImpl implements RenderletManager {
	private Collection<RegistrationRequest> pendingRequests
			= new HashSet<RegistrationRequest>();



	private static class RegistrationRequest {

		String renderletServiceName;
		UriRef renderingSpecification;
		UriRef rdfType;
		String mode;
		MediaType mediaType;

		public RegistrationRequest(String renderletServiceName, UriRef renderingSpecification, UriRef rdfType, String mode, MediaType mediaType) {
			this.renderletServiceName = renderletServiceName;
			this.renderingSpecification = renderingSpecification;
			this.rdfType = rdfType;
			this.mode = mode;
			this.mediaType = mediaType;
		}
	}
	private Logger logger = LoggerFactory.getLogger(RenderletManagerImpl.class);
	private ComponentContext componentContext;
	private final Set<ServiceReference> renderletRefStore =
			Collections.synchronizedSet(new HashSet<ServiceReference>());
	private final Set<ServiceRegistration> registeredTypeRenderlets =
			Collections.synchronizedSet(new HashSet<ServiceRegistration>());
	private ReentrantReadWriteLock configLock = new ReentrantReadWriteLock();
	/**
	 * maps from service-id to the renderlet
	 */
	private Map<String, Renderlet> renderletMap = new HashMap<String, Renderlet>();
	private BundleContext bundleContext;

	/**
	 * Registeres a renderlet.
	 * 
	 * For the same rdfType, mediaType and Mode at motst one built-in and one 
	 * non-built-in renderlet can be registered. An attempt to register a second 
	 * renderlet results in the unregistration of the previously registered one 
	 * 
	 * @param renderingSpecification the argument that is passed to the
	 * 	renderlet.
	 * @param rdfType defines the RDF-type to be rendered with specified renderlet
	 * 	and renderingSpecification and mode.
	 * @param mode defines the mode in which the renderlet and rendering
	 *		specification has to be used.
	 *		mode may be null, that indicates that it is only used when no mode is
	 *		required.
	 * @param mediaType The media type of the rendered
	 * @param builtIn ignored
	 */
	public void registerRenderlet(String renderletServiceName,
			final UriRef renderingSpecification,
			final UriRef rdfType,
			final String mode,
			final MediaType mediaType, boolean builtIn) {
		RegistrationRequest registrationRequest = new RegistrationRequest(renderletServiceName,
				renderingSpecification,
				rdfType,
				mode,
				mediaType);
		if (!attemptRegistration(registrationRequest)) {
			logger.info("no renderlet service registered for "
					+ renderletServiceName + ", will retry later.");
			pendingRequests.add(registrationRequest);
		}
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
		handlePendingRegistrations();
	}

	protected void unbindRenderlet(ServiceReference renderletRef) {
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
		synchronized (renderletRefStore) {
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
		bundleContext = componentContext.getBundleContext();
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
		for (ServiceRegistration registration : registeredTypeRenderlets) {
			registration.unregister();
		}
		registeredTypeRenderlets.clear();
		bundleContext = null;
	}

	private void handlePendingRegistrations() {
		Lock l = configLock.readLock();
		l.lock();
		try {
			Iterator<RegistrationRequest> iterator = pendingRequests.iterator();
			while (iterator.hasNext()) {
				RegistrationRequest pending = iterator.next();
				if (attemptRegistration(pending)) {
					iterator.remove();
				}
			}
		} finally {
			l.unlock();
		}
	}

	private boolean attemptRegistration(RegistrationRequest registrationRequest) {
		final String renderletServiceName = registrationRequest.renderletServiceName;
		UriRef renderingSpecification = registrationRequest.renderingSpecification;
		final UriRef rdfType = registrationRequest.rdfType;
		final String modePattern = registrationRequest.mode;
		final MediaType mediaType = registrationRequest.mediaType;

		final Renderlet renderlet = renderletMap.get(renderletServiceName);
		if (renderlet == null) {
			return false;
		}
		final URI renderingSpecificationUri;
		try {
			renderingSpecificationUri = renderingSpecification != null
					? new URI(renderingSpecification.getUnicodeString())
					: null;
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
		TypeRenderlet typeRenderlet = new TypeRenderlet() {

			@Override
			public UriRef getRdfType() {
				return rdfType;
			}

			@Override
			public String getModePattern() {
				return modePattern;
			}

			@Override
			public MediaType getMediaType() {
				return mediaType;
			}

			@Override
			public void render(GraphNode node, GraphNode context,
					Map<String, Object> sharedRenderingValues,
					CallbackRenderer callbackRenderer,
					RequestProperties typeRenderletRequestProperties,
					OutputStream os) throws IOException {
				Renderlet.RequestProperties renderletRequestProperties =
						new Renderlet.RequestProperties(typeRenderletRequestProperties.getUriInfo(),
						typeRenderletRequestProperties.getRequestHeaders(),
						typeRenderletRequestProperties.getResponseHeaders(), bundleContext);
				renderlet.render(node, context, sharedRenderingValues,
						callbackRenderer, renderingSpecificationUri, 
						typeRenderletRequestProperties.getMode(),
						typeRenderletRequestProperties.getMediaType(),
						renderletRequestProperties, os);
			}

			@Override
			public String toString() {
				return "RenderletManager managed for: "+renderletServiceName+" with "+renderingSpecificationUri;
			}


		};

		ServiceRegistration oldServiceRegistration =
				getAlreadyRegisteredServiceReg(rdfType, mediaType, modePattern);
		if(oldServiceRegistration != null) {
			oldServiceRegistration.unregister();
			registeredTypeRenderlets.remove(oldServiceRegistration);
		}

		ServiceRegistration registration = bundleContext.registerService(TypeRenderlet.class.getName(), typeRenderlet,
				new Hashtable());
		registeredTypeRenderlets.add(registration);
		return true;
	}

	private ServiceRegistration getAlreadyRegisteredServiceReg(UriRef rdfType, 
			MediaType mediaType, String modePattern) {
		
		for (ServiceRegistration serviceRegistration : registeredTypeRenderlets) {
			TypeRenderlet registeredRenderlet = (TypeRenderlet)
					bundleContext.getService(serviceRegistration.getReference());
			if (registeredRenderlet != null
					&& registeredRenderlet.getRdfType().equals(rdfType)
					&& registeredRenderlet.getMediaType().equals(mediaType)) {

				if(modePattern == null && registeredRenderlet.getModePattern() == null) {
					return serviceRegistration;
				} else if(modePattern != null && registeredRenderlet.getModePattern() != null) {
					if(registeredRenderlet.getModePattern().equals(modePattern)) {
						return serviceRegistration;
					}
				}
			}
		}
		return null;
	}
}
