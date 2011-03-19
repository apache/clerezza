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
package org.apache.clerezza.platform.typerendering.scalaserverpages;

import org.apache.clerezza.platform.typerendering.TypeRenderlet;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.scala.scripting.CompilerService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;

import javax.ws.rs.core.MediaType;
import java.net.URL;

/**
 * 
 * A service to create and register TypeRenderlets from ScalaServerPages
 * 
 * @author reto
 *  
 * 
 */
@Component
@Service(ScalaServerPagesService.class)
public class ScalaServerPagesService {


	@Reference
	private CompilerService scalaCompilerService;
	private BundleContext bundleContext;

	protected void activate(ComponentContext componentContext) {
		bundleContext = componentContext.getBundleContext();
	}

	protected void deactivate(ComponentContext componentContext) {
		bundleContext = null;
	}

	public ServiceRegistration registerScalaServerPage(URL location,  UriRef rdfType,
			String modePattern, MediaType mediaType) {
		TypeRenderlet sspTypeRenderlet = new SspTypeRenderlet(location, rdfType,
				modePattern, mediaType, scalaCompilerService);
		return bundleContext.registerService(TypeRenderlet.class.getName(),
				sspTypeRenderlet, null);
	}


}
