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

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.clerezza.platform.dashboard.GlobalMenuItem;
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.platform.typerendering.seedsnipe.SeedsnipeRenderlet;
import org.apache.clerezza.platform.usermanager.UserComparator;
import org.apache.clerezza.platform.usermanager.UserManager;
import org.apache.clerezza.platform.usermanager.webinterface.ontology.USERMANAGER;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.sparql.ParseException;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.LIST;
import org.apache.clerezza.rdf.ontologies.PERMISSION;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;
import org.apache.clerezza.rdf.utils.UnionMGraph;
import org.apache.clerezza.utils.customproperty.CustomProperty;
import org.apache.clerezza.utils.customproperty.ontology.CUSTOMPROPERTY;
import org.apache.clerezza.web.fileserver.BundlePathNode;
import org.apache.clerezza.web.fileserver.FileServer;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.osgi.framework.Bundle;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 * @author koersgen, hasan, tio
 */
@Component(metatype=true)
@Services({
	@Service(value=Object.class),
	@Service(value=GlobalMenuItemsProvider.class)
})
@Property(name="javax.ws.rs", boolValue=true)
@Path("/admin/user-manager")
public class UserManagerWeb implements GlobalMenuItemsProvider {
	
	@Reference(target=SystemConfig.SYSTEM_GRAPH_FILTER)
	private MGraph systemGraph;

	@Reference
	private RenderletManager renderletManager;

	@Reference
	private ContentGraphProvider cgProvider;

	@Reference
	private UserManager userManager;

	@Reference
	private CustomProperty customPropertyManager;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private FileServer fileServer;

	protected void activate(final ComponentContext context) throws IOException,
			URISyntaxException {
		Bundle bundle = context.getBundleContext().getBundle();
		URL resourceDir = getClass().getResource("staticweb");
		PathNode pathNode = new BundlePathNode(bundle, resourceDir.getPath());
		logger.info("Initializing file server for {} ({})", resourceDir,
				resourceDir.getFile());

		fileServer = new FileServer(pathNode);

		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource(
						"user-overview-template.xhtml").toURI().toString()),
				USERMANAGER.UserOverviewPage, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource("add-user-template.xhtml")
						.toURI().toString()), USERMANAGER.AddUserPage, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource(
						"user-permission-template.xhtml").toURI().toString()),
				USERMANAGER.UserPermissionPage, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource("update-user-template.xhtml")
						.toURI().toString()), USERMANAGER.UpdateUserPage, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource(
						"custom-property-template.xhtml").toURI().toString()),
				USERMANAGER.CustomFieldPage, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource(
						"role-overview-template.xhtml").toURI().toString()),
				USERMANAGER.RoleOverviewPage, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource(
						"role-permission-template.xhtml").toURI().toString()),
				USERMANAGER.RolePermissionPage, "naked",
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource(
						"add-single-property-template.xhtml").toURI()
						.toString()), USERMANAGER.SingleCustomPropertyPage,
				"naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(getClass().getResource(
						"add-multiple-property-template.xhtml").toURI()
						.toString()), USERMANAGER.MultipleCustomPropertyPage,
				"naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);
		renderletManager
				.registerRenderlet(SeedsnipeRenderlet.class.getName(),
						new UriRef(getClass().getResource(
								"custom-user-infos-template.xhtml").toURI()
								.toString()),
						USERMANAGER.CustomUserInformationPage, "naked",
						MediaType.APPLICATION_XHTML_XML_TYPE, true);
	}

	@GET
	public Response userMgmtHome(@Context UriInfo uriInfo) {
		if (uriInfo.getAbsolutePath().toString().endsWith("/")) {
			return RedirectUtil.createSeeOtherResponse("list-users", uriInfo);
		}
		return RedirectUtil.createSeeOtherResponse("user-manager/list-users",
				uriInfo);
	}

	@GET
	@Path("list-users")
	public GraphNode listUsers(@QueryParam(value = "from") Integer from,
			@QueryParam(value = "to") Integer to, @Context UriInfo uriInfo) {
		TrailingSlash.enforceNotPresent(uriInfo);

		MGraph resultGraph = new SimpleMGraph();

		Iterator<NonLiteral> users = userManager.getUsers();
		SortedSet<GraphNode> sortedSet = new TreeSet<GraphNode>(
				new UserComparator());
		while (users.hasNext()) {
			sortedSet.add(new GraphNode(users.next(), systemGraph));
		}
		List<GraphNode> userList = new ArrayList<GraphNode>();
		userList.addAll(sortedSet);

		if (from == null || from < 0) {
			logger.info("Query parameter from is null or negative, set to 1");
			from = 0;
		}
		if (to == null || to < 0) {
			logger.info("Query parameter to is null or negative, set to 10");
			to = 10;
		}
		if (to > userList.size()) {
			to = userList.size();
		}

		int prevFrom = from - 10;
		int nextTo = to + 10;

		if (prevFrom < 0) {
			prevFrom = 0;
		}
		if (nextTo > userList.size()) {
			nextTo = userList.size();
		}

		NonLiteral userOverviewPage = new BNode();
		List<Resource> resultList = new RdfList(userOverviewPage, resultGraph);
		for (int i = from; i < to; i++) {
			resultList.add(userList.get(i).getNode());
		}
		
		UnionMGraph unionGraph = new UnionMGraph(resultGraph, systemGraph);
		
		GraphNode result = new GraphNode(userOverviewPage, unionGraph);

		result.addProperty(RDF.type, USERMANAGER.UserOverviewPage);
		result.addProperty(RDF.type, PLATFORM.HeadedPage);
		result.addProperty(LIST.predecessor, new UriRef(uriInfo
				.getAbsolutePath().toString()
				+ "?from=" + prevFrom + "&to=" + from));
		result.addProperty(LIST.successor, new UriRef(uriInfo.getAbsolutePath()
				.toString()
				+ "?from=" + to + "&to=" + nextTo));
		result.addProperty(LIST.indexFrom, LiteralFactory.getInstance()
				.createTypedLiteral(from));
		result.addProperty(LIST.indexTo, LiteralFactory.getInstance()
				.createTypedLiteral(to));
		result.addProperty(LIST.length, LiteralFactory.getInstance()
				.createTypedLiteral(sortedSet.size()));
		
		return result;
	}

	@GET
	@Path("add-user")
	public GraphNode addUser(@Context UriInfo uriInfo) {

		TrailingSlash.enforceNotPresent(uriInfo);

		MGraph resultGraph = new SimpleMGraph();
		NonLiteral addUserPage = new BNode();

		resultGraph.add(new TripleImpl(addUserPage, RDF.type,
				USERMANAGER.AddUserPage));
		resultGraph.add(new TripleImpl(addUserPage, RDF.type,
				PLATFORM.HeadedPage));

		Iterator<NonLiteral> roles = userManager.getRoles();
		while (roles.hasNext()) {
			resultGraph.add(new TripleImpl(addUserPage, USERMANAGER.role, roles
					.next()));
		}
		MGraph contentGraph = cgProvider.getContentGraph();
		Iterator<Triple> formFields = contentGraph.filter(null, RDF.type,
				USERMANAGER.UserFormField);
		while (formFields.hasNext()) {
			resultGraph
					.add(new TripleImpl(addUserPage,
							CUSTOMPROPERTY.customfield, formFields.next()
									.getSubject()));
		}


		return new GraphNode(addUserPage, new UnionMGraph(
				resultGraph, systemGraph, contentGraph));
	}

	@POST
	@Consumes("multipart/form")
	@Path("add-user")
	public Response addUser(MultiPartBody form, @Context UriInfo uriInfo) {

		String userName = form.getTextParameterValues("userName")[0];
		String email = form.getTextParameterValues("email")[0];
		String pathPrefix = form.getTextParameterValues("pathPrefix")[0];
		String psw = form.getTextParameterValues("psw")[0];
		String[] userRole = form.getTextParameterValues("userRoles");

		List<String> userRoles = new ArrayList<String>();
		for (int i = 0; i < userRole.length; i++) {
			userRoles.add(userRole[i]);
		}

		StringWriter writer = new StringWriter();
		checkParamLength(writer, userName, "Username");
		checkQuote(writer, userName, "Username");
		checkParamLength(writer, email, "Email");
		checkQuote(writer, email, "Email");
		checkQuote(writer, pathPrefix, "Path-Prefix");
		checkParamLength(writer, psw, "Password");
		checkQuote(writer, psw, "Password");

		String message = writer.toString();
		if (!message.isEmpty()) {
			returnInputErrorMessages(message);
		}

		userManager.storeUser(userName, email, psw, userRoles, pathPrefix);

		MGraph contentGraph = cgProvider.getContentGraph();
		NonLiteral user = new BNode();
		contentGraph.add(new TripleImpl(user, RDF.type, FOAF.Agent));
		contentGraph.add(new TripleImpl(user, PLATFORM.userName, new PlainLiteralImpl(
				userName)));

		saveCustomUserInformation(contentGraph, userName, userRoles, form);
		return RedirectUtil.createSeeOtherResponse("list-users", uriInfo);
	}

	private void saveCustomUserInformation(MGraph contentGraph,
			String userName, List<String> roles, MultiPartBody form) {
		NonLiteral user = getCustomUser(contentGraph, userName);
		if (user != null) {
			for (int i = 0; i < roles.size(); i++) {
				NonLiteral collection = customPropertyManager
						.getCustomPropertyCollection(PERMISSION.Role, roles
								.get(i));
				ArrayList<UriRef> customproperties = customPropertyManager
						.getPropertiesOfCollection(collection);
				for (UriRef property : customproperties) {
					String[] values = form.getTextParameterValues(property
							.getUnicodeString());
					Iterator<Triple> actualValues = contentGraph.filter(user,
							property, null);
					while (actualValues.hasNext()) {
						contentGraph.remove(actualValues.next());
					}
					for (int k = 0; k < values.length; k++) {
						contentGraph.add(new TripleImpl(user, property,
								LiteralFactory.getInstance()
										.createTypedLiteral(values[k])));
					}
				}
			}
		} else {
			System.out.println("No such user in Database");
		}
	}

	private NonLiteral getCustomUser(MGraph contentGraph, String userName) {
		Iterator<Triple> users = contentGraph.filter(null, PLATFORM.userName,
				new PlainLiteralImpl(userName.trim()));
		if (users.hasNext()) {
			return users.next().getSubject();
		} else {
			return null;
		}
	}

	/**
	 * @return {@link GraphNode}
	 * @throws ParseException
	 */

	@GET
	@Path("custom-user")
	@Produces("text/plain")
	public GraphNode getRelevantCustomInformation(
			@QueryParam(value = "resource") UriRef resource,
			@QueryParam(value = "roles") String roles,
			@QueryParam(value = "user") String userName,
			@Context UriInfo uriInfo) throws ParseException {
		MGraph contentGraph = cgProvider.getContentGraph();
		MGraph resultGraph = new SimpleMGraph();
		NonLiteral node = new BNode();

		resultGraph.add(new TripleImpl(resource, USERMANAGER.custominformation,
				node));
		resultGraph.add(new TripleImpl(node, RDF.type,
				USERMANAGER.CustomUserInformationPage));

		ArrayList<NonLiteral> customfields = new ArrayList<NonLiteral>();

		if (roles != "" && roles.trim().length() > 0) {
			String[] rolesArray = roles.split(",");
			for (int i = 0; i < rolesArray.length; i++) {
				NonLiteral collection = customPropertyManager
						.getCustomPropertyCollection(PERMISSION.Role,
								rolesArray[i]);
				customfields.addAll(customPropertyManager
						.getCustomfieldsOfCollection(collection));
			}
		}

		for (NonLiteral customField : customfields) {
			UriRef property = customPropertyManager
					.getCustomFieldProperty(customField);

			if (userName != null && !userName.equals("")
					&& userName.trim().length() > 0) {
				NonLiteral user = getCustomUser(contentGraph, userName);
				if (user != null) {
					Iterator<Triple> values = contentGraph.filter(user,
							property, null);
					while (values.hasNext()) {
						Resource value = values.next().getObject();
						resultGraph.add(new TripleImpl(customField,
								CUSTOMPROPERTY.actualvalues, value));
					}
				}
			}
			resultGraph.add(new TripleImpl(node, CUSTOMPROPERTY.customfield,
					customField));
		}
		return new GraphNode(node, new UnionMGraph(resultGraph, contentGraph));
	}

	private boolean checkParamLength(StringWriter writer, String param,
			String paramName) {
		if ((param == null) || (param.trim().length() == 0)) {
			writer.append("<p>" + paramName + " may not be null</p>");
			return false;
		}
		return true;
	}

	private boolean checkQuote(StringWriter writer, String param,
			String paramName) {
		if ((param != null) && param.contains("\"")) {
			writer.append("<p>" + paramName + " may not contain \"</p>");
			return false;
		}
		return true;
	}

	private void returnInputErrorMessages(String message) {
		ResponseBuilder responseBuilder = Response
				.ok("<html><body><p>Input Error(s):</p>" + message
						+ "</body></html>");
		throw new WebApplicationException(responseBuilder.build());
	}

	@POST
	@Path("delete-user")
	public Response deleteUser(@FormParam(value = "userName") String userName,
			@Context UriInfo uriInfo) {

		checkUserParam(userName);
		MGraph contentGraph = cgProvider.getContentGraph();
		NonLiteral user = getCustomUser(contentGraph, userName);
		if (user != null) {
			Iterator<Triple> userTriples = contentGraph
					.filter(user, null, null);
			while (userTriples.hasNext()) {
				contentGraph.remove(userTriples.next());
			}
			userManager.deleteUser(userName);
			return RedirectUtil.createSeeOtherResponse("list-users", uriInfo);
		}
		return Response.status(Status.NOT_FOUND).entity(
				"User " + userName + "does not exist in our database").build();
	}

	private void checkUserParam(String userName) {
		StringWriter writer = new StringWriter();
		checkParamLength(writer, userName, "Username");
		checkQuote(writer, userName, "Username");
		String message = writer.toString();
		if (!message.isEmpty()) {
			returnInputErrorMessages(message);
		}
	}

	/**
	 * show page to manage user permissionEntries
	 */
	@GET
	@Path("manage-user-permissions")
	public GraphNode manageUserPermissions(
			@QueryParam(value = "userName") String userName,
			@Context UriInfo uriInfo) {

		TrailingSlash.enforceNotPresent(uriInfo);

		MGraph resultGraph = new SimpleMGraph();
		NonLiteral userPermissionPage = new BNode();
		resultGraph.add(new TripleImpl(userPermissionPage, RDF.type,
				PLATFORM.HeadedPage));
		resultGraph.add(new TripleImpl(userPermissionPage, RDF.type,
				USERMANAGER.UserPermissionPage));

		NonLiteral user = userManager.getUserByName(userName);
		if (user != null) {
			resultGraph.add(new TripleImpl(userPermissionPage,
					USERMANAGER.user, user));
			Iterator<NonLiteral> permissions = userManager
					.getPermissionsOfUser(user);
			while (permissions.hasNext()) {
				resultGraph.add(new TripleImpl(userPermissionPage,
						USERMANAGER.permission, permissions.next()));
			}
			return new GraphNode(userPermissionPage, new UnionMGraph(
					resultGraph, systemGraph));
			
		}
		throw new WebApplicationException(Response.status(Status.NOT_FOUND)
				.entity("User " + userName + "does not exist in our database")
				.build());
	}

	/**
	 * add user permissionEntries
	 */
	@POST
	@Path("add-user-permissions")
	public Response addUserPermissions(
			@FormParam(value = "name") String userName,
			@FormParam(value = "permEntries") List<String> permissionEntries,
			@Context UriInfo uriInfo) {
		checkUserParam(userName);
		userManager.assignPermissionsToUser(userName, permissionEntries);
		try {
			return RedirectUtil.createSeeOtherResponse(
					"manage-user-permissions?userName="
							+ URLEncoder.encode(userName, "UTF-8"), uriInfo);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * delete user permissionEntries
	 */
	@POST
	@Path("delete-user-permissions")
	public Response deleteUserPermissions(
			@FormParam(value = "name") String userName,
			@FormParam(value = "permEntries") List<String> permissionEntries,
			@Context UriInfo uriInfo) {
		checkUserParam(userName);
		userManager.deletePermissionsOfUser(userName, permissionEntries);
		try {
			return RedirectUtil.createSeeOtherResponse(
					"manage-user-permissions?userName="
							+ URLEncoder.encode(userName, "UTF-8"), uriInfo);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	@GET
	@Path("update-user")
	public GraphNode updateUser(
			@QueryParam(value = "userName") String userName,
			@Context UriInfo uriInfo) {
		TrailingSlash.enforceNotPresent(uriInfo);

		NonLiteral user = userManager.getUserByName(userName);
		if (user != null) {
			MGraph resultGraph = new SimpleMGraph();
			NonLiteral updateUserPage = new BNode();
			resultGraph.add(new TripleImpl(updateUserPage, RDF.type,
					USERMANAGER.UpdateUserPage));
			resultGraph.add(new TripleImpl(updateUserPage, RDF.type,
					PLATFORM.HeadedPage));
			Iterator<NonLiteral> roles = userManager.getRoles();
			while (roles.hasNext()) {
				resultGraph.add(new TripleImpl(updateUserPage,
						USERMANAGER.role, roles.next()));
			}

			MGraph contentGraph = cgProvider.getContentGraph();
			resultGraph.add(new TripleImpl(updateUserPage, USERMANAGER.user,
					user));

			Iterator<NonLiteral> userRoles = userManager
					.getRolesOfUser(user);

			ArrayList<NonLiteral> customfields = new ArrayList<NonLiteral>();

			while (userRoles.hasNext()) {
				customfields.addAll(customPropertyManager
						.getCustomfieldsOfCollection(customPropertyManager
								.getCustomPropertyCollection(PERMISSION.Role,
										userRoles.next().toString())));
			}
			for (NonLiteral customfield : customfields) {
				resultGraph.add(new TripleImpl(updateUserPage,
						CUSTOMPROPERTY.customfield, customfield));
				UriRef property = customPropertyManager
						.getCustomFieldProperty(customfield);
				NonLiteral contentUser = getCustomUser(contentGraph, userName);
				Iterator<Triple> values = contentGraph.filter(contentUser,
						property, null);
				while (values.hasNext()) {
					PlainLiteral value = (PlainLiteral) values.next()
							.getObject();
					resultGraph.add(new TripleImpl(customfield,
							CUSTOMPROPERTY.actualvalues, value));
				}
			}
			return new GraphNode(updateUserPage,
					new UnionMGraph(resultGraph, systemGraph, contentGraph));
		}
		throw new WebApplicationException(Response.status(Status.NOT_FOUND)
				.entity("User " + userName + "does not exist in our database")
				.build());
	}

	@POST
	@Consumes("multipart/form")
	@Path("update-user")
	public Response updateUser(MultiPartBody form, @Context UriInfo uriInfo)
			throws UnsupportedEncodingException {

		String userName = getTextParamValueOfForm(form, 0, "userName");
		String email = getTextParamValueOfForm(form, 0, "email");
		String pathPrefix = getTextParamValueOfForm(form, 0, "pathPrefix");
		String[] userRole = form.getTextParameterValues("userRoles");
		List<String> userRoles = new ArrayList<String>();
		for (int i = 0; i < userRole.length; i++) {
			userRoles.add(userRole[i]);
		}
		email = email.replaceAll("mailto:", "");
		NonLiteral user = userManager.getUserByName(userName);
		if (user != null) {
			userManager.updateUser(userName, email, null, userRoles,
					pathPrefix);
			MGraph contentGraph = cgProvider.getContentGraph();
			saveCustomUserInformation(contentGraph, userName, userRoles, form);
			return RedirectUtil.createSeeOtherResponse("list-users", uriInfo);
		}
		return Response.status(Status.NOT_FOUND).entity(
				"User " + userName + "does not exist in our database").build();
	}

	private String getTextParamValueOfForm(MultiPartBody form, int index,
			String paramName) {
		String value = form.getTextParameterValues(paramName)[index];
		return value.trim().length() > 0 ? value : null;
	}

	@GET
	@Path("list-roles")
	public GraphNode listRoles(@Context UriInfo uriInfo) {
		TrailingSlash.enforceNotPresent(uriInfo);

		MGraph resultGraph = new SimpleMGraph();
		NonLiteral roleOverviewPage = new BNode();
		resultGraph.add(new TripleImpl(roleOverviewPage, RDF.type,
				USERMANAGER.RoleOverviewPage));
		resultGraph.add(new TripleImpl(roleOverviewPage, RDF.type,
				PLATFORM.HeadedPage));

		Iterator<NonLiteral> roles = userManager.getRoles();

		while (roles.hasNext()) {
			resultGraph.add(new TripleImpl(roleOverviewPage, USERMANAGER.role,
					roles.next()));
		}
		
		return new GraphNode(roleOverviewPage,
				new UnionMGraph(resultGraph, systemGraph));
	}

	/**
	 * add role
	 * 
	 * @param roleTitle
	 *            the title of the new role to add
	 */
	@POST
	@Path("add-role")
	public Response addRole(@FormParam(value = "roleTitle") String title,
			@Context UriInfo uriInfo) {

		StringWriter writer = new StringWriter();
		checkParamLength(writer, title, "Role title");
		checkQuote(writer, title, "Role title");

		if (userManager.roleExists(title)) {
			writer.append("<p>Role with title " + title + " exists</p>");
		}
		String message = writer.toString();
		if (!message.isEmpty()) {
			returnInputErrorMessages(message);
		}
		userManager.storeRole(title);

		return RedirectUtil.createSeeOtherResponse("list-roles", uriInfo);
	}

	/**
	 * delete role
	 * 
	 * @param roleTitle
	 *            the title of the role to delete
	 */
	@POST
	@Path("delete-role")
	public Response deleteRole(@FormParam(value = "roleTitle") String title,
			@Context UriInfo uriInfo) {

		checkRoleParam(title);
		userManager.deleteRole(title);
		return RedirectUtil.createSeeOtherResponse("list-roles", uriInfo);
	}

	private void checkRoleParam(String title) {
		StringWriter writer = new StringWriter();
		checkParamLength(writer, title, "Role title");
		checkQuote(writer, title, "Role title");

		String message = writer.toString();
		if (!message.isEmpty()) {
			returnInputErrorMessages(message);
		}
	}

	/**
	 * show page to manage role permissionEntries
	 */
	@GET
	@Path("manage-role-permissions")
	public GraphNode manageRolePermissions(
			@QueryParam(value = "roleTitle") String title,
			@Context UriInfo uriInfo) {
		TrailingSlash.enforceNotPresent(uriInfo);

		MGraph resultGraph = new SimpleMGraph();
		NonLiteral rolePermissionPage = new BNode();
		resultGraph.add(new TripleImpl(rolePermissionPage, RDF.type,
				PLATFORM.HeadedPage));
		resultGraph.add(new TripleImpl(rolePermissionPage, RDF.type,
				USERMANAGER.RolePermissionPage));

		NonLiteral role = userManager.getRoleByTitle(title);
		if (role != null) {
			resultGraph.add(new TripleImpl(rolePermissionPage,
					USERMANAGER.role, role));
			Iterator<NonLiteral> permissions = userManager
					.getPermissionsOfRole(role);
			while (permissions.hasNext()) {
				resultGraph.add(new TripleImpl(rolePermissionPage,
						USERMANAGER.permission, permissions.next()));
			}
		}
		return new GraphNode(rolePermissionPage, new UnionMGraph(resultGraph,
				systemGraph));
	}

	/**
	 * add role permissionEntries
	 */
	@POST
	@Path("add-role-permissions")
	public Response addRolePermissions(
			@FormParam(value = "roleTitle") String title,
			@FormParam(value = "permEntries") List<String> permissionEntries,
			@Context UriInfo uriInfo) {

		checkRoleParam(title);
		userManager.assignPermissionsToRole(title, permissionEntries);
		try {
			return RedirectUtil.createSeeOtherResponse(
					"manage-role-permissions?roleTitle="
							+ URLEncoder.encode(title, "UTF-8"), uriInfo);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * delete role permissionEntries
	 */
	@POST
	@Path("delete-role-permissions")
	public Response deleteRolePermissions(
			@FormParam(value = "roleTitle") String title,
			@FormParam(value = "permEntries") List<String> permissionEntries,
			@Context UriInfo uriInfo) {

		checkRoleParam(title);
		userManager.deletePermissionsOfRole(title, permissionEntries);
		try {
			return RedirectUtil.createSeeOtherResponse(
					"manage-role-permissions?roleTitle="
							+ URLEncoder.encode(title, "UTF-8"), uriInfo);
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex);
		}
	}

	@GET
	@Path("add-single-property")
	public GraphNode getAddSinglePropertyPage(
			@QueryParam(value = "roleTitle") String role) {
		MGraph resultGraph = new SimpleMGraph();
		NonLiteral node = new BNode();
		resultGraph.add(new TripleImpl(node, RDF.type,
				USERMANAGER.SingleCustomPropertyPage));
		resultGraph.add(new TripleImpl(node, RDF.type,
				PLATFORM.HeadedPage));

		resultGraph.add(new TripleImpl(node, USERMANAGER.role,
				new PlainLiteralImpl(role)));
		return new GraphNode(node, resultGraph);
	}

	@GET
	@Path("add-multiple-property")
	public GraphNode getAddMultiplePropertyPage(
			@QueryParam(value = "roleTitle") String role) {
		MGraph resultGraph = new SimpleMGraph();
		NonLiteral node = new BNode();
		resultGraph.add(new TripleImpl(node, RDF.type,
				USERMANAGER.MultipleCustomPropertyPage));
		resultGraph.add(new TripleImpl(node, RDF.type,
				PLATFORM.HeadedPage));

		resultGraph.add(new TripleImpl(node, USERMANAGER.role,
				new PlainLiteralImpl(role)));
		return new GraphNode(node, resultGraph);
	}

	@POST
	@Path("add-single-property")
	@Produces("text/plain")
	public Response addSingleCustomField(
			@FormParam(value = "title") String title,
			@FormParam(value = "label") String label,
			@FormParam(value = "property") String property,
			@FormParam(value = "length") int length, @Context UriInfo uriInfo) {
		UriRef propertyUri = new UriRef(property);
		customPropertyManager.addSingleCustomField(PERMISSION.Role, title,
				label, propertyUri, length, 1);
		return RedirectUtil.createSeeOtherResponse("list-roles", uriInfo);
	}

	@POST
	@Path("add-multiple-property")
	@Produces("text/plain")
	public Response addMultipleCustomField(
			@FormParam(value = "mediaType") String title,
			@FormParam(value = "label") String label,
			@FormParam(value = "property") String property,
			@FormParam(value = "multiselect") String multiselect,
			@FormParam(value = "selectablevalues") String selectablevalues,
			@Context UriInfo uriInfo) {
		UriRef propertyUri = new UriRef(property);
		customPropertyManager.addMultipleCustomField(PERMISSION.Role, title,
				label, propertyUri, multiselect, selectablevalues, 1);
		return RedirectUtil.createSeeOtherResponse("list-roles", uriInfo);
	}

	@POST
	@Path("delete-custom-field")
	@Produces("text/plain")
	public Response deleteCustomProperty(
			@FormParam(value = "role") String role,
			@FormParam(value = "property") String property,
			@Context UriInfo uriInfo) {
		UriRef propertyUri = new UriRef(property);
		if (customPropertyManager.deleteCustomField(PERMISSION.Role, role,
				propertyUri)) {
			return RedirectUtil.createSeeOtherResponse(
					"manage-custom-properties?role=" + role, uriInfo);
		} else {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	@Path("manage-custom-properties")
	public GraphNode manageCustomProperties(
			@QueryParam(value = "role") String role, @Context UriInfo uriInfo) {
		TrailingSlash.enforceNotPresent(uriInfo);
		MGraph contentGraph = cgProvider.getContentGraph();
		MGraph resultGraph = new SimpleMGraph();
		NonLiteral propertyManagementPage = new BNode();
		resultGraph.add(new TripleImpl(propertyManagementPage,
				USERMANAGER.role, new PlainLiteralImpl(role)));
		resultGraph.add(new TripleImpl(propertyManagementPage, RDF.type,
				USERMANAGER.CustomFieldPage));
		resultGraph.add(new TripleImpl(propertyManagementPage, RDF.type,
				PLATFORM.HeadedPage));
		ArrayList<NonLiteral> customfields = customPropertyManager
				.getCustomfieldsOfCollection(customPropertyManager
						.getCustomPropertyCollection(PERMISSION.Role, role));
		for (NonLiteral customfield : customfields) {
			resultGraph.add(new TripleImpl(customfield, USERMANAGER.role,
					new PlainLiteralImpl(role)));
			resultGraph.add(new TripleImpl(propertyManagementPage,
					CUSTOMPROPERTY.customfield, customfield));
		}
		return new GraphNode(propertyManagementPage, new UnionMGraph(
				resultGraph, contentGraph));
	}

	/**
	 * Returns a PathNode of a static file from the staticweb folder.
	 * 
	 * @return {@link PathNode}
	 */
	@GET
	@Path("{path:.+}")
	public PathNode getStaticFile(@PathParam("path") String path) {
		final PathNode node = fileServer.getNode(path);
		return node;

	}

	@Override
	public Set<GlobalMenuItem> getMenuItems() {
		Set<GlobalMenuItem> items = new HashSet<GlobalMenuItem>();

		items.add(new GlobalMenuItem("/admin/user-manager/", "UMR", "User Manager", 2,
				"Main-Modules"));
		return items;
	}
}
