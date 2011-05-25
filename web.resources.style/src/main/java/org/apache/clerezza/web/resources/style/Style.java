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
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesService;
import org.apache.clerezza.rdf.ontologies.HIERARCHY;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.osgi.service.component.ComponentContext;

/**
 * Registers the Scala Sever PAges provided by this bundle.
 *
 * @author tio, reto
 */
@Component(immediate=true)
public class Style {

	@Reference
	private ScalaServerPagesService sspService;

	/**
	 * configuration.
	 *
	 * @param context
	 */
	protected void activate(ComponentContext context) {
		URL templateURL = getClass().getResource("globalmenu-naked.ssp");
		sspService.registerScalaServerPage(templateURL, RDFS.Resource, "menu",
				MediaType.APPLICATION_XHTML_XML_TYPE);

		templateURL = getClass().getResource("rdf-list-template.ssp");
		sspService.registerScalaServerPage(templateURL, RDF.List, ".*naked",
				MediaType.APPLICATION_XHTML_XML_TYPE);

		templateURL = getClass().getResource("headed-page-template.ssp");
		sspService.registerScalaServerPage(templateURL, PLATFORM.HeadedPage, "(?!.*naked).*",
				MediaType.APPLICATION_XHTML_XML_TYPE);

		templateURL = getClass().getResource("headed-page-template.ssp");
		sspService.registerScalaServerPage(templateURL, HIERARCHY.Collection, "(?!.*naked).*",
				MediaType.APPLICATION_XHTML_XML_TYPE);
	}

}
