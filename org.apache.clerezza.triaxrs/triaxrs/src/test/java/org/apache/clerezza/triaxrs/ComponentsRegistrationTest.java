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

import java.io.File;
import java.io.InputStream;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentInstance;
import org.apache.clerezza.jaxrs.extensions.prefixmanager.BundlePrefixManager;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.RequestURI;
import org.wymiwyg.wrhapi.Response;

/**
 * @author mir
 * 
 */
public class ComponentsRegistrationTest {

	private static boolean methodInvoked = false;

	@Path("/a")
	public static class MyResource1 {
		@GET
		public void handleGet() {
			methodInvoked = true;
		}
	}

	@Path("/b")
	public static class MyResource2 {
		@GET
		public void handleGet() {
			methodInvoked = true;
		}
	}

	@Path("/c")
	public static class MyResource3 {
		@GET
		public void handleGet() {
			methodInvoked = true;
		}
	}

	@Test
	public void testComponentRegistration() throws Exception {
		JaxRsHandler handler = HandlerCreator.getHandler("", null);
		BundlePrefixManager manager = new BundlePrefixManager() {

			@Override
			public String getPrefix(Bundle bundle) {
				return "/test";
			}

		};

		MyComponentContext context = new MyComponentContext();

		ServiceReference serviceRef1 = createServiceReferenceMock();
		context.addServiceReferenceAndComponent(serviceRef1, new MyResource1());
		handler.bindComponent(serviceRef1);

		Assert.assertFalse(isReachable(handler, "/test/a"));

		handler.bindBundlePrefixManager(manager);
		handler.activate(context);

		Assert.assertTrue(isReachable(handler, "/test/a"));

		handler.unbindBundlePrefixManager(manager);

		ServiceReference serviceRef2 = createServiceReferenceMock();
		context.addServiceReferenceAndComponent(serviceRef2, new MyResource2());
		handler.bindComponent(serviceRef2);

		Assert.assertFalse(isReachable(handler, "/test/b"));

		handler.bindBundlePrefixManager(manager);

		Assert.assertTrue(isReachable(handler, "/test/b"));

		ServiceReference serviceRef3 = createServiceReferenceMock();
		context.addServiceReferenceAndComponent(serviceRef3, new MyResource3());

		handler.bindComponent(serviceRef3);
		Assert.assertTrue(isReachable(handler, "/test/c"));
		
		handler.unbindComponent(serviceRef1);
		handler.unbindComponent(serviceRef2);
		handler.unbindComponent(serviceRef3);
	}

	private boolean isReachable(JaxRsHandler handler, String path) {
		try {
			Request requestMock = EasyMock.createNiceMock(Request.class);
			Response responseMock = EasyMock.createNiceMock(Response.class);
			expect(requestMock.getMethod()).andReturn(Method.GET).anyTimes();
			RequestURI requestURI = EasyMock.createNiceMock(RequestURI.class);
			expect(requestURI.getPath()).andReturn(path);
			expect(requestMock.getRequestURI()).andReturn(requestURI).anyTimes();
			replay(requestMock);
			replay(requestURI);
			replay(responseMock);
			handler.handle(requestMock, responseMock);
			return methodInvoked;
		} catch (HandlerException he) {
			he.printStackTrace();
			return false;
		} finally {
			methodInvoked = false;
		}
	}
	
	private static ServiceReference createServiceReferenceMock() {
		ServiceReference referenceMock = EasyMock
				.createNiceMock(ServiceReference.class);
		EasyMock.makeThreadSafe(referenceMock, true);
		Bundle bundleMock = EasyMock.createNiceMock(Bundle.class);
		expect(referenceMock.getBundle()).andReturn(bundleMock).anyTimes();
		replay(referenceMock);
		replay(bundleMock);

		return referenceMock;
	}


	
	public static class MyComponentContext implements ComponentContext {

		Map<ServiceReference, Object> reference2ComponentMap = new HashMap<ServiceReference, Object>();

		@Override
		public void disableComponent(String name) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public void enableComponent(String name) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public BundleContext getBundleContext() {
			return new BundleContext() {

				@Override
				public String getProperty(String string) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public Bundle getBundle() {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public Bundle installBundle(String string) throws BundleException {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public Bundle installBundle(String string, InputStream in) throws BundleException {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public Bundle getBundle(long l) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public Bundle[] getBundles() {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public void addServiceListener(ServiceListener sl, String string) throws InvalidSyntaxException {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public void addServiceListener(ServiceListener sl) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public void removeServiceListener(ServiceListener sl) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public void addBundleListener(BundleListener bl) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public void removeBundleListener(BundleListener bl) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public void addFrameworkListener(FrameworkListener fl) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public void removeFrameworkListener(FrameworkListener fl) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public ServiceRegistration registerService(String[] strings, Object o, Dictionary dctnr) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public ServiceRegistration registerService(String string, Object o, Dictionary dctnr) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public ServiceReference[] getServiceReferences(String string, String string1) throws InvalidSyntaxException {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public ServiceReference[] getAllServiceReferences(String string, String string1) throws InvalidSyntaxException {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public ServiceReference getServiceReference(String string) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public Object getService(ServiceReference sr) {
					return reference2ComponentMap.get(sr);
				}

				@Override
				public boolean ungetService(ServiceReference sr) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public File getDataFile(String string) {
					throw new UnsupportedOperationException("Not supported yet.");
				}

				@Override
				public Filter createFilter(String string) throws InvalidSyntaxException {
					throw new UnsupportedOperationException("Not supported yet.");
				}
			};
		}

		@Override
		public ComponentInstance getComponentInstance() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Dictionary getProperties() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public ServiceReference getServiceReference() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Bundle getUsingBundle() {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Object locateService(String name) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public Object locateService(String name, ServiceReference reference) {
			return reference2ComponentMap.get(reference);
		}

		@Override
		public Object[] locateServices(String name) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void addServiceReferenceAndComponent(ServiceReference ref,
				Object component) {
			reference2ComponentMap.put(ref, component);
		}

	}
}
