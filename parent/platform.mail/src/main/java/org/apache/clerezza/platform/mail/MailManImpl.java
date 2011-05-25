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
package org.apache.clerezza.platform.mail;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.MediaType;
import org.apache.clerezza.platform.typerendering.Renderer;
import org.apache.clerezza.platform.typerendering.RendererFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.sparql.ParseException;
import org.apache.clerezza.rdf.core.sparql.QueryParser;
import org.apache.clerezza.rdf.core.sparql.ResultSet;
import org.apache.clerezza.rdf.core.sparql.query.SelectQuery;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.PLATFORM;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 *
 * Implementation of <code>MailMan</code> providing  utility methods to send
 * emails rendering GraphNodes
 *
 * @author mir, daniel
 */
@Component
@Service(MailMan.class)
@Reference
public class MailManImpl implements MailMan {

	@Reference
	private TcManager tcManager;

	@Reference
	RendererFactory rendererFactory;

	@Reference
	MailSessionFactory mailSessionFactory;


	private static String SYSTEM_GRAPH_URI = "urn:x-localinstance:/system.graph";
	private UriRef systemGraphUri = new UriRef(SYSTEM_GRAPH_URI);

	@Override
	public void sendEmailToUser(final String fromUser, final String toUser,
			final String subject, final String message) throws MessagingException {
		try {
			AccessController.checkPermission(new MailManPermission(fromUser, MailManPermission.SEND_FROM));
			AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

				@Override
				public Object run() throws MessagingException {
					InternetAddress fromAddress = getUserAddress(fromUser);
					InternetAddress toAddress = getUserAddress(toUser);
					sendEmail(fromAddress, toAddress, null, null, subject, message);
					return null;
				}
			});
		} catch (PrivilegedActionException ex) {
			if (ex.getException() instanceof MessagingException) {
				throw (MessagingException) ex.getException();
			} else {
				throw new RuntimeException(ex.getException());
			}

		}
	}

	@Override
	public void sendEmailToUser(final String fromUser, final String toUser,
			final String subject, final GraphNode graphNode,
			final List<MediaType> acceptableMediaTypes, final String mode)
			throws MessagingException {
		try {
			AccessController.checkPermission(
					new MailManPermission(fromUser, MailManPermission.SEND_FROM));
			AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

				@Override
				public Object run() throws MessagingException {
					InternetAddress fromAddress = getUserAddress(fromUser);
					InternetAddress toAddress = getUserAddress(toUser);
					sendEmail(fromAddress, toAddress, null, null, subject, graphNode,
							acceptableMediaTypes, mode);
					return null;
				}
			});
		} catch (PrivilegedActionException ex) {
			if (ex.getException() instanceof MessagingException) {
				throw (MessagingException) ex.getException();
			} else {
				throw new RuntimeException(ex.getException());
			}
		}
	}

	@Override
	public void sendEmailToUsers(final String fromUser, final String[] toUsers,
			final String subject, final String message) throws MessagingException {

		try {
			AccessController.checkPermission(
					new MailManPermission(fromUser, MailManPermission.SEND_FROM));
			AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

				@Override
				public Object run() throws MessagingException {
					for (int i = 0; i < toUsers.length; i++) {
						String toUser = toUsers[i];
						sendEmailToUser(fromUser, toUser, subject, message);
					}
					return null;
				}
			});
		} catch (PrivilegedActionException ex) {
			if (ex.getException() instanceof MessagingException) {
				throw (MessagingException) ex.getException();
			} else {
				throw new RuntimeException(ex.getException());
			}
		}
	}

	@Override
	public void sendEmailToUsers(final String fromUser, final String[] toUsers,
			final String subject, final GraphNode graphNode,
			final List<MediaType> acceptableMediaTypes, final String mode)
			throws MessagingException {
		try {
			AccessController.checkPermission(
					new MailManPermission(fromUser, MailManPermission.SEND_FROM));
			AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

				@Override
				public Object run() throws MessagingException {
					for (int i = 0; i < toUsers.length; i++) {
						String toUser = toUsers[i];
						sendEmailToUser(fromUser, toUser, subject, graphNode,
								acceptableMediaTypes, mode);
					}
					return null;
				}
			});
		} catch (PrivilegedActionException ex) {
			if (ex.getException() instanceof MessagingException) {
				throw (MessagingException) ex.getException();
			} else {
				throw new RuntimeException(ex.getException());
			}
		}
	}

	@Override
	public void sendEmail(InternetAddress from, InternetAddress to,
			InternetAddress[] cc, InternetAddress[] bcc, String subject,
			GraphNode graphNode, List<MediaType> acceptableMediaTypes,
			String mode) throws MessagingException {
		AccessController.checkPermission(
				new MailManPermission("", MailManPermission.SEND_MAIL));
		sendEmail(from, to, cc, bcc, subject, graphNode, null, acceptableMediaTypes,
				mode);
	}

	@Override
	public void sendEmail(InternetAddress from, InternetAddress to,
			InternetAddress[] cc, InternetAddress[] bcc, String subject,
			String message) throws MessagingException {
		AccessController.checkPermission(
				new MailManPermission("", MailManPermission.SEND_MAIL));
		sendEmail(from, to, cc, bcc, subject, message, "text/plain", null, null);
	}

	@Override
    public void sendEmail(InternetAddress from, InternetAddress to,
            InternetAddress[] cc, InternetAddress[] bcc, String subject,
            String message, String mediaType) throws MessagingException {
        AccessController.checkPermission(
                new MailManPermission("", MailManPermission.SEND_MAIL));
        sendEmail(from, to, cc, bcc, subject, message, mediaType, null, null);
    }

	private void sendEmail(final InternetAddress from, final InternetAddress to,
			final InternetAddress[] cc, final InternetAddress[] bcc,
			final String subject, final Object content, final String mediaType,
			final List<MediaType> acceptableMediaTypes, final String mode)
			throws MessagingException {
		final Session session = mailSessionFactory.getSession();
		try {
			AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

				@Override
				public Object run() throws MessagingException {
					MimeMessage mimeMessage = new MimeMessage(session);
					Date date = new Date();
					mimeMessage.setSentDate(date);
					mimeMessage.setSubject(subject);
					if (content instanceof GraphNode) {
						GraphNode graphNode = (GraphNode) content;
						Renderer renderer = rendererFactory.createRenderer(graphNode, mode,
								acceptableMediaTypes == null ? Collections.singletonList(
								MediaType.WILDCARD_TYPE) : acceptableMediaTypes);
						if (renderer == null) {
							throw new MessagingException("No renderer appropriate found");
						}
						mimeMessage.setDataHandler(
								new GraphNodeDataHandler(graphNode, renderer));
					} else {
						mimeMessage.setContent(content, mediaType);
					}
					mimeMessage.setFrom(from);
					if (to != null) {
						mimeMessage.addRecipient(Message.RecipientType.TO, to);
					}
					if (cc != null) {
						mimeMessage.addRecipients(Message.RecipientType.CC, cc);
					}
					if (bcc != null) {
						mimeMessage.addRecipients(Message.RecipientType.BCC, bcc);
					}
					Transport.send(mimeMessage);
					return null;
				}
			});
		} catch (PrivilegedActionException ex) {
			if (ex.getException() instanceof MessagingException) {
				throw (MessagingException) ex.getException();
			} else {
				throw new RuntimeException(ex.getException());
			}
		}

	}


	private InternetAddress getUserAddress(String user) throws MessagingException {
		MGraph systemGraph = tcManager.getMGraph(systemGraphUri);

		final String queryString = "SELECT ?email WHERE { "
				+ "?x " + FOAF.mbox + " ?email . "
				+ "?x " + PLATFORM.userName + " \"" + user + "\" . "
				+ "}";
		try {
			SelectQuery selectQuery = (SelectQuery) QueryParser.getInstance().parse(queryString);
			ResultSet result = tcManager.executeSparqlQuery(selectQuery, systemGraph);
			if (result.hasNext()) {
				Resource email = result.next().get("email");
				String emailString = ((UriRef) email).getUnicodeString();
				//TODO should add personal name (if available) as well
				return new InternetAddress(emailString.substring("mailto:".length()));
			}
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		} catch (AddressException ex) {
			throw new RuntimeException(ex);
		}
		throw new MessagingException("No address found for user " + user);
	}
}
