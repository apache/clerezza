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
package org.apache.clerezza.platform.style.classic;

import org.apache.clerezza.platform.typerendering.WebRenderingService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/**
 *
 * @author hasan
 */
@Component(immediate=true, metatype=true)
@Service(ClassicStyleConfig.class)
@WebRenderingService
public class ClassicStyleConfig {
	private static final String DEFAULT_JQUERY_URL = "/jquery/jquery-1.3.2.min.js";

	@Property(value = "/jquery/jquery-1.3.2.min.js",
		description = "Specifies the URL of the jQuery script")
	public static final String JQUERY_URL = "jQueryUrl";

	private String jQueryUrl = DEFAULT_JQUERY_URL;

	protected void activate(ComponentContext context) {
		jQueryUrl = (String) context.getProperties().get(JQUERY_URL);
	}

	public String getJQueryUrl() {
		return jQueryUrl.isEmpty() ? DEFAULT_JQUERY_URL : jQueryUrl;
	}
}
