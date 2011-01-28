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
package org.apache.clerezza.platform.scripting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.script.ScriptEngineFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @scr.component
 *
 * @author hasan
 */
public class ScriptEngineFactoryManager implements BundleListener {

	final Logger logger = LoggerFactory.getLogger(getClass());

	private ComponentContext componentContext = null;

	private Map<Long, List<ServiceRegistration>> registeredFactories =
			new HashMap<Long, List<ServiceRegistration>>();

	private final String SERVICE_DEF_PATH = "/META-INF/services/";
	private final String SERVICE_DEF_FILE = "javax.script.ScriptEngineFactory";

	protected void activate(final ComponentContext componentContext) {
		this.componentContext = componentContext;
		registerExistingEngineFactories();
		componentContext.getBundleContext().addBundleListener(this);
	}

	private void registerExistingEngineFactories() {
		if (componentContext == null) {
			logger.warn("componentContext is null while trying to register" +
					"script engine factories in started bundles");
			return;
		}
		Bundle[] bundles = componentContext.getBundleContext().getBundles();
		for (Bundle bundle : bundles) {
			if (bundle.getState() == Bundle.ACTIVE) {
				List<String> engineFactoryNames = getEngineFactoryNames(bundle);
				registerEngineFactories(bundle, engineFactoryNames);
			}
		}
	}

	private List<String> getEngineFactoryNames(Bundle bundle) {
		List<String> engineFactoryNames = new ArrayList<String>();
		BufferedReader in = null;
		try {
			URL entry = bundle.getEntry(SERVICE_DEF_PATH+SERVICE_DEF_FILE);
			if (entry == null) {
				return null;
			}
			in = new BufferedReader(new InputStreamReader(entry.openStream()));
			String engineFactoryName;
			while ((engineFactoryName = in.readLine()) != null) {
				engineFactoryName = engineFactoryName.trim();
				if (!engineFactoryName.isEmpty()) {
					engineFactoryNames.add(engineFactoryName);
				}
			}
			in.close();
		} catch (IOException ex) {
			// ignored, since this bundle seems to not contain a script engine
			logger.info("Cannot read {} in bundle {}",
					SERVICE_DEF_PATH + SERVICE_DEF_FILE, bundle.getSymbolicName());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					// ignored
				}
			}
		}
		return engineFactoryNames;
	}

	private void registerEngineFactories(Bundle bundle, List<String> engineFactoryNames) {
		if (engineFactoryNames == null || engineFactoryNames.isEmpty()) {
			return;
		}
		List<ServiceRegistration> serviceRegList = new ArrayList<ServiceRegistration>();
		for (String engineFactoryName : engineFactoryNames) {
			try {
				Class factoryClass = Class.forName(engineFactoryName);
				ScriptEngineFactory factory = (ScriptEngineFactory) factoryClass.newInstance();
				String[] clazzes = {engineFactoryName, SERVICE_DEF_FILE};
				ServiceRegistration serviceReg = componentContext.getBundleContext().registerService(clazzes, factory, null);
				serviceRegList.add(serviceReg);

				if (engineFactoryName.equals("com.sun.script.jruby.JRubyScriptEngineFactory")) {
					factory = new JRubyScriptEngineFactoryWrapper(
							(ScriptEngineFactory) factoryClass.newInstance());
					String[] wrappedClazzes = {
						"org.apache.clerezza.platform.scripting.JRubyScriptEngineFactoryWrapper",
						SERVICE_DEF_FILE
					};
					serviceRegList.add(componentContext.getBundleContext().registerService(wrappedClazzes, factory, null));
				}

			} catch (InstantiationException ex) {
				logger.warn("Cannot instantiate {}", engineFactoryName);
				throw new RuntimeException(ex);
			} catch (IllegalAccessException ex) {
				throw new RuntimeException(ex);
			} catch (ClassNotFoundException ex) {
				logger.warn("Cannot find class {}", engineFactoryName);
				throw new RuntimeException(ex);
			}
		}
		long bundleId = bundle.getBundleId();
		registeredFactories.put(bundleId, serviceRegList);
	}

	protected void deactivate(final ComponentContext componentContext) {
		deregisterEngineFactories();
		componentContext.getBundleContext().removeBundleListener(this);
		this.componentContext = null;
	}

	private void deregisterEngineFactories() {
		Iterator<List<ServiceRegistration>> serviceRegLists =
				registeredFactories.values().iterator();
		while (serviceRegLists.hasNext()) {
			for (ServiceRegistration serviceReg : serviceRegLists.next()) {
				serviceReg.unregister();
			}
		}
		registeredFactories.clear();
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		Bundle bundle = event.getBundle();
		switch (event.getType()) {
			case BundleEvent.STARTED:
				registerEngineFactories(bundle, getEngineFactoryNames(bundle));
				break;
			case BundleEvent.STOPPED:
				long bundleId = bundle.getBundleId();
				List<ServiceRegistration> serviceRegList = registeredFactories.get(bundleId);
				if (serviceRegList != null && !serviceRegList.isEmpty()) {
					for (ServiceRegistration serviceReg : serviceRegList) {
						serviceReg.unregister();
					}
					registeredFactories.remove(bundleId);
				}
				break;
		}
	}
}
