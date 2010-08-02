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


import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;

/**
 * Provides an isntance of javax.mail.Session matching the configuration of this 
 * service.
 *
 * @author reto
 */
@Component(metatype=true)
@Service(MailSessionFactory.class)
public class MailSessionFactory {
	/**
	 * Service property
	 *
	 */
	@Property(value="false", description="Specifies if TLS (SSL) encryption is used.")
	public static final String MAIL_USE_TLS = "useTLS";
	/**
	 * Service property
	 *
	 */
	@Property(value="localhost", description="Specifies the SMTP host.")
	public static final String MAIL_SMTP_HOST = "smtpHost";
	/**
	 * Service property
	 */
	@Property(value="25", description="Specifies the SMTP port.")
	public static final String MAIL_SMTP_PORT = "smtpPort";
	/**
	 * Service property
	 *
	 */
	@Property(value="myPassword", description="Specifies the authentication password for SMTP.")
	public static final String MAIL_SMTP_PASSWORD = "smtpPassword";
	/**
	 * Service property
	 *
	 */
	@Property(value="myUserName", description="Specifies the User to authenticate for SMTP.")
	public static final String MAIL_SMTP_USER = "smtpUser";

	private Properties properties;

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

	public Session getSession() {
		Authenticator auth = new Authenticator() {
			@Override
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(properties.
						getProperty("smtpUser"),
						properties.getProperty("smtpPassword"));
			}
		};
		return Session.getInstance(properties, auth);
	}
}
