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
package org.apache.clerezza.web.resources.style;

import java.net.URL;


import javax.ws.rs.core.MediaType;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.osgi.service.component.ComponentContext;

/**
 * Bundlized version oficons. Bundles which uses these icons
 * should use this service (bundle depends on org.apache.clerezza.web.resources.style.Style)
 *
 * @author tio
 */
@Component(immediate=true)
public class Style {

	@Reference
	private RenderletManager renderletManager;

	/**
	 * configuration.
	 *
	 * @param context
	 */
	protected void activate(ComponentContext context) {
		URL templateURL = getClass().getResource("globalmenu-naked.ssp");

		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(templateURL.toString()), RDFS.Resource,
				"menu", MediaType.APPLICATION_XHTML_XML_TYPE, true);

		templateURL = getClass().getResource("headed-page-template.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(templateURL.toString()), PLATFORM.HeadedPage, "(?!.*naked).*",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);

	}

}
