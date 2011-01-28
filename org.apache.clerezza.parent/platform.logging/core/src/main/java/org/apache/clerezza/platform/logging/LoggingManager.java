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
package org.apache.clerezza.platform.logging;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.AccessControlException;
import java.security.AccessController;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.dashboard.GlobalMenuItem;
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider;
import org.apache.clerezza.platform.logging.ontologies.LOGGING;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.osgi.framework.Bundle;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;

@Component
@Services({
	@Service(value = Object.class),
	@Service(value = GlobalMenuItemsProvider.class)
})
@Property(name = "javax.ws.rs", boolValue = true)
@Path("admin/logging")
public class LoggingManager implements GlobalMenuItemsProvider {

	@Reference
	private RenderletManager renderletManager;
	@Reference
	private ConfigurationAdmin configurationAdmin;
	private String paxLoggingLocation = null;

	public void activate(ComponentContext context)
			throws URISyntaxException, IOException {
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource(
				"config-page-naked.ssp").toURI().toString()),
				LOGGING.LoggingConfigPage, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);

		for (Bundle bundle : context.getBundleContext().getBundles()) {
			if (bundle.getSymbolicName().equals("org.ops4j.pax.logging.pax-logging-service")) {
				paxLoggingLocation = bundle.getLocation();
				break;
			}
		}

	}

	private void setProperties(Dictionary props)
			throws IOException {
		Configuration config = getServiceConfig();
		config.update(props);
	}

	/*private void setProperties(Hashtable properties) {
	setProperties
	}*/
	private Configuration getServiceConfig()
			throws IOException {
		Configuration config = configurationAdmin.getConfiguration("org.ops4j.pax.logging", paxLoggingLocation);
		return config;
	}

	@GET
	public GraphNode entry(@Context UriInfo uriInfo)
			throws IOException {
		AccessController.checkPermission(new LoggingManagerAccessPermission());
		TrailingSlash.enforcePresent(uriInfo);
		SimpleMGraph resultMGraph = new SimpleMGraph();
		GraphNode result = new GraphNode(new BNode(), resultMGraph);
		result.addPropertyValue(LOGGING.loggingConfig, getPropertiesAsString());
		result.addProperty(RDF.type, PLATFORM.HeadedPage);
		result.addProperty(RDF.type, LOGGING.LoggingConfigPage);
		return result;
	}

	private String getPropertiesAsString()
			throws IOException {
		Configuration config = getServiceConfig();
		Properties properties = new Properties();
		Dictionary propertyDictionary = config.getProperties();
		Enumeration<String> keys = propertyDictionary.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			properties.put(key, propertyDictionary.get(key));
		}
		StringWriter stringWriter = new StringWriter();
		properties.store(stringWriter, "properties of the pax-logging service");
		return stringWriter.toString();
	}

	@POST
	@Path("setConfiguration")
	public Response setConfiguration(@Context UriInfo uriInfo,
			@FormParam("configuration") String configuration)
			throws IOException {
		AccessController.checkPermission(new LoggingManagerAccessPermission());
		Properties properties = new Properties();
		properties.load(new StringReader(configuration));
		setProperties(properties);
		return RedirectUtil.createSeeOtherResponse("./", uriInfo);
	}

	@Override
	public Set<GlobalMenuItem> getMenuItems() {
		Set<GlobalMenuItem> items = new HashSet<GlobalMenuItem>();
		try {
			AccessController.checkPermission(new LoggingManagerAccessPermission());
		} catch (AccessControlException e) {
			return items;
		}
		items.add(new GlobalMenuItem("/admin/logging", "Logging", "Logging", 3,
				"Administration"));
		return items;

	}
}
