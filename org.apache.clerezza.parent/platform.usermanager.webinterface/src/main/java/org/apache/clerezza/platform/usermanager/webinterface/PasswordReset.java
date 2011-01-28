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
package org.apache.clerezza.platform.usermanager.webinterface;

import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import javax.mail.MessagingException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.clerezza.platform.mail.MailMan;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.seedsnipe.SeedsnipeRenderlet;
import org.apache.clerezza.platform.usermanager.UserManager;
import org.apache.clerezza.platform.usermanager.webinterface.ontology.USERMANAGER;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.PERMISSION;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.UnionMGraph;
import org.wymiwyg.commons.util.Util;

/**
 * @author mir
 */
@Component(metatype=true)
@Service(value=Object.class)
@Property(name="javax.ws.rs", boolValue=true)
@Path("/reset")
public class PasswordReset {

	private final Logger logger = LoggerFactory.getLogger(PasswordReset.class);
	
	@Reference
	private RenderletManager renderletManager;
	
	@Reference
	private UserManager userManager;
	
	@Reference
	private MailMan mailMan;

	@Reference(target=SystemConfig.SYSTEM_GRAPH_FILTER)
	private LockableMGraph systemGraph;

	/**
	 * Service property
	 */
	@Property(value="admin",
		description="Platform user that is responsible for password management")
	public static final String PASSWORD_USER = "passwordUser";
	
	private String passwordUser;	

	/**
	 * The activate method is called when SCR activates the component configuration.
	 *
	 * @param componentContext
	 */
	protected void activate(ComponentContext componentContext) {
		URL templateURL = getClass().getResource("reset.xhtml");
		renderletManager.registerRenderlet(SeedsnipeRenderlet.class.getName(),
				new UriRef(templateURL.toString()), USERMANAGER.PasswordResetPage,
				null, MediaType.APPLICATION_XHTML_XML_TYPE, true);

		templateURL = getClass().getResource("reset_mail.txt");
		renderletManager.registerRenderlet(SeedsnipeRenderlet.class.getName(),
				new UriRef(templateURL.toString()), USERMANAGER.PasswordResetMail,
				null, MediaType.TEXT_PLAIN_TYPE, true);
		passwordUser = (String)componentContext.getProperties().get(PASSWORD_USER);
		logger.info("Password Reset activated.");
	}

	@GET
	public GraphNode resetPage(@Context UriInfo uriInfo) {
		TrailingSlash.enforcePresent(uriInfo);
		GraphNode result = new GraphNode(new BNode(), new SimpleMGraph());
		result.addProperty(RDF.type, USERMANAGER.PasswordResetPage);
		return result;
	}

	@POST
	public Response reset(@FormParam("user") final String userName,
			@FormParam("email") final String email,
			@Context final UriInfo uriInfo) {
		return AccessController.doPrivileged(new PrivilegedAction<Response>() {

			@Override
			public Response run() {
				String storedName = userManager.getNameByEmail(email);
				String newPassword = null;
				if (userName.equals(storedName)) {
					newPassword = Util.createRandomString(7);
					userManager.updateUser(userName, null, newPassword,
							Collections.EMPTY_LIST, null);
				} else {
					return createResponse("Username and e-mail address don't match.");
				}
				try {
					NonLiteral agent;
					Lock readLock = systemGraph.getLock().readLock();
					readLock.lock();
					try {
						Iterator<Triple> agents = systemGraph.filter(null, PLATFORM.userName,
								new PlainLiteralImpl(userName));
						agent = agents.next().getSubject();
					} finally {
						readLock.unlock();
					}
					MGraph temporary = new SimpleMGraph();
					temporary.add(new TripleImpl(agent, PERMISSION.password,
							new PlainLiteralImpl(newPassword)));
					MGraph result = new UnionMGraph(temporary, systemGraph);
					GraphNode mailGraph = new GraphNode(new BNode(), result);
					mailGraph.addProperty(RDF.type, USERMANAGER.PasswordResetMail);
					mailGraph.addProperty(USERMANAGER.recipient, agent);
					List<MediaType> acceptableMediaTypes =
					Collections.singletonList(MediaType.TEXT_PLAIN_TYPE);
					mailMan.sendEmailToUser(passwordUser, userName, "New Password",
					mailGraph, acceptableMediaTypes, null);
				} catch (MessagingException ex) {
					throw new RuntimeException(ex);
				}
				return createResponse("Successfully password reseted. Check your e-mail box. " +
						"An automatically generated password was sent to your e-mail address.");
			}
		});
	}

	private Response createResponse(String message) {
		Response.ResponseBuilder responseBuilder = Response.ok(message);
		return responseBuilder.build();
	}
}
