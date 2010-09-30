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
package org.apache.clerezza.triaxrs;

import org.apache.clerezza.jaxrs.extensions.MethodResponse;
import org.apache.clerezza.jaxrs.extensions.ResourceMethodException;
import org.apache.clerezza.jaxrs.extensions.RootResourceExecutor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.jaxrs.extensions.prefixmanager.BundlePrefixManager;
import org.apache.clerezza.triaxrs.providers.AggregatedProviders;
import org.apache.clerezza.triaxrs.providers.CascadingProviders;
import org.apache.clerezza.triaxrs.providers.DefaultProviders;
import org.apache.clerezza.triaxrs.providers.ProvidersImpl;
import org.apache.clerezza.triaxrs.util.MethodUtil;
import org.apache.clerezza.triaxrs.util.PathMatching;
import org.wymiwyg.wrhapi.Handler;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI.Type;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.ResponseStatus;
import org.wymiwyg.wrhapi.util.MessageBody2Read;

/**
 * 
 * Am implementation of JAX-RS (aka JSR 311) based on wrhapi.
 * 
 * It supports both injection of <code>javax.ws.rs.core.Application</code>
 * instances, as well as direct injection of <code>Provider</code>s and root
 * resources. For direct injection a component just need to expose a service of 
 * type <code>java.lang.Object</code> and have the property "javax.ws.rs" set to
 * true.
 * 
 * @author reto
 * 
 * @scr.component name="org.apache.clerezza.triaxrs.JaxRsHandler"
 * @scr.service interface="org.wymiwyg.wrhapi.Handler"
 * @scr.reference name="component" cardinality="0..n" policy="dynamic"
 *                interface="java.lang.Object" target="(javax.ws.rs=true)"
 * @scr.reference name="bundlePrefixManager" cardinality="0..1" policy="dynamic"
 *                interface="org.apache.clerezza.jaxrs.extensions.prefixmanager.BundlePrefixManager"
 */
public class JaxRsHandler implements Handler {
	
	private static Logger logger = LoggerFactory.getLogger(JaxRsHandler.class);

	/**
	 * applicationConfigs are injected by the osgi framework. At the moment, we
	 * can handle one application per handler.
	 * 
	 * @scr.reference cardinality="0..1"
	 */
	Application applicationConfig;
	
	/**
	 * @scr.reference
	 */
	private RootResourceExecutor resourceExecutor;

	/**
	 * stores all <code>ServiceReference</code>s of <code>Application</code>s
	 * that were bound before the JaxRsHandler was activated.
	 */
	private Set<ServiceReference> applicationReferenceStore = new HashSet<ServiceReference>();

	/**
	 * stores all <code>ServiceReference</code>s of component objects (where
	 * javax.ws.rs=true) that were bound before the JaxRsHandler was activated.
	 */
	Set<ServiceReference> componentReferenceStore = new HashSet<ServiceReference>();

	/**
	 * manages the set of root-resources
	 */
	private RootResources rootResources;

	/**
	 * the resource descriptor provided by the application, so they can be
	 * removed when the application is removed
	 */
	Collection<RootResourceDescriptor> applicationProvidedDescriptors = 
			new ArrayList<RootResourceDescriptor>();

	/**
	 * Lock used when changing the configuration.
	 */
	private static ReentrantReadWriteLock configLock = new ReentrantReadWriteLock();

	/**
	 * Provides access to the providers, this aggregation of (in order): -
	 * applicationProviders (if available) - componentSpecifiedProviders -
	 * built-in providers
	 */
	public static final AggregatedProviders providers = new AggregatedProviders(configLock);

	/**
	 * Providers injected with OSGi DS
	 */
	private CascadingProviders componentSpecifiedProviders = new CascadingProviders();

	/**
	 * ComponentContext injected in the activate()-Method by OSGi DS
	 */
	private ComponentContext componentContext;

	/**
	 * Keeps the mappings form bundles symbolic names to a bundle prefixes
	 */
	private BundlePrefixManager prefixManager;

	/**
	 * Keeps the mappings from components (providers) to their path prefixes.
	 * This is necessary because during unbindComponent we might not have
	 * access to prefixManager anymore (since it might have been unbound).
	 */
	private Map<Object, String> component2PathPrefixMap = 
			new HashMap<Object, String>();

	/**
	 * Keeps the WebRequest for a thread.
	 */
	public static ThreadLocal<WebRequest> localRequest = new ThreadLocal<WebRequest>();

	private Set<HttpMethod> httpMethods = new HashSet<HttpMethod>();

	public JaxRsHandler() {
		// initialize default providers
		Class<?>[] defaultProviders = DefaultProviders.getDefaultProviders();
		Providers builtInProviders = new ProvidersImpl(defaultProviders);
		providers.reset(componentSpecifiedProviders,
				builtInProviders);
		rootResources = new RootResources();
		resourceExecutor = new RootResourceExecutorImpl();
	}
	
	protected void bindBundlePrefixManager(BundlePrefixManager prefixManager) {
		logger.debug("Binding bundle prefix manager");
		configLock.writeLock().lock();
		try {
			this.prefixManager = prefixManager;
			registerFromStores();
		} finally {
			configLock.writeLock().unlock();
		}
	}
	
	protected void unbindBundlePrefixManager(BundlePrefixManager prefixManager) {
		logger.debug("Unbinding bundle prefix manager");
		configLock.writeLock().lock();
		try {
			this.prefixManager = null;
		} finally {
			configLock.writeLock().unlock();
		}
	}

	/**
	 * Binds the specified JAX-RS root-resource or provider.
	 * 
	 * @param component
	 *            The new JAX-RS component to bind.
	 */
	protected void bindComponent(ServiceReference serviceReference) {
		logger.debug("Bind component of bundle {}", serviceReference
				.getBundle().getSymbolicName());
		configLock.writeLock().lock();
		try {
			if (componentContext != null && prefixManager != null) {
				registerComponent(serviceReference);
			} else {
				componentReferenceStore.add(serviceReference);
			}
		} finally {
			configLock.writeLock().unlock();
		}
	}

	private void registerComponent(ServiceReference serviceReference) {
		String bundlePathPrefix = prefixManager.getPrefix(serviceReference
				.getBundle());

		Object component = componentContext.getBundleContext().getService(serviceReference);
		if (component == null) {
			return;
		}
		registerComponent(component, bundlePathPrefix);
		component2PathPrefixMap.put(component, bundlePathPrefix);
	}

	protected void registerComponent(Object component, String pathPrefix) {      
		final Class<?> clazz = component.getClass();
		final Path path = clazz.getAnnotation(Path.class);
		if (path == null) {
			// Warn about and ignore classes that do not conform to the
			// requirements of root resource or provider classes.

			if (clazz.getAnnotation(Provider.class) != null) {
				logger.info("Register provider {} to path {}", component.getClass().getName(), pathPrefix);
				componentSpecifiedProviders.addInstance(component, pathPrefix);
			} else {
				logger.warn("Ignoring component: {} (Not a root resource or provider)",
						component);
			}
		} else {
			logger.info("Register resource {} to path {}{}", 
					new Object[]{component.getClass().getName(), pathPrefix, path.value()});
			collectHttpMethods(component.getClass());
			final RootResourceDescriptor descriptor = new RootResourceDescriptor(
					clazz, component, pathPrefix + path.value(), providers);
			rootResources.add(descriptor);
			// FIXME An unbind will potentially remove the wrong
			// descriptor from the set. Solution: Use a list or a
			// lookup table.
		}
	}

	private void collectHttpMethods(Class<?> clazz) {
		Set<java.lang.reflect.Method> annotatedMethods = MethodUtil.getAnnotatedMethods(clazz);
		for (java.lang.reflect.Method method : annotatedMethods) {
			Annotation[] annotations = method.getAnnotations();
			for (Annotation annotation : annotations) {
				HttpMethod httpMethod = annotation.annotationType().getAnnotation(HttpMethod.class);
				if (httpMethod != null) {
					httpMethods.add(httpMethod);
				}
			}
		}
	}

	/**
	 * Unbinds the specified JAXRS component referred by the parameter.
	 * 
	 */
	protected void unbindComponent(ServiceReference serviceReference) {
		configLock.writeLock().lock();
		try {
			if (!componentReferenceStore.remove(serviceReference)) {
				Object component = componentContext.getBundleContext().getService(serviceReference);
				if (component == null) {
					logger.warn("Failed to unregister {} as no service could be located", serviceReference);
					return;
				}
				unregisterComponent(component, component2PathPrefixMap.get(component));
				component2PathPrefixMap.remove(component);
			}
		} finally {
			configLock.writeLock().unlock();
		}
	}

	protected void unregisterComponent(Object component, String pathPrefix) {
		final Class<?> clazz = component.getClass();
		final Path path = clazz.getAnnotation(Path.class);
		logger.info("Unbinding: {}", component);
		if (path != null) {
			final RootResourceDescriptor descriptor = new RootResourceDescriptor(
					clazz, component, path.value(), providers);
			rootResources.remove(descriptor);
			// FIXME An unbind will potentially remove the wrong
			// descriptor from the set. Solution: Use a list or a
			// lookup table.
			// why? the component is unique and two root-resource
			// descriptors aren't equals without the same component
		} else if (clazz.getAnnotation(Provider.class) != null) {
			componentSpecifiedProviders.removeInstance(component, pathPrefix);
		}
	}

	/**
	 * this is called when a new application config arrives
	 * 
	 * @param applicationConfig
	 */
	protected void bindApplicationConfig(ServiceReference serviceReference) {
		configLock.writeLock().lock();
		try {
			if (componentContext != null && prefixManager != null) {
				registerApplicationConfig(serviceReference);
			} else {
				applicationReferenceStore.add(serviceReference);
			}
		} finally {
			configLock.writeLock().unlock();
		}
	}

	private void registerApplicationConfig(ServiceReference serviceReference) {
		String bundlePathPrefix = prefixManager.getPrefix(serviceReference
				.getBundle());

        Application applicationConfigToRegister = (Application) componentContext
				.locateService("applicationConfig", serviceReference);
		if(applicationConfigToRegister == null) {
			return;
		}

		registerApplicationConfig(applicationConfigToRegister, bundlePathPrefix);
	}

	protected void registerApplicationConfig(Application applicationConfig,
			String pathPrefix) {
		logger.info("Binding application config: {} ({})", applicationConfig, 
				applicationConfig.getClass());

		this.applicationConfig = applicationConfig;
		Providers[] delegates = providers.getDelegates();
		if (delegates.length != 2) {
			throw new RuntimeException(
					"expecting service discovered and buil-in providers");
		}
		CascadingProviders applicationProviders = new CascadingProviders();
		providers.reset(applicationProviders, delegates[0], delegates[1]);
		Set<Class<?>> appProvidedClasses = applicationConfig.getClasses();
		// TODO add application provider in a way that they are removable
		// and that

		for (Class<?> clazz : appProvidedClasses) {
			registerSingleComponentOfApplication(clazz, null,
					applicationProviders, pathPrefix);
		}
		Set<Object> singletons = applicationConfig.getSingletons();
		for (Object singleton : singletons) {
			Class<?> clazz = singleton.getClass();
			registerSingleComponentOfApplication(clazz, singleton,
					applicationProviders, pathPrefix);
		}

		logger.debug("Binding application config finished.");
	}

	protected void registerSingleComponentOfApplication(Class<?> clazz,
			Object instance, CascadingProviders applicationProviders,
			String pathPrefix) {
		Path path = clazz.getAnnotation(Path.class);
		if (path != null) {
			RootResourceDescriptor rootResourceDescriptor;
			collectHttpMethods(clazz);
			String completePath = pathPrefix + path.value();
			if (instance == null) {
				rootResourceDescriptor = new RootResourceDescriptor(clazz,
						completePath);
			} else {
				rootResourceDescriptor = new RootResourceDescriptor(clazz,
						instance, completePath, providers);
			}
			rootResources.add(rootResourceDescriptor);
			applicationProvidedDescriptors.add(rootResourceDescriptor);

		} else {
			if (clazz.getAnnotation(Provider.class) != null) {
				if (instance != null) {
					applicationProviders.addInstance(instance, pathPrefix);
				} else {
					applicationProviders.addClass(clazz, pathPrefix);
				}
			} else {
				logger.warn("Ignoring application component: {}" +
						" (Not a root resource or provider)", clazz);
			}
		}
	}

	protected void unbindApplicationConfig(Application applicationConfig) {
		configLock.writeLock().lock();
		try {
			this.unregisterApplicationConfig(applicationConfig);
		} finally {
			configLock.writeLock().unlock();
		}
	}

	protected void unregisterApplicationConfig(Application applicationConfig) {
		logger.info("unbinding app config.");
		this.applicationConfig = null;
		Providers[] delegates = providers.getDelegates();
		if (delegates.length != 3) {
			throw new RuntimeException("expecting application provided,"
					+ " service discovered and buil-in providers");
		}
		providers.reset(delegates[1], delegates[2]);
		for (RootResourceDescriptor rootResourceDescriptor : applicationProvidedDescriptors) {
			rootResources.remove(rootResourceDescriptor);
		}
		applicationProvidedDescriptors.clear();
	}

	/**
	 * The activate method is called when SCR activates the component
	 * configuration
	 * 
	 * @param componentContext
	 */
	protected void activate(ComponentContext componentContext) {
		configLock.writeLock().lock();
		try {
			this.componentContext = componentContext;
			registerFromStores();
		} finally {
			configLock.writeLock().unlock();
		}
	}

	private void registerFromStores() {
		if (componentContext != null && prefixManager != null) {	
			
			for (ServiceReference appRef : applicationReferenceStore) {
				this.registerApplicationConfig(appRef);
			}
			applicationReferenceStore.clear();
	
			for (ServiceReference compRef : componentReferenceStore) {
				this.registerComponent(compRef);
			}
			componentReferenceStore.clear();
		}
	}

	@Override
	public void handle(Request origRequest, Response response)
			throws HandlerException {


		WebRequest request = new WebRequestImpl(origRequest, providers);
		localRequest.set(request);

		try {
			MethodResponse methodResponse;
			Type type = request.getWrhapiRequest().getRequestURI().getType();
			Method method = request.getWrhapiRequest().getMethod();
			if (Type.NO_RESOURCE.equals(type) && Method.OPTIONS.equals(method)) {
				ResponseBuilder builder = javax.ws.rs.core.Response.ok();
				StringWriter sw = new StringWriter();
				Iterator<HttpMethod> iter = httpMethods.iterator();
				if (iter.hasNext()) {
					for (int i = 0; i < httpMethods.size() - 1; i++) {
						HttpMethod httpMethod = iter.next();
						sw.append(httpMethod.value());
						sw.append(",");
					}
					sw.append(iter.next().value());
				}
				builder.header(HeaderName.ALLOW.toString(), sw.toString());
				methodResponse = getWildCardOptionsResponse();

			} else {
				RootResources.ResourceAndPathMatching resourceAndPathMatching;
				configLock.readLock().lock();
				try {
					resourceAndPathMatching = rootResources.getResourceAndPathMatching(request);
				} finally {
					configLock.readLock().unlock();
				}
				final PathMatching pathMatching = resourceAndPathMatching.getPathMatching();

				methodResponse = resourceExecutor.execute(
						request, resourceAndPathMatching.getRootResource(),
						pathMatching.getRemainingURIPath(), pathMatching.getParameters());
			}
			ProcessableResponse processableResponse;
			try {
				processableResponse = (ProcessableResponse) methodResponse;
			} catch (ClassCastException e) {
				throw new RuntimeException("processing of other MethodResponse" +
						" implementations not yet supported");
			}
			ResponseProcessor.handleReturnValue(request, response, processableResponse);
			
		} catch (ResourceMethodException ex) {
			Throwable cause = ex.getCause();
			if (cause == null) { // This should not happen
				logger.error("Exception {}", ex);
			} else {
				handleException(cause, request, response);
			}
		} catch (NoMatchingRootResourceException e) {
			response.setResponseStatus(ResponseStatus.NOT_FOUND);
			response.setBody(new MessageBody2Read() {

				@Override
				public ReadableByteChannel read() throws IOException {
					return Channels
							.newChannel(new ByteArrayInputStream(
									("JaxRsHandler - " + rootResources.size() + " root resource descriptor registered")
											.getBytes()));
				}
			});
		} catch (Exception ex) {
			handleException(ex, request, response);
		}
	}

	static void handleException(Throwable exception, WebRequest request, Response response) throws HandlerException, RuntimeException {
		if (exception instanceof WebApplicationException) {
			WebApplicationException webEx = (WebApplicationException) exception;
			logger.debug("Exception {}", webEx);
			javax.ws.rs.core.Response jaxResponse = webEx.getResponse();
			if ((jaxResponse == null) || (jaxResponse.getEntity() == null)) {
				ExceptionMapper<WebApplicationException> exMapper = (ExceptionMapper<WebApplicationException>) providers
						.getExceptionMapper(webEx.getClass());
				if (exMapper != null) {
					jaxResponse = exMapper.toResponse(webEx);
				}
			}
			try {
				ResponseProcessor.processJaxResponse(request, response, jaxResponse,
						null, Collections.singleton(MediaType.valueOf("text/html")));
				return;
			} catch (IOException ex1) {
				logger.info("Exception processing response from WebApplicationException", ex1);
			}
		}
		ExceptionMapper exMapper = providers.getExceptionMapper(exception.getClass());
		if (exMapper != null) {
			logger.debug("Exception with exception mapper", exception);
			javax.ws.rs.core.Response jaxResponse;
			try {
				jaxResponse = exMapper.toResponse(exception);
			} catch (Exception ex1) {
				/*
				 * Return a server error (status code 500) response to
				 * the client
				 */
				jaxResponse = javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).build();
			}
			try {
				ResponseProcessor.processJaxResponse(request, response, jaxResponse, null, Collections.singleton(MediaType.valueOf("text/html")));
			} catch (IOException ex1) {
				logger.info("Exception processing response from exception mapper", ex1);
			}
		} else {
			/*
			 * Unchecked exceptions and errors MUST be re-thrown and
			 * allowed to propagate to the underlying container
			 */
			if (exception instanceof RuntimeException) {
				if (!(exception instanceof AccessControlException)) {
					logger.warn("RuntimeException (with no exception mapper)", exception);
				} else {
					logger.debug("AccessControlException (will rethrow)", exception);
				}
				throw (RuntimeException) exception;
			}
			logger.warn("Exception (with no exception mapper)", exception);
			throw new HandlerException(exception);
		}
	}

	private MethodResponse getWildCardOptionsResponse() {
		ResponseBuilder builder = javax.ws.rs.core.Response.ok();
				StringWriter sw = new StringWriter();
				Iterator<HttpMethod> iter = httpMethods.iterator();
				if (iter.hasNext()) {
					for (int i = 0; i < httpMethods.size() - 1; i++) {
						HttpMethod httpMethod = iter.next();
						sw.append(httpMethod.value());
						sw.append(",");
					}
					sw.append(iter.next().value());
				}
				builder.header(HeaderName.ALLOW.toString(), sw.toString());
				return  ProcessableResponse.createProcessableResponse(
						builder.build(), null, null, null, null);
	}
}
