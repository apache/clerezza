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
package org.apache.clerezza.platform.defaultacceptheader;

import java.util.Iterator;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.wymiwyg.wrhapi.Handler;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.filter.Filter;
import org.wymiwyg.wrhapi.util.AcceptHeaderEntry;
import org.wymiwyg.wrhapi.util.EnhancedRequest;
import org.wymiwyg.wrhapi.util.InvalidPatternException;
import org.wymiwyg.wrhapi.util.MediaRange;

/**
 * This Filter sets the default accet header if the request has no accept
 * header or if only the wildcard media type "*\/*" is set as acceptable media type.
 * The default accept header can be configured over the OSGi configuration admin
 * or conveniently over the Felix Web Console.
 *
 * @author mir
 */
@Component(metatype=true)
@Service(Filter.class)
public class DefaultAcceptHeaderSetter implements Filter {


	@Property(value={"application/xhtml+xml", "text/html;q=.9", "application/rdf+xml;q=.8",
		"*/*;q=.1"},
	description="The default accept header used if a request does not have an " +
			"accept header.")
	public static final String DEFAULT_ACCEPT_HEADER = "defaultAcceptHeader";

	private String[] defaultAcceptHeader;
	
	private MediaRange wildcardRange;

	public DefaultAcceptHeaderSetter() throws InvalidPatternException {
		wildcardRange = new MediaRange("*/*");
	}
		

	@Override
	public void handle(Request request, Response response, Handler handler)
			throws HandlerException {
		if (hasNoAcceptHeaderOrOnlyWildcard(request)) {
			handler.handle(new AcceptAddingRequest(request, defaultAcceptHeader),
					response);			
		} else {
			handler.handle(request, response);		
		}
	}

	private boolean hasNoAcceptHeaderOrOnlyWildcard(final Request request) throws HandlerException {
		EnhancedRequest ehRequest = new EnhancedRequest(request);
		Iterator<AcceptHeaderEntry> iter = ehRequest.getAccept();
		if (!iter.hasNext()) {
			return true;
		}
		AcceptHeaderEntry entry = iter.next();
		if (entry.getRange().equals(wildcardRange) && !iter.hasNext()) {
			return true;
		}
		return false;
	}

	protected void activate(ComponentContext context) throws Exception {
		defaultAcceptHeader = (String[]) context.getProperties().
				get(DEFAULT_ACCEPT_HEADER);
	}



}
