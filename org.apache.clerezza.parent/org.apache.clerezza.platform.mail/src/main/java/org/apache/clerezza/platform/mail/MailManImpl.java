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
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.MediaType;
import org.osgi.service.component.ComponentContext;
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
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * @scr.component
 * @scr.service interface="org.apache.clerezza.platform.mail.MailMan"
 *
 * @author mir, daniel
 */
public class MailManImpl implements MailMan {

	/**
	 * Service property
	 *
	 * @scr.property value="false"
	 *               description="Specifies if TLS (SSL) encryption is used."
	 */
	public static final String MAIL_USE_TLS = "useTLS";
	/**
	 * Service property
	 *
	 * @scr.property value="smtp.myHost.org"
	 *               description="Specifies the SMTP host."
	 */
	public static final String MAIL_SMTP_HOST = "smtpHost";
	/**
	 * Service property
	 *
	 * @scr.property value="25" description="Specifies the SMTP port."
	 */
	public static final String MAIL_SMTP_PORT = "smtpPort";
	/**
	 * Service property
	 *
	 * @scr.property value="myPassword" description=
	 *               "Specifies the authentication password (plain text) for SMTP."
	 */
	public static final String MAIL_SMTP_PASSWORD = "smtpPassword";
	/**
	 * Service property
	 *
	 * @scr.property value="myUserName"
	 *               description="Specifies the User to authenticate for SMTP."
	 */
	public static final String MAIL_SMTP_USER = "smtpUser";
	private Properties properties;
	/**
	 * @scr.reference
	 */
	private TcManager tcManager;

	/**
	 * @scr.reference
	 */
	RendererFactory rendererFactory;


	private static String SYSTEM_GRAPH_URI = "http://tpf.localhost/system.graph";
	private UriRef systemGraphUri = new UriRef(SYSTEM_GRAPH_URI);

	protected void activate(ComponentContext componentContext) {
		properties = new Properties();

		properties.setProperty("mail.debug", "false");

		if (((String) componentContext.getProperties().get(MAIL_USE_TLS)).equals("true")) {
			properties.setProperty("mail.smtp.starttls.enable", "true");
			properties.setProperty("mail.smtp.auth", "true");
			properties.setProperty("mail.smtp.socketFactory.fallback", "false");
		}

		properties.setProperty("mail.transport.protocol", "smtp");
		properties.setProperty("mail.smtp.port", (String) componentContext.getProperties().get(MAIL_SMTP_PORT));
		properties.setProperty("mail.smtp.host", (String) componentContext.getProperties().get(MAIL_SMTP_HOST));
		properties.setProperty("smtpUser", (String) componentContext.getProperties().get(MAIL_SMTP_USER));
		properties.setProperty("smtpPassword", (String) componentContext.getProperties().get(MAIL_SMTP_PASSWORD));
	}

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

	public void sendEmail(final InternetAddress from,final InternetAddress to,
			final InternetAddress[] cc,	final InternetAddress[] bcc,
			final String subject, final Object content,	final String mediaType,
			final List<MediaType> acceptableMediaTypes,	final String mode)
			throws MessagingException {
		try {
			AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

				@Override
				public Object run() throws MessagingException {
					Authenticator auth = new Authenticator() {

						@Override
						public PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(properties.
									getProperty("smtpUser"),
									properties.getProperty("smtpPassword"));
						}
					};
					Session session = Session.getInstance(properties, auth);
					MimeMessage mimeMessage = new MimeMessage(session);
					Date date = new Date();
					mimeMessage.setSentDate(date);
					mimeMessage.setSubject(subject);
					if (content instanceof GraphNode) {
						GraphNode graphNode = (GraphNode) content;
						Renderer renderer = rendererFactory.
							createRenderer(graphNode, mode, 
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

		final String queryString = "SELECT ?email WHERE { " +
				"?x " + FOAF.mbox + " ?email . " +
				"?x " + FOAF.name + " \"" + user + "\" . " +
				"}";
		try {
			SelectQuery selectQuery = (SelectQuery) QueryParser.getInstance().parse(queryString);
			ResultSet result = tcManager.executeSparqlQuery(selectQuery, systemGraph);
			if (result.hasNext()) {
				Resource email = result.next().get("email");
				String emailString = ((UriRef) email).getUnicodeString();
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
