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

import java.io.IOException;
import java.util.Enumeration;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/**
 * This Filter acts on client agents matching a pattern defined by the service
 * property
 * <code>pattern</code>, it changes the returhned content type from
 * application/xhtml+xml to application/html and adds application/xhtml+xml to
 * the accept header.
 *
 */
@Component(immediate=true)
@Service(Filter.class)
@Properties(value = {
    @Property(name="pattern",value=".*"),
    @Property(name = "service.ranking", intValue = Integer.MAX_VALUE),
    @Property(name = "agent-pattern", value = {".*MSIE.*", ""})
})
public class Xhtml2HtmlFilter implements Filter {

    private Pattern[] patterns;


    private boolean isApplicable(final HttpServletRequest request) {
        if (htmlPreferredInAccept(request)) {
            return true;
        }
        final Enumeration<String> userAgentStrings = request.getHeaders("User-Agent");
        while (userAgentStrings.hasMoreElements()) {
            final String userAgentString = userAgentStrings.nextElement();
            for (final Pattern pattern : patterns) {
                if (pattern.matcher(userAgentString).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean htmlPreferredInAccept(HttpServletRequest request) {
        Enumeration<String> accepts = request.getHeaders("Accept");
        AcceptHeader acceptHeader = new AcceptHeader(accepts);
        for (MediaType accept : acceptHeader.getEntries()) {
            if (accept.isCompatible(MediaType.APPLICATION_XHTML_XML_TYPE)) {
                return false;
            }
            if (accept.isCompatible(MediaType.TEXT_HTML_TYPE)) {
                return true;
            }
        }
        return false;
    }

    protected void activate(ComponentContext context) throws Exception {
        final String[] patternStrings = (String[]) context.getProperties().
                get("agent-pattern");
        patterns = new Pattern[patternStrings.length];
        for (int i = 0; i < patternStrings.length; i++) {
            patterns[i] = Pattern.compile(patternStrings[i]);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request,
            ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!isApplicable((HttpServletRequest)request)) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(new WrappedRequest((HttpServletRequest)request), 
                    new WrappedResponse((HttpServletResponse)response));
        }
    }

    @Override
    public void destroy() {
    }
}
