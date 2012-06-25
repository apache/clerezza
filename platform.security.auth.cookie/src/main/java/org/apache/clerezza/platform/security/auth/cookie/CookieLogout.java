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
package org.apache.clerezza.platform.security.auth.cookie;

import java.net.URI;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.security.auth.cookie.onotology.LOGIN;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.wymiwyg.wrhapi.util.Cookie;

/**
 *
 * @author mir
 */
@Component
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/logout")
public class CookieLogout {

    private final Logger logger = LoggerFactory.getLogger(CookieLogout.class);
    @Reference
    private RenderletManager renderletManager;
 
    /**
     * The activate method is called when SCR activates the component configuration.
     *
     * @param componentContext
     */
    protected void activate(ComponentContext componentContext) {

	logger.info("Cookie Logout activated.");
    }

    @GET
    public Response logout(@Context UriInfo uriInfo,
	    @HeaderParam("Referer") URI referer,
	    @Context ServletRequest req) {
	TrailingSlash.enforceNotPresent(uriInfo);
	ResponseBuilder responseBuilder;

	if (referer != null) {
	    responseBuilder = Response.seeOther(referer);
	} else {
	    responseBuilder = Response.fromResponse(
		    RedirectUtil.createSeeOtherResponse("logout/success", uriInfo));
	}
	responseBuilder.header("Connection", "close"); //will
	logger.info("logout! Closing connection");
	//we need to get the ssl session.
	//With tomcat this works with javax.servlet.request.ssl_session_mgr attribute as
	//explained here http://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html
	if (req != null) {
	    HttpSession session = ((HttpServletRequest) req).getSession();
	    if (session != null) {
		session.invalidate();
		logger.info("logout! invalidating session");
	    }
	} else {
	    logger.info("request is null!");
	}
	responseBuilder.header(HttpHeaders.SET_COOKIE, getLogoutCookie());
	return responseBuilder.build();
    }

    @GET
    @Path("success")
    public GraphNode logoutSuccessPage(@Context UriInfo uriInfo) {
	TrailingSlash.enforcePresent(uriInfo);
	GraphNode result = new GraphNode(new BNode(), new SimpleMGraph());
	PlainLiteral message = new PlainLiteralImpl(
		"You successfully logged out.");
	result.addProperty(LOGIN.message, message);
	result.addProperty(RDF.type, LOGIN.LoginPage);

	String baseUri = uriInfo.getBaseUri().getScheme() + "://"
		+ uriInfo.getBaseUri().getAuthority();

	result.addProperty(LOGIN.refererUri, new UriRef(baseUri + "/dashboard/overview"));
	return result;
    }

    public static Cookie getLogoutCookie() {
	Cookie cookie = new Cookie(CookieLogin.AUTH_COOKIE_NAME, null);
	cookie.setMaxAge(0);
	return cookie;
    }
}
