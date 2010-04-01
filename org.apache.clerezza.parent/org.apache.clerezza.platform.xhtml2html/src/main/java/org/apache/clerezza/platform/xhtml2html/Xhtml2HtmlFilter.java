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
package org.apache.clerezza.platform.xhtml2html;

import java.util.Iterator;
import java.util.regex.Pattern;
import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.wymiwyg.wrhapi.Handler;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.Request;
import org.wymiwyg.wrhapi.Response;
import org.wymiwyg.wrhapi.filter.Filter;
import org.wymiwyg.wrhapi.util.AcceptHeaderEntry;
import org.wymiwyg.wrhapi.util.EnhancedRequest;

/**
 * This Filter acts on client agents matching a pattern defined by the 
 * service property <code>pattern</code>, it changes the returhned content type 
 * from application/xhtml+xml to application/html and adds application/xhtml+xml
 * to the accept header.
 *
 *
 * @scr.component
 * @scr.service interface="org.wymiwyg.wrhapi.filter.Filter"
 * @scr.property name="pattern"
 *               values.name=".*MSIE.*"
 *
 * @author rbn
 */
@Component
@Service(Filter.class)
@Property(name="pattern", value={".*MSIE.*", ""})
public class Xhtml2HtmlFilter implements Filter {

	private Pattern[] patterns;
	final MimeType xhtmlMimeType;
	final MimeType htmlMimeType;
	{
		try {
			xhtmlMimeType = new MimeType("application", "xhtml+xml");
			htmlMimeType = new MimeType("text", "html");
		} catch (MimeTypeParseException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void handle(Request request, Response response, Handler handler)
			throws HandlerException {
		if (!isApplicable(request)) {
			handler.handle(request, response);
		} else {
			handler.handle(new WrappedRequest(request), new WrappedResponse(response));
		}
	}

	private boolean isApplicable(final Request request) throws HandlerException {
		if (htmlPreferredInAccept(request)) {
			return true;
		}
		final String[] userAgentStrings = request.getHeaderValues(HeaderName.USER_AGENT);
		for (final String userAgentString : userAgentStrings) {
			for (final Pattern pattern : patterns) {
				if (pattern.matcher(userAgentString).matches()) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean htmlPreferredInAccept(Request request) throws HandlerException {
		EnhancedRequest ehRequest = new EnhancedRequest(request);
		Iterator<AcceptHeaderEntry> iter = ehRequest.getAccept();
		while (iter.hasNext()) {
			AcceptHeaderEntry entry = iter.next();
			if (entry.getRange().match(xhtmlMimeType)) {
				return false;
			}
			if (entry.getRange().match(htmlMimeType)) {
				return true;
			}
		}
		return false;
	}

	protected void activate(ComponentContext context) throws Exception {
		final String[] patternStrings = (String[]) context.getProperties().
				get("pattern");
		patterns = new Pattern[patternStrings.length];
		for(int i = 0; i < patternStrings.length; i++) {
			patterns[i] = Pattern.compile(patternStrings[i]);
		}
	}



}
