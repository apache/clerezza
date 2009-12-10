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
package org.apache.clerezza.triaxrs.blackbox;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Dictionary;
import java.util.Hashtable;

import org.easymock.EasyMock;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.jaxrs.extensions.prefixmanager.BundlePrefixManager;
import org.apache.clerezza.triaxrs.Constants;
import org.apache.clerezza.triaxrs.prefixmanager.TriaxrsPrefixManager;

/**
 * 
 * @author mir
 *
 */
public class TestTriaxrsPrefixManager {

	@Test
	public void testGetCustomPrefix() throws Exception {
		Dictionary properties = new Hashtable();
		String[] mappings = new String[1];
		mappings[0] = "org.example.TestBundle=/custom";
		properties.put("mappings", mappings);
		ComponentContext context = createComponentContextMock(properties);
		ConfigurationAdmin configAdmin = createConfigAdminMock(new Hashtable());

		BundlePrefixManager prefixManager = createBundlePrefixManager(
				configAdmin, context);
		
		Bundle bundle = creaBundleMock("org.example.TestBundle", new Hashtable());

		assertEquals("/custom", prefixManager.getPrefix(bundle));
	}
	
	@Test
	public void testGetDefaultPrefix() throws Exception {
		ComponentContext context = createComponentContextMock(new Hashtable());
		ConfigurationAdmin configAdmin = createConfigAdminMock(new Hashtable());

		BundlePrefixManager prefixManager = createBundlePrefixManager(
				configAdmin, context);
		
		Dictionary headers = new Hashtable();
		headers.put(Constants.TRIAXRS_PATH_PREFIX, "/default");
		Bundle bundle = creaBundleMock("org.example.TestBundle", headers);

		assertEquals("/default", prefixManager.getPrefix(bundle));
	}
	
	@Test
	public void testCustomPriorDefault() throws Exception {
		Dictionary properties = new Hashtable();
		String[] mappings = new String[1];
		mappings[0] = "org.example.TestBundle=/custom";
		properties.put("mappings", mappings);
		ComponentContext context = createComponentContextMock(properties);
		ConfigurationAdmin configAdmin = createConfigAdminMock(new Hashtable());

		BundlePrefixManager prefixManager = createBundlePrefixManager(
				configAdmin, context);
		
		Dictionary headers = new Hashtable();
		headers.put(Constants.TRIAXRS_PATH_PREFIX, "/default");
		Bundle bundle = creaBundleMock("org.example.TestBundle", headers);

		assertEquals("/custom", prefixManager.getPrefix(bundle));
	}
	
	@Test
	public void testCustomPrefixDeactivated() throws Exception {
		Dictionary properties = new Hashtable();
		String[] mappings = new String[1];
		mappings[0] = "org.example.TestBundle=/custom";
		properties.put("mappings", mappings);
		properties.put(TriaxrsPrefixManager.TRIAXRS_USE_CUSTOM_PREFIXES, false);
		ComponentContext context = createComponentContextMock(properties);
		ConfigurationAdmin configAdmin = createConfigAdminMock(new Hashtable());

		BundlePrefixManager prefixManager = createBundlePrefixManager(
				configAdmin, context);
		
		Bundle bundle = creaBundleMock("org.example.TestBundle", new Hashtable());

		assertEquals("", prefixManager.getPrefix(bundle));
	}
	
	@Test
	public void testDefaultPrefixDeactivated() throws Exception {
		Dictionary properties = new Hashtable();
		String[] mappings = new String[1];
		mappings[0] = "org.example.TestBundle=/custom";
		properties.put("mappings", mappings);
		properties.put(TriaxrsPrefixManager.TRIAXRS_USE_CUSTOM_PREFIXES, false);
		properties.put(TriaxrsPrefixManager.TRIAXRS_USE_DEFAULT_PREFIXES, false);
		ComponentContext context = createComponentContextMock(properties);
		ConfigurationAdmin configAdmin = createConfigAdminMock(new Hashtable());

		BundlePrefixManager prefixManager = createBundlePrefixManager(
				configAdmin, context);
		
		Dictionary headers = new Hashtable();
		headers.put(Constants.TRIAXRS_PATH_PREFIX, "/default");
		Bundle bundle = creaBundleMock("org.example.TestBundle", headers);

		assertEquals("", prefixManager.getPrefix(bundle));
	}
	
	private static ConfigurationAdmin createConfigAdminMock(
			Dictionary properties) {
		final ConfigurationAdmin configAdminMock = EasyMock
				.createMock(ConfigurationAdmin.class);
		final Configuration configurationMock = EasyMock
				.createMock(Configuration.class);
		try {
			expect(configAdminMock.getConfiguration(isA(String.class)))
					.andReturn(configurationMock).anyTimes();
		} catch (Exception e) {
		}
		expect(configurationMock.getProperties()).andReturn(properties)
				.anyTimes();
		try {
			configurationMock.update(isA(Dictionary.class));
		} catch (Exception e) {}
		
		replay(configAdminMock);
		replay(configurationMock);
		return configAdminMock;
	}	

	private static ComponentContext createComponentContextMock(
			Dictionary properties) {
		final ComponentContext contextMock = EasyMock
				.createMock(ComponentContext.class);
		expect(contextMock.getProperties()).andReturn(properties).anyTimes();
		replay(contextMock);
		return contextMock;
	}

	private static BundlePrefixManager createBundlePrefixManager(
			final ConfigurationAdmin configAdmin, final ComponentContext context) {

		TriaxrsPrefixManager prefixManager = new TriaxrsPrefixManager() {
			{
				bindConfigurationAdmin(configAdmin);
				activate(context);

			}
		};
		return prefixManager;
	}

	private static Bundle creaBundleMock(String symbolicName, Dictionary headers) {
		final Bundle bundleMock = EasyMock
				.createMock(Bundle.class);
		expect(bundleMock.getSymbolicName()).andReturn(symbolicName).anyTimes();
		expect(bundleMock.getHeaders()).andReturn(headers).anyTimes();
		replay(bundleMock);
		return bundleMock;
	}

}
