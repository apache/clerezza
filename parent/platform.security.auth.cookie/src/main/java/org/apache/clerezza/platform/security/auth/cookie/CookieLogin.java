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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.security.auth.Subject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.security.auth.AuthenticationService;
import org.apache.clerezza.platform.security.auth.LoginListener;
import org.apache.clerezza.platform.security.auth.NoSuchAgent;
import org.apache.clerezza.platform.security.auth.cookie.onotology.LOGIN;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.web.fileserver.BundlePathNode;
import org.apache.clerezza.web.fileserver.FileServer;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.wymiwyg.commons.util.Base64;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 *
 * @author mir
 */
@Component
@Service(Object.class)
@Reference(name="loginListener",
		cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
		policy=ReferencePolicy.DYNAMIC,
		referenceInterface=LoginListener.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/login")
public class CookieLogin {

	/**
	 * Name of the authentication cookie
	 */
	public static final String AUTH_COOKIE_NAME = "auth";
	private final Logger logger = LoggerFactory.getLogger(CookieLogin.class);
	private FileServer fileServer;
	private final Set<LoginListener> loginListenerSet = Collections.synchronizedSet(new HashSet<LoginListener>());

	@Reference
	private RenderletManager renderletManager;

	@Reference
	AuthenticationService authenticationService;

	/**
	 * The activate method is called when SCR activates the component configuration.
	 *
	 * @param componentContext
	 */
	protected void activate(ComponentContext componentContext) {

		Bundle bundle = componentContext.getBundleContext().getBundle();
		URL resourceDir = getClass().getResource("staticweb");
		PathNode pathNode = new BundlePathNode(bundle, resourceDir.getPath());
		fileServer = new FileServer(pathNode);


		URL templateURL = getClass().getResource("login.xhtml");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(templateURL.toString()), LOGIN.LoginPage,
				null, MediaType.APPLICATION_XHTML_XML_TYPE, true);

		templateURL = getClass().getResource("login_naked.xhtml");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(templateURL.toString()), LOGIN.LoginPage,
				"naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);

		logger.info("Cookie Login activated.");
	}
	
	private String getUserName() {
		final AccessControlContext userContext = AccessController.getContext();
		Subject subject = AccessController.doPrivileged(
				new PrivilegedAction<Subject> (){
			@Override
			public Subject run() {
				return Subject.getSubject(userContext);
			}
		});
		if (subject == null) {
			return null;
		}
		Iterator<Principal> iter = subject.getPrincipals().iterator();
		String name = null;

		if (iter.hasNext()) {
			name = iter.next().getName();
		}
		return name;
	}

	@GET
	public GraphNode loginPage(@Context UriInfo uriInfo,
			@QueryParam("referer") String refererUri,
			@QueryParam("cause") Integer cause) {
		TrailingSlash.enforceNotPresent(uriInfo);
		GraphNode result = new GraphNode(new BNode(), new SimpleMGraph());
		result.addProperty(RDF.type, LOGIN.LoginPage);
		result.addProperty(LOGIN.refererUri, new UriRef(refererUri));
		String user = getUserName();
		if (!user.equals("anonymous") && cause != null &&
				cause.equals(CookieAuthentication.NOT_ENOUGH_PERMISSIONS)) {
			try {
				result.addProperty(LOGIN.message, new PlainLiteralImpl("The user " +
						user + " does not have the required permissions to view this page" +
						" (" + URLDecoder.decode(refererUri, "UTF-8") + ")." +
						" Please log in as another user."));
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException(ex);
			}
		}
		
		return result;
	}

	@POST
	public Object login(@FormParam("user") final String userName,
			@FormParam("pass") final String password,
			@FormParam("referer") final String referer,
			@DefaultValue("false") @FormParam("stayloggedin") final Boolean stayLoggedIn,
			@Context final UriInfo uriInfo) {
		return AccessController.doPrivileged(new PrivilegedAction<Object>() {
			@Override
			public Object run() {
				GraphNode result = new GraphNode(new BNode(), new SimpleMGraph());
				result.addProperty(RDF.type, LOGIN.LoginPage);
				PlainLiteral failedMessage = new PlainLiteralImpl(
						"Username name or password are wrong");
				try {
					if (authenticationService.authenticateUser(userName,password)) {
						Set<LoginListener> tempLoginListenerSet = null;
						synchronized(loginListenerSet) {
							tempLoginListenerSet = new HashSet<LoginListener>(loginListenerSet);
						}
						for (Iterator<LoginListener> it = tempLoginListenerSet.iterator(); it.hasNext();) {
							LoginListener listener = it.next();
							listener.userLoggedIn(userName);
						}
						ResponseBuilder responseBuilder = Response.fromResponse(
							RedirectUtil.createSeeOtherResponse(
							referer, uriInfo));
						responseBuilder.header(HttpHeaders.SET_COOKIE,
								getLoginCookie(userName, password, stayLoggedIn));
						return responseBuilder.build();
					} else {
						result.addProperty(LOGIN.message, failedMessage);
						result.addProperty(LOGIN.refererUri, new UriRef(referer));
					}
					return result;
				} catch (NoSuchAgent ex) {
					result.addProperty(LOGIN.message, failedMessage);
					result.addProperty(LOGIN.refererUri, new UriRef(referer));
					return result;
				}
			}
		});
	}

	/**
	 * Returns a Cookie that contains the Baes64 encoded username and password.
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	public static NewCookie getLoginCookie(String userName, String password,
			Boolean stayLoggedIn) {
		String cookieString = userName + ":" + password;
		int maxAge = -1;
		if (stayLoggedIn) {
			maxAge = Integer.MAX_VALUE;
		}
		NewCookie cookie = new NewCookie(AUTH_COOKIE_NAME, Base64.encode(
				cookieString.getBytes()), "/", null, null, maxAge, false);
		return cookie;
	}

	@GET
	@Path("{path:.+}")
	public PathNode getStaticFile(@PathParam("path") String path) {
		return fileServer.getNode(path);
	}

	/**
	 * Registers a <code>LoginListener</code>
	 *
	 * @param listener the listener to be registered
	 */
	protected void bindLoginListener(LoginListener listener) {
		loginListenerSet.add(listener);
	}

	/**
	 * Unregisters a <code>LoginListener</code>
	 *
	 * @param listener the listener to be unregistered
	 */
	protected void unbindLoginListener(LoginListener listener) {
		loginListenerSet.remove(listener);
	}
}
