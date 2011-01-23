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

import java.util.Iterator;
import org.apache.clerezza.ssl.keygen.CertSerialisation;
import org.apache.clerezza.ssl.keygen.Certificate;
import org.apache.clerezza.foafssl.ontologies.CERT;
import org.apache.clerezza.foafssl.ontologies.RSA;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.jaxrs.utils.TrailingSlash;
import org.apache.clerezza.platform.accountcontrolpanel.ontologies.CONTROLPANEL;
import org.apache.clerezza.platform.config.PlatformConfig;
import org.apache.clerezza.platform.typerendering.RenderletManager;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesRenderlet;
import org.apache.clerezza.platform.usermanager.UserManager;
import org.apache.clerezza.platform.users.WebIdGraphsService;
import org.apache.clerezza.rdf.core.*;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.ontologies.DC;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.UnionMGraph;
import org.apache.clerezza.web.fileserver.FileServer;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.math.BigInteger;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import org.apache.clerezza.platform.typerendering.scala.PageRenderlet;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.ssl.keygen.KeygenService;

/**
 *
 * Presents a panel where the user can create a webid and edit her profile.
 * 
 * @author reto
 */
//@Component
//@Service(value = Object.class)
//@Property(name = "javax.ws.rs", boolValue = true)
@Path("/user/{id}/profile")
public class ProfilePanel extends FileServer {

	private static final Logger logger = LoggerFactory.getLogger(ProfilePanel.class);
//	@Reference
	private UserManager userManager;
//	@Reference
	private org.apache.clerezza.ssl.keygen.KeygenService keygenSrvc;
//	@Reference
	private TcManager tcManager;
//	@Reference
	private RenderletManager renderletManager;
//	@Reference
	private WebIdGraphsService webIdGraphsService;
//	@Reference
	private PlatformConfig platformConfig;

	protected void activate(ComponentContext componentContext) {
//		URL templateURL = getClass().getResource("profile_panel.scala");
//		renderletManager.registerRenderlet(PageRenderlet.class.getName(),
//				null, CONTROLPANEL.ProfilePage,
//				"naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);
//equivalent of above with no ssp?
//		renderletManager.registerRenderlet("org.apache.clerezza.platform.accountcontrolpanel.profile_panel", 
//				null, CONTROLPANEL.ProfilePage, "naked", MediaType.APPLICATION_XHTML_XML_TYPE, true);
		configure(componentContext.getBundleContext(), "profile-staticweb");
	}

	@GET
	public GraphNode getPersonalProfilePage(@Context UriInfo uriInfo, @PathParam(value = "id") String userName) {
		TrailingSlash.enforceNotPresent(uriInfo);
		GraphNode resultNode = getPersonalProfile(userName, new UriRef(uriInfo.getAbsolutePath().toString()));
		resultNode.addProperty(RDF.type, PLATFORM.HeadedPage);
		resultNode.addProperty(RDF.type, CONTROLPANEL.ProfilePage);
		return resultNode;
	}

	private GraphNode getPersonalProfile(final String userName, final UriRef profile) {
		return AccessController.doPrivileged(new PrivilegedAction<GraphNode>() {

			@Override
			public GraphNode run() {
				GraphNode userInSystemGraph = userManager.getUserInSystemGraph(userName);
				NonLiteral userNodeInSystemGraph = (NonLiteral) userInSystemGraph.getNode();
				if (userNodeInSystemGraph instanceof BNode) {
					//no personal profile without web-id
					SimpleMGraph simpleMGraph = new SimpleMGraph();
					GraphNode profileNode = new GraphNode(new BNode(), simpleMGraph);
					profileNode.addProperty(CONTROLPANEL.isLocalProfile,
							LiteralFactory.getInstance().createTypedLiteral(true));
					UriRef suggestedPPDUri = getSuggestedPPDUri(userName);
					profileNode.addProperty(CONTROLPANEL.suggestedPPDUri,
							LiteralFactory.getInstance().createTypedLiteral(suggestedPPDUri));
					NonLiteral agent = new BNode();
					profileNode.addProperty(FOAF.primaryTopic, agent);
					simpleMGraph.add(new TripleImpl(agent, PLATFORM.userName,
							LiteralFactory.getInstance().createTypedLiteral(userName)));
					return profileNode;
				} else {
					return getProfileInUserGraph((UriRef) userNodeInSystemGraph, profile);
				}
			}
		});
	}

	private UriRef getSuggestedPPDUri(String userName) {
		return new UriRef(platformConfig.getDefaultBaseUri().getUnicodeString()
				+ "user/" + userName + "/profile");
	}

	private GraphNode getProfileInUserGraph(UriRef webId, UriRef profile) {
		WebIdGraphsService.WebIdGraphs webIdGraphs = webIdGraphsService.getWebIdGraphs(webId);
		MGraph userGraph = webIdGraphs.publicUserGraph();
		logger.debug("got publicUserGraph of size {}.", userGraph.size());
		GraphNode userGraphNode = new GraphNode(webId, userGraph);
		GraphNode resultNode = new GraphNode(profile,
				new UnionMGraph(new SimpleMGraph(), userGraphNode.getGraph()));
		resultNode.addProperty(CONTROLPANEL.isLocalProfile,
				LiteralFactory.getInstance().createTypedLiteral(webIdGraphs.isLocal()));
		resultNode.addProperty(FOAF.primaryTopic, userGraphNode.getNode());
		return resultNode;
	}

	@POST
	@Path("set-existing-webid")
	public Response setExistingWebId(@Context final UriInfo uriInfo,
			@FormParam("webid") final UriRef webId, @PathParam(value = "id") final String userName) {
		//TODO check that its not local
		//TODO check its not an existing user
		return AccessController.doPrivileged(new PrivilegedAction<Response>() {

			@Override
			public Response run() {
				GraphNode userInSystemGraph = userManager.getUserInSystemGraph(userName);
				userInSystemGraph.replaceWith(webId);
				return RedirectUtil.createSeeOtherResponse("../profile", uriInfo);
			}
		});
	}

	@POST
	@Path("create-new-web-id")
	public Response createNewWebId(@Context final UriInfo uriInfo,
			@PathParam(value = "id") final String userName) {
		//TODO check its not an existing user
		final UriRef ppd = getSuggestedPPDUri(userName);
		final UriRef webId = new UriRef(ppd.getUnicodeString() + "#me");
		final WebIdGraphsService.WebIdGraphs webIdGraphs = webIdGraphsService.getWebIdGraphs(webId);
		webIdGraphs.localGraph().add(new TripleImpl(ppd, FOAF.primaryTopic, webId));
		webIdGraphs.localGraph().add(new TripleImpl(ppd, RDF.type, FOAF.PersonalProfileDocument));
		return AccessController.doPrivileged(new PrivilegedAction<Response>() {

			@Override
			public Response run() {
				GraphNode userInSystemGraph = userManager.getUserInSystemGraph(userName);
				userInSystemGraph.replaceWith(webId);
				return RedirectUtil.createSeeOtherResponse("../profile", uriInfo);
			}
		});
	}

	@POST
	@Path("keygen")
	public Response createCert(@FormParam("webId") UriRef webId,
			@FormParam("cn") String commonName,
			@FormParam("spkac") String spkac,
			@FormParam("crmf") String crmf,
			@FormParam("hours") String hours,
			@FormParam("days") String days,
			@FormParam("csr") String csr,
			@FormParam("comment") String comment) {

		logger.info("in keygen code. webId={}", webId);
		logger.info("cn={}", commonName);
		logger.info("hours={}", hours);
		logger.info("days={}", days);
		logger.info("spkac={}", spkac);
		logger.info("crmf={}", crmf);
		logger.info("csr={}", csr);

		Certificate cert = null;
		if (spkac != null && spkac.length() > 0) {
			cert = keygenSrvc.createFromSpkac(spkac);
			if (cert == null) {
				logger.warn("unable to create certificate from spkac request");
			}
		}
		if (cert == null && crmf != null && crmf.length() > 0) {
			cert = keygenSrvc.createFromCRMF(crmf);
			if (cert == null) {
				logger.warn("unable to create certificate from crmf requrest :" + crmf);
			}
		}
		if (cert == null && csr != null && csr.length() > 0) {
			cert = keygenSrvc.createFromPEM(csr);
			if (cert == null) {
				logger.warn("unable to create certificate from csr request :" + csr);
			}
		}
		if (cert == null) {
			throw new RuntimeException("The server was unable to craete a certificate");
		}
		cert.setSubjectCommonName(commonName);
		cert.addDurationInHours(hours);
		cert.addDurationInDays(days);
		cert.startEarlier("2"); // start a few hours earlier in order to remove chances of time synchronisation issues
		cert.setSubjectWebID(webId.getUnicodeString());

		CertSerialisation ser;
		try {
			ser = cert.getSerialisation();
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		RSAPublicKey pubKey = (RSAPublicKey) cert.getSubjectPublicKey().getPublicKey();
		BigInteger publicExponent = pubKey.getPublicExponent();
		BigInteger modulus = pubKey.getModulus();
		final WebIdGraphsService.WebIdGraphs webIdGraphs = webIdGraphsService.getWebIdGraphs(webId);
		final GraphNode certNode = new GraphNode(new BNode(), webIdGraphs.localGraph());
		certNode.addProperty(RDF.type, RSA.RSAPublicKey);
		certNode.addProperty(CERT.identity, webId);
		final GraphNode agent = new GraphNode(webId, webIdGraphs.localGraph());
		certNode.addPropertyValue(RSA.modulus, modulus);
		certNode.addPropertyValue(RSA.public_exponent, publicExponent);
		if (comment != null && comment.length() > 0) {
			certNode.addPropertyValue(RDFS.comment, comment);
		}
		certNode.addPropertyValue(DC.date, cert.getStartDate());

		Response.ResponseBuilder resBuild = Response.ok(ser.getContent(), MediaType.valueOf(ser.getMimeType()));
		return resBuild.build();

	}

	@POST
	@Path("deletekey")
	public Response deleteKey(@Context final UriInfo uriInfo,
			@FormParam("webId") final UriRef webId,
			@FormParam("keyhash") List<String> keys) {
		final WebIdGraphsService.WebIdGraphs webIdGraphs = webIdGraphsService.getWebIdGraphs(webId);
		final GraphNode agent = new GraphNode(webId, webIdGraphs.localGraph());
		Iterator<GraphNode> subjects = agent.getSubjectNodes(CERT.identity);
		for (GraphNode nl; subjects.hasNext();  ) {
			nl = subjects.next();
			Iterator<Resource> modulusIt = nl.getObjects(RSA.modulus);
			if (!modulusIt.hasNext()) break;
			Resource modLit = modulusIt.next(); //we only get the first, any more would be an error
			if (modulusIt.hasNext()) logger.warn("data error, a modulus too many in cert for "+webId);
			if (!(modLit instanceof TypedLiteral)) {
				logger.warn("a public key has a modulus that is not a literal for "+webId);
				break;
			}
			BigInteger modulus = LiteralFactory.getInstance().createObject(BigInteger.class, (TypedLiteral)modLit);
			for (String key : keys) {
				if (modulus.hashCode() == Integer.decode(key)) {
					//we delete the key. Even thous it is extreemly unlikely that anything could go wrong here
					//it would be a bitch to debug this. So one should probably just send the full key.
					//in fact it would be impossible to duplicate the problem
					nl.deleteNodeContext();
					break;
				}
			}
		}
		//shoud one really have a redirect? or should the post just be to the profile page?
	    return RedirectUtil.createSeeOtherResponse("../profile", uriInfo);
	}

	@POST
	@Path("modify")
	public Response modifyProfile(@Context final UriInfo uriInfo,
			@PathParam(value = "id") final String userName,
			@FormParam("webId") final UriRef webId,
			@FormParam("name") final String name,
			@FormParam("description") final String description) {
		final WebIdGraphsService.WebIdGraphs webIdGraphs = webIdGraphsService.getWebIdGraphs(webId);
		final GraphNode agent = new GraphNode(webId, webIdGraphs.localGraph());
		agent.deleteProperties(FOAF.name);
		agent.addPropertyValue(FOAF.name, name);
		agent.deleteProperties(DC.description);
		agent.addPropertyValue(DC.description, description);
		logger.debug("local graph (uri: {}) is now of size {}", webIdGraphs.localGraphUri(), webIdGraphs.localGraph().size());
		return RedirectUtil.createSeeOtherResponse("../profile", uriInfo);
	}

	protected void bindUserManager(UserManager usermanager) {
		userManager = usermanager;
	}

	protected void unbindUserManager(UserManager usermanager) {
		if (userManager == usermanager) {
			userManager = null;
		}
	}

	protected void bindKeygenSrvc(KeygenService keygenservice) {
		keygenSrvc = keygenservice;
	}

	protected void unbindKeygenSrvc(KeygenService keygenservice) {
		if (keygenSrvc == keygenservice) {
			keygenSrvc = null;
		}
	}

	protected void bindTcManager(TcManager tcmanager) {
		tcManager = tcmanager;
	}

	protected void unbindTcManager(TcManager tcmanager) {
		if (tcManager == tcmanager) {
			tcManager = null;
		}
	}

	protected void bindRenderletManager(RenderletManager renderletmanager) {
		renderletManager = renderletmanager;
	}

	protected void unbindRenderletManager(RenderletManager renderletmanager) {
		if (renderletManager == renderletmanager) {
			renderletManager = null;
		}
	}

	protected void bindWebIdGraphsService(WebIdGraphsService webidgraphsservice) {
		webIdGraphsService = webidgraphsservice;
	}

	protected void unbindWebIdGraphsService(WebIdGraphsService webidgraphsservice) {
		if (webIdGraphsService == webidgraphsservice) {
			webIdGraphsService = null;
		}
	}

	protected void bindPlatformConfig(PlatformConfig platformconfig) {
		platformConfig = platformconfig;
	}

	protected void unbindPlatformConfig(PlatformConfig platformconfig) {
		if (platformConfig == platformconfig) {
			platformConfig = null;
		}
	}
}
