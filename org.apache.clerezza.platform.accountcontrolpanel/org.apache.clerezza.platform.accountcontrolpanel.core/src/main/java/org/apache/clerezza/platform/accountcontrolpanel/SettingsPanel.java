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
package org.apache.clerezza.platform.accountcontrolpanel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import javax.ws.rs.core.UriInfo;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.clerezza.platform.accountcontrolpanel.ontologies.CONTROLPANEL;
import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleLiteralFactory;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.OSGI;
import org.apache.clerezza.rdf.ontologies.PERMISSION;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.UnionMGraph;
import org.apache.clerezza.triaxrs.prefixmanager.TriaxrsPrefixManager;

/**
 * 
 * Account control panel
 * 
 * @author mir, hasan
 */
@Component
@Service(value = Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Reference(name = "configurationAdmin", cardinality = ReferenceCardinality.OPTIONAL_UNARY,
policy = ReferencePolicy.DYNAMIC, referenceInterface = ConfigurationAdmin.class)
@Path("/user/{id}/control-panel")
public class SettingsPanel {

	private ComponentContext componentContext;
	@Reference(target = SystemConfig.SYSTEM_GRAPH_FILTER)
	private MGraph systemGraph; // System graph for user data access
	@Reference
	private RenderletManager renderletManager;
	@Reference
	private ContentGraphProvider cgProvider;
	private final Logger logger = LoggerFactory.getLogger(SettingsPanel.class);
	private ConfigurationAdmin configAdmin;

	/**
	 * Mainpage
	 * 
	 * @param id is the username as given in the URL
	 * @return an array of installed {@link Bundle}s to be managed 
	 * 
	 */
	@GET
	public GraphNode settingsPage(@PathParam(value = "id") String idP,
			@QueryParam("changedPassword") String changedPassword,
			@Context UriInfo uriInfo) {
		TrailingSlash.enforceNotPresent(uriInfo);
		final String id = idP;
		GraphNode graphNode;
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""));
		try {
			AccessController.checkPermission(new UserBundlePermission(id, ""));
			graphNode = AccessController.doPrivileged(new PrivilegedAction<GraphNode>() {

				@Override
				public GraphNode run() {
					Bundle[] bundles = userBundles(getAgent(id));
					return asGraphNode(id, bundles);
				}
			});
		} catch (AccessControlException e) {
			graphNode = new GraphNode(new BNode(), new SimpleMGraph());
			graphNode.addProperty(CONTROLPANEL.userBundlePermission,
					SimpleLiteralFactory.getInstance().createTypedLiteral(
					new Boolean(false)));
		}
		try {
			AccessController.checkPermission(new ChangePasswordPermission(
					id, ""));
			graphNode.addProperty(CONTROLPANEL.changePasswordPermission,
					SimpleLiteralFactory.getInstance().createTypedLiteral(
					new Boolean(true)));
		} catch (AccessControlException e) {
			graphNode.addProperty(CONTROLPANEL.changePasswordPermission,
					SimpleLiteralFactory.getInstance().createTypedLiteral(
						Boolean.valueOf(false)));
		}
		if (changedPassword != null && changedPassword.equals("false")) {
			graphNode.addProperty(CONTROLPANEL.changedPassword,
				new PlainLiteralImpl("false"));
		}
		graphNode.addProperty(RDF.type, CONTROLPANEL.SettingsPage);
		graphNode.addProperty(RDF.type, PLATFORM.HeadedPage);
		return graphNode;
	}

	private void addBundleDescriptionToGraph(MGraph responseGraph, Bundle bundle) {
		TypedLiteral status = LiteralFactory.getInstance().
				createTypedLiteral(bundle.getState());
		UriRef bundleUri = new UriRef(bundle.getLocation());
		Triple triple = new TripleImpl(bundleUri, OSGI.status, status);
		responseGraph.add(triple);

		TypedLiteral bundleId = LiteralFactory.getInstance().
				createTypedLiteral(bundle.getBundleId());
		triple = new TripleImpl(bundleUri, OSGI.bundle_id, bundleId);
		responseGraph.add(triple);
	}

	private GraphNode asGraphNode(final String userId, Bundle[] bundles) {
		final MGraph responseGraph = new SimpleMGraph();
		for (Bundle bundle : bundles) {
			addBundleDescriptionToGraph(responseGraph, bundle);
		}
		return AccessController.doPrivileged(new PrivilegedAction<GraphNode>() {

			@Override
			public GraphNode run() {
				Graph userDescriptionGraph = new GraphNode(
						getAgent(userId), systemGraph).getNodeContext();
				UnionMGraph unionGraph = new UnionMGraph(responseGraph,
						userDescriptionGraph);
				GraphNode graphNode = new GraphNode(getAgent(userId), unionGraph);
				graphNode.addProperty(CONTROLPANEL.userBundlePermission,
						SimpleLiteralFactory.getInstance().createTypedLiteral(
						Boolean.valueOf(true)));
				return graphNode;
			}
		});

	}

	/**
	 * Retrieves all bundles owned by a user represented by agent
	 * 
	 * @param agent represents the user who owns bundles to be returned
	 * @return an array of {@link Bundle}s owned by the user
	 * 
	 */
	private Bundle[] userBundles(final NonLiteral agent) {
		logger.debug("Retrieve all bundles from user: {}", agent);
		Bundle[] installedBundles = componentContext.getBundleContext().getBundles();
		final Map<String, Long> locationMapper = new HashMap<String, Long>();
		for (Bundle b : installedBundles) {
			locationMapper.put(b.getLocation(), b.getBundleId());
		}

		return AccessController.doPrivileged(new PrivilegedAction<Bundle[]>() {

			@Override
			public Bundle[] run() {
				Set<Bundle> bundles = new HashSet<Bundle>();

				Iterator<Triple> agentBundles = systemGraph.filter(null,
						OSGI.owner, agent);
				while (agentBundles.hasNext()) {
					String location = ((UriRef) agentBundles.next().getSubject()).getUnicodeString();
					Long id = locationMapper.get(location);
					if (id != null) {
						bundles.add(componentContext.getBundleContext().getBundle(id));
					}
				}
				return bundles.toArray(new Bundle[bundles.size()]);
			}
		});
	}

	private NonLiteral getAgent(String id) {

		logger.debug("Get agent with id {}", id);

		Iterator<Triple> agents = systemGraph.filter(null, PLATFORM.userName,
				new PlainLiteralImpl(id));

		if (agents.hasNext()) {
			return agents.next().getSubject();
		} else {
			logger.debug("System graph does not contain user: {}", id);
			ResponseBuilder responseBuilder =
					Response.ok("<html><body>User does not exist</body></html>");
			throw new WebApplicationException(responseBuilder.build());
		}
	}

	private PlainLiteralImpl getAgentPathPrefix(final NonLiteral agent) {
		return AccessController.doPrivileged(new PrivilegedAction<PlainLiteralImpl>() {

			@Override
			public PlainLiteralImpl run() {
				Iterator<Triple> pathPrefixes = systemGraph.filter(agent,
						OSGI.agent_path_prefix, null);

				if (pathPrefixes.hasNext()) {
					return (PlainLiteralImpl) pathPrefixes.next().getObject();
				}

				return null;
			}
		});
	}

	/**
	 * Installs a bundle from the specified location.
	 *
	 * @param id is the username as given in the URL
	 * @param location specifies the URL of the bundle to be installed
	 * @return an array of installed {@link Bundle}s to be managed
	 *
	 */
	@POST
	@Path("install-bundle")
	@Consumes
	public Response installBundle(@PathParam(value = "id") final String id,
			MultiPartBody multiForm,
			@Context UriInfo uriInfo) {
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""));
		AccessController.checkPermission(new UserBundlePermission(id, ""));

		FormFile[] formFiles = multiForm.getFormFileParameterValues("bundle");
		String filename = formFiles[0].getFileName();
		byte[] bundleBytes = formFiles[0].getContent();

		if (bundleBytes.length == 0) {
			String message;
			if (filename.equals("")) {
				message = "No bundle specified";
			} else {
				message = "Bundle has length 0";
			}
			ResponseBuilder responseBuilder =
					Response.status(Response.Status.INTERNAL_SERVER_ERROR).
					entity(message);
			throw new WebApplicationException(responseBuilder.build());
		}
		final InputStream bundleInputStream = new ByteArrayInputStream(bundleBytes);

		final String location = "userbundle:" + id + "/" + filename;
		logger.info("Install bundle {} to location {}", id, location);

		AccessController.doPrivileged(new PrivilegedAction<Object>() {

			@Override
			public Bundle[] run() {
				final NonLiteral agent = getAgent(id);
				final Triple triple = new TripleImpl(new UriRef(location), OSGI.owner,
						agent);
				try {
					systemGraph.add(triple);
					Bundle bundle = componentContext.getBundleContext().
							installBundle(location, bundleInputStream);
					PlainLiteralImpl prefix = getAgentPathPrefix(agent);
					if (prefix != null) {
						addBundlePrefix(bundle, prefix.getLexicalForm());
					}
					return null;
				} catch (BundleException ex) {
					systemGraph.remove(triple);
					logger.debug("Failed to install a bundle from: {}", location);
					logger.error("Exception during install bundle: {}", ex);
					ResponseBuilder responseBuilder =
							Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							ex.getMessage());
					throw new WebApplicationException(responseBuilder.build());
				}
			}
		});
		return RedirectUtil.createSeeOtherResponse("../control-panel", uriInfo);
	}

	private void addBundlePrefix(Bundle bundle, String prefix) {
		String symbolicName = bundle.getSymbolicName();
		if (configAdmin != null) {
			try {
				Configuration configuration = configAdmin.getConfiguration(
						TriaxrsPrefixManager.class.getName());
				Dictionary properties = configuration.getProperties();
				if (properties == null) {
					properties = new Hashtable();
				}
				Dictionary mappings = TriaxrsPrefixManager.parseMappings(
						(String[]) properties.get(TriaxrsPrefixManager.TRIAXRS_MAPPINGS));
				logger.debug("Prefix {} added to bundle {}", prefix, symbolicName);
				mappings.put(symbolicName, prefix);
				String[] newMappings = TriaxrsPrefixManager.unparseMappings(mappings);
				properties.put(TriaxrsPrefixManager.TRIAXRS_MAPPINGS,
						newMappings);
				configuration.update(properties);
			} catch (IOException e) {
				logger.warn("Unable to update configuration: {}", e.toString());
			}
		} else {
			logger.warn("Cannot add prefix mapping. Configuration Admin is missing");
		}
	}

	void removeBundlePrefix(Bundle bundle) {
		String symbolicName = bundle.getSymbolicName();
		if ((this.configAdmin != null) && (symbolicName != null)) {
			try {
				Configuration configuration = configAdmin.getConfiguration(
						TriaxrsPrefixManager.class.getName());
				Dictionary properties = configuration.getProperties();
				if (properties == null) {
					properties = new Hashtable();
				}
				Dictionary mappings = TriaxrsPrefixManager.parseMappings(
						(String[]) properties.get(TriaxrsPrefixManager.TRIAXRS_MAPPINGS));
				mappings.remove(symbolicName);
				String[] newMappings = TriaxrsPrefixManager.unparseMappings(mappings);
				properties.put(TriaxrsPrefixManager.TRIAXRS_MAPPINGS,
						newMappings);
				configuration.update(properties);
			} catch (IOException e) {
				logger.warn("Unable to update configuration: {}", e.toString());
			}
		} else {
			logger.warn("Cannot add prefix mapping. Configuration Admin is missing");
		}
	}

	/**
	 * Starts the bundle with the specified bundle id.
	 * 
	 * @param id is the username as given in the URL
	 * @param bundleIdString specifies the id of the bundle to be started
	 * @return an array of installed {@link Bundle}s to be managed 
	 * 
	 */
	@POST
	@Path("start-bundle")
	public Response startBundle(@PathParam(value = "id") String idP,
			@FormParam("bundleId") String bundleIdStringP,
			@Context UriInfo uriInfo) {
		final String id = idP;
		final String bundleIdString = bundleIdStringP;
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""));
		AccessController.checkPermission(new UserBundlePermission(id, ""));
		logger.info("Start bundle {} ", id);

		final long bundleId = new Long(bundleIdString);

		AccessController.doPrivileged(new PrivilegedAction<Object>() {

			@Override
			public Bundle[] run() {
				try {
					final Bundle bundle = componentContext.getBundleContext().getBundle(bundleId);
					bundle.start();
				} catch (BundleException e) {
					logger.debug("Failed to start bundle {}", bundleIdString);
					logger.error("Exception during start bundle: {}", e);
					ResponseBuilder responseBuilder =
							Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							e.getMessage());
					throw new WebApplicationException(responseBuilder.build());
				}
				return null;
			}
		});
		return RedirectUtil.createSeeOtherResponse("../control-panel", uriInfo);

	}

	/**
	 * Stops the bundle with the specified bundle id.
	 * 
	 * @param id is the username as given in the URL
	 * @param bundleIdString specifies the id of the bundle to be stopped
	 * @return an array of installed {@link Bundle}s to be managed 
	 * 
	 */
	@POST
	@Path("stop-bundle")
	public Response stopBundle(@PathParam(value = "id") String idP,
			@FormParam("bundleId") String bundleIdStringP,
			@Context UriInfo uriInfo) {
		final String id = idP;
		final String bundleIdString = bundleIdStringP;
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""));
		AccessController.checkPermission(new UserBundlePermission(id, ""));
		logger.info("Stop bundle {}", id);

		final long bundleId = new Long(bundleIdString);
		AccessController.doPrivileged(new PrivilegedAction<Object>() {

			@Override
			public Bundle[] run() {
				try {
					Bundle bundle = componentContext.getBundleContext().getBundle(bundleId);
					bundle.stop();
				} catch (BundleException e) {
					logger.debug("Failed to stop bundle ", bundleIdString);
					logger.error("Exception during stop bundle: {}", e);
					ResponseBuilder responseBuilder =
							Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							e.getMessage());
					throw new WebApplicationException(responseBuilder.build());
				}
				return null;
			}
		});
		return RedirectUtil.createSeeOtherResponse("../control-panel", uriInfo);

	}

	/**
	 * Uninstalls the bundle with the specified bundle id.
	 * 
	 * @param id is the username as given in the URL
	 * @param bundleIdString specifies the id of the bundle to be uninstalled
	 * @return an array of installed {@link Bundle}s to be managed 
	 * 
	 */
	@POST
	@Path("uninstall-bundle")
	public Response uninstallBundle(@PathParam(value = "id") String idP,
			@FormParam("bundleId") String bundleIdStringP,
			@Context UriInfo uriInfo) {
		final String id = idP;
		final String bundleIdString = bundleIdStringP;
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""));
		AccessController.checkPermission(new UserBundlePermission(id, ""));
		logger.info("Uninstall bundle {}", id);

		final long bundleId = new Long(bundleIdString);
		AccessController.doPrivileged(new PrivilegedAction<Object>() {

			@Override
			public Bundle[] run() {
				final NonLiteral agent = getAgent(id);
				try {
					Bundle bundle = componentContext.getBundleContext().getBundle(bundleId);
					bundle.uninstall();
					final Triple triple = new TripleImpl(new UriRef(bundle.getLocation()),
							OSGI.owner, agent);
					systemGraph.remove(triple);
					removeBundlePrefix(bundle);
				} catch (BundleException e) {
					logger.debug("Failed to uninstall bundle {}", bundleIdString);
					logger.error("Exception during uninstall bundle: {}", e);
					ResponseBuilder responseBuilder =
							Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
							e.getMessage());
					throw new WebApplicationException(responseBuilder.build());
				}
				return null;
			}
		});
		return RedirectUtil.createSeeOtherResponse("../control-panel", uriInfo);

	}

	/**
	 * changes the password of an user
	 *
	 * @param idP id is the username as given in the URL
	 * @param lang represents the user's new standard language.
	 * @return
	 */
	@POST
	@Path("change-language")
	public Response changeUserLanguage(@PathParam(value = "id") String idP,
			@FormParam("availablelanguages") final String lang, @Context UriInfo uriInfo) {

		final String id = idP;
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""));
		AccessController.doPrivileged(new PrivilegedAction() {
			@Override
			public Object run() {
				GraphNode userNode = new GraphNode(getAgent(id), systemGraph);
				userNode.deleteProperties(PLATFORM.preferredLangInISOCode);
				userNode.addProperty(PLATFORM.preferredLangInISOCode, LiteralFactory.getInstance().createTypedLiteral(lang));
				return null;
			}
		});

		return RedirectUtil.createSeeOtherResponse("../control-panel", uriInfo);
	}

	/**
	 * changes the password of an user
	 * 
	 * @param idP id is the username as given in the URL
	 * @param oldPW the current user password
	 * @param newPW the new password
	 * @param confirmNewPW the new password
	 * @return
	 */
	@POST
	@Path("change-password")
	public Response changePassword(@PathParam(value = "id") String idP,
			@FormParam("oldPW") final String oldPW,
			@FormParam("newPW") final String newPW,
			@FormParam("confirmNewPW") final String confirmNewPW,
			@Context UriInfo uriInfo) {
		final String id = idP;
		AccessController.checkPermission(new AccountControlPanelAppPermission(id, ""));
		AccessController.checkPermission(new ChangePasswordPermission(id, ""));
		boolean changedPassword = false;
		if (newPW.trim().equals(confirmNewPW.trim()) && checkPWStrings(oldPW, newPW)) {
			changedPassword = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {

				private String getCurrentPassword(NonLiteral agent) {
					String currentPassword = null;
					Iterator<Triple> oldPWTriple = systemGraph.filter(agent,
							PERMISSION.passwordSha1, null);
					if (oldPWTriple.hasNext()) {
						Literal currentPWLiteral = (Literal) oldPWTriple.next().getObject();
						currentPassword = currentPWLiteral.getLexicalForm();
					}
					return currentPassword;
				}
				
				@Override
				public Boolean run() {
					final NonLiteral agent = getAgent(id);
					// The encoded current password which the user typed in 
					String encodedOlpPW = getEncodedPW(oldPW);
					// The current password which is in the system graph
					String currentPassword = getCurrentPassword(agent);
					if ((currentPassword != null) && !currentPassword.equals(encodedOlpPW)) {
						logger.info("Typed wrong current password!");
						return false;
					} else {
						removeOldPwAndAddNewPW(agent, currentPassword, newPW);
						return true;
					}
				}

				private void removeOldPwAndAddNewPW(NonLiteral agent, String currentPassword,
						String newPW) {
					Triple newPWTriple = new TripleImpl(agent,
							PERMISSION.passwordSha1,
							new PlainLiteralImpl(getEncodedPW(newPW)));
					if (currentPassword != null) {
						Triple oldPWTriple = new TripleImpl(agent,
							PERMISSION.passwordSha1, new PlainLiteralImpl(
							currentPassword));
						systemGraph.remove(oldPWTriple);
						logger.debug("removed old password from systemgraph");
					}
					systemGraph.add(newPWTriple);
					logger.debug("user " + id + " changed password");
				}

				private String getEncodedPW(String password) {
					if (password == null) {
						return null;
					}
					try {
						return bytes2HexString(MessageDigest.getInstance("SHA1").digest(
								password.getBytes("UTF-8")));

					} catch (NoSuchAlgorithmException e) {
						throw new RuntimeException(e);
					} catch (UnsupportedEncodingException e) {
						throw new RuntimeException(e);
					}
				}

				//Copied from org.apache.clerezza.platform.usermanager.basis.BasicUserManager				 
				private String bytes2HexString(byte[] bytes) {
					char[] HEXDIGITS = "0123456789abcdef".toCharArray();
					char[] result = new char[bytes.length << 1];
					for (int i = 0, j = 0; i < bytes.length; i++) {
						result[j++] = HEXDIGITS[bytes[i] >> 4 & 0xF];
						result[j++] = HEXDIGITS[bytes[i] & 0xF];
					}
					return new String(result);
				}
			});
		} else {
			logger.info("Changing password failed!");
			changedPassword = false;
		}
		if (changedPassword) {
			return RedirectUtil.createSeeOtherResponse("../control-panel", uriInfo);
		} else {
			return RedirectUtil.createSeeOtherResponse("../control-panel?changedPassword=false", uriInfo);
		}
	}

	/**
	 * checks if the typed strings are valid
	 */
	private boolean checkPWStrings(String oldPW, String newPW) {
		if (newPW.length() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * The activate method is called when SCR activates the component configuration.
	 * 
	 * @param componentContext
	 */
	protected void activate(ComponentContext componentContext) {
		this.componentContext = componentContext;
		URL templateURL = getClass().getResource("settings-panel.ssp");
		renderletManager.registerRenderlet(ScalaServerPagesRenderlet.class.getName(),
				new UriRef(templateURL.toString()), CONTROLPANEL.SettingsPage,
				"naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);

		logger.info("Account Control Panel activated.");
	}

	protected void bindConfigurationAdmin(ConfigurationAdmin configAdmin) {
		logger.debug("Binding configuration admin");
		this.configAdmin = configAdmin;
	}

	protected void unbindConfigurationAdmin(ConfigurationAdmin configAdmin) {
		logger.debug("Unbinding configuration admin");
		this.configAdmin = null;
	}

}
