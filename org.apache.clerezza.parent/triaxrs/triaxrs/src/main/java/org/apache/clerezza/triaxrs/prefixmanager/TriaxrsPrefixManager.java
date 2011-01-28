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
package org.apache.clerezza.triaxrs.prefixmanager;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.osgi.framework.Bundle;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.jaxrs.extensions.prefixmanager.BundlePrefixManager;
import org.apache.clerezza.triaxrs.Constants;

/**
 * This class provides a function to get the appropriate path prefix for a given
 * bundle.
 * 
 * @author mir
 * 
 * @scr.component name="org.apache.clerezza.triaxrs.prefixmanager.TriaxrsPrefixManager" immediate="true"
 * @scr.service interface="org.apache.clerezza.jaxrs.extensions.prefixmanager.BundlePrefixManager"
 * @scr.reference name="configurationAdmin" cardinality="0..1" policy="dynamic"
 *                interface="org.osgi.service.cm.ConfigurationAdmin"
 * 
 */
public class TriaxrsPrefixManager implements BundlePrefixManager {

	/**
	 * Service property header, which contains the mappings between bundle
	 * symbolic names and bundle prefixes.
	 * 
	 * @scr.property name="mappings" values.name="key" values.value="foo"
	 *               values.name="path" values.value="bar"
	 * description="Contains the mappings between bundle symbolic names and custom prefixes"
	 */
	public static final String TRIAXRS_MAPPINGS = "mappings";
	
	/**
	 * Service property header which specifies, if the default path prefix of
	 * bundles is used. If the header is not set, the value "true" is assumed.
	 * 
	 * @scr.property type="Boolean" value="true"
	 * description="Specifies if the default path prefix of bundles is used."
	 */
	public static final String TRIAXRS_USE_DEFAULT_PREFIXES = "UseDefaultPrefix";

	/**
	 * Service property header which specifies if the custom path prefix of
	 * bundles is used. If the header is not set, the value "true" is assumed.
	 * 
	 * @scr.property type="Boolean" value="true"
	 * description="Specifies if the custom path prefix of bundles is used."
	 */
	public static final String TRIAXRS_USE_CUSTOM_PREFIXES = "UseCustomPrefix";

	private Logger logger = LoggerFactory.getLogger(TriaxrsPrefixManager.class);

	private ConfigurationAdmin configAdmin;

	private ComponentContext componentContext;

	/**
	 * Return the appropriate path prefix for the given bundle
	 * 
	 * @param bundle
	 * @return
	 */
	@Override
	public String getPrefix(Bundle bundle) {
		String prefix = "";

		String defaultPrefix = getDefaultPrefix(bundle);
		this.addDefaultMapping(bundle.getSymbolicName(), defaultPrefix);
		String customPrefix = getCustomPrefix(bundle);

		if (useDefaultPrefixes()) {
			prefix = defaultPrefix;
		}

		if (useCustomPrefixes()) {
			if (customPrefix != null) {
				prefix = customPrefix;
			} else {
				prefix = defaultPrefix;
			}
		}

		prefix = prefix.trim();
		
		if(prefix.endsWith("/")) {
            prefix = prefix.
                    substring(0, prefix.length() - 1);
        }	
		return prefix;
	}

	private Dictionary getProperties() {
		return componentContext.getProperties();
	}

	private Dictionary getMappings() {
		return parseMappings((String[]) getProperties().get(TRIAXRS_MAPPINGS));
	}

	/**
	 * Adds the mapping to the configurations properties if there is no mapping
	 * already
	 * 
	 * @param symbolicName
	 * @param prefix
	 */
	private void addDefaultMapping(String symbolicName, String prefix) {
		if (this.configAdmin != null) {

			try {
				Configuration configuration = configAdmin
						.getConfiguration(TriaxrsPrefixManager.class.getName());
				Dictionary properties = configuration.getProperties();
				if (properties == null) {
					properties = new Hashtable();
				}
				Dictionary mappings = parseMappings((String[]) properties
						.get(TRIAXRS_MAPPINGS));
				if (mappings.get(symbolicName) != null) {
					return;
				}
				mappings.put(symbolicName, prefix);
				String[] newMappings = unparseMappings(mappings);
				properties.put(TRIAXRS_MAPPINGS, newMappings);
				configuration.update(properties);
			} catch (IOException e) {
				logger.warn("Unable to update configuration: {}", e.toString());
			}
		} else {
			logger.warn("Cannot add prefix mapping. Configuration Admin is missing");
		}
	}

	private String getDefaultPrefix(Bundle bundle) {
		Dictionary<String, String> headers = bundle.getHeaders();
		String defaultPrefix = headers.get(Constants.TRIAXRS_PATH_PREFIX);
		if (defaultPrefix == null) {
			defaultPrefix = "";
		}
		return defaultPrefix;
	}

	private String getCustomPrefix(Bundle bundle) {
		return (String) this.getMappings().get(bundle.getSymbolicName());
	}

	private boolean useCustomPrefixes() {
		Boolean boolUseCustom = ((Boolean) this.getProperties().get(
				TRIAXRS_USE_CUSTOM_PREFIXES));
		return boolUseCustom == null || boolUseCustom;
	}

	private boolean useDefaultPrefixes() {
		Boolean boolUseDefault = ((Boolean) this.getProperties().get(
				TRIAXRS_USE_DEFAULT_PREFIXES));
		return boolUseDefault == null || boolUseDefault;
	}

	public static Dictionary<String, String> parseMappings(String[] mappings) {
		Dictionary<String, String> result = new Hashtable<String, String>();
		if (mappings == null) {
			return result;
		}
		for (String mapping : mappings) {
			String[] parts = mapping.split("=");
			if (parts.length == 2) {
				result.put(parts[0], parts[1]);
			} else if (parts.length == 1) {
				result.put(parts[0], "");
			}
		}
		return result;
	}

	public static String[] unparseMappings(Dictionary mappings) {
		Enumeration keys = mappings.keys();
		String[] result = new String[mappings.size()];
		int index = 0;

		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			result[index] = key + "="
					+ formatPrefix((String) mappings.get(key));
			index++;
		}
		return result;
	}

	private static String formatPrefix(String prefix) {
		prefix = prefix.trim();
		if (!prefix.startsWith("/")) {
			prefix = "/" + prefix;
		}

		if (!prefix.endsWith("/")) {
			prefix += "/";
		}
		return prefix;
	}

	protected void bindConfigurationAdmin(ConfigurationAdmin configAdmin) {
		logger.debug("Binding configuration admin");
		this.configAdmin = configAdmin;
	}

	protected void unbindConfigurationAdmin(ConfigurationAdmin configAdmin) {
		logger.debug("Unbinding configuration admin");
	}

	/**
	 * The activate method is called when SCR activates the component
	 * configuration
	 * 
	 * @param componentContext
	 */
	protected void activate(ComponentContext componentContext) {
		this.componentContext = componentContext;
	}
}
