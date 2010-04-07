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
package org.apache.clerezza.platform.content;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;


import javax.ws.rs.core.MediaType;

import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.platform.typerendering.seedsnipe.SeedsnipeRenderlet;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.DISCOBITS;
import org.apache.clerezza.rdf.ontologies.RDFS;

/**
 *
 * @author tio
 */
@Component(immediate = true)
public class DiscobitTemplating {

	@Reference
	RenderletManager renderletManager;

	protected void activate(ComponentContext context) {

		// register seedsnipe renderlets
		renderletManager.registerRenderlet(SeedsnipeRenderlet.class.getName(),
				new UriRef(getClass().getResource("Resource.xhtml").toString()),
				RDFS.Resource, null, MediaType.APPLICATION_XHTML_XML_TYPE, true);

		renderletManager.registerRenderlet(SeedsnipeRenderlet.class.getName(),
				new UriRef(getClass().getResource("Resource_naked.xhtml").toString()),
				RDFS.Resource, "naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);

		renderletManager.registerRenderlet(SeedsnipeRenderlet.class.getName(),
				new UriRef(getClass().getResource("XHTML_InfoDiscoBit_naked.xhtml").toString()),
				DISCOBITS.XHTMLInfoDiscoBit, "naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);

		renderletManager.registerRenderlet(SeedsnipeRenderlet.class.getName(),
				new UriRef(getClass().getResource("OrderedContent_naked.xhtml").toString()),
				DISCOBITS.OrderedContent, "naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);

		renderletManager.registerRenderlet(SeedsnipeRenderlet.class.getName(),
				new UriRef(getClass().getResource("TitledContent.xhtml").toString()),
				DISCOBITS.TitledContent, null, MediaType.APPLICATION_XHTML_XML_TYPE, true);

		renderletManager.registerRenderlet(TitledContentRenderlet.class.getName(),
				null, DISCOBITS.TitledContent, "naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);

		// registre renderlet for XMLLiteral datatype.
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource("XmlLiteral.ssp").toString()),
				new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral"), null,
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
	}
}
