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
package org.apache.clerezza.integrationtest.web.framework;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.osgi.service.component.ComponentContext;

/**
 * Sends an E-Mail notification when the Integrationtest-Framework terminates
 * abnormally.
 * 
 * @author daniel
 * 
 * @scr.component
 * @scr.service interface=
 *              "org.apache.clerezza.integrationtest.web.framework.AbnormalTeminationNotificator"
 */
public class MailAbnormalTerminationNotificator implements
		AbnormalTeminationNotificator {

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

	/**
	 * Service property
	 * 
	 * @scr.property value="integrationtest@myHost.org"
	 *               description="Specifies the E-Mail address of the sender."
	 */
	public static final String MAIL_SENDER = "emailSender";

	/**
	 * Service property
	 * 
	 * @scr.property value="myName@myHost.org"
	 *               description="Specifies the E-Mail address of the recipient."
	 */
	public static final String MAIL_RECIPIENT = "emailRecipient";

	/**
	 * Service property
	 * 
	 * @scr.property value="[Integrationtest-Framework]"
	 *               description="Specifies the begining of the E-Mail subject."
	 */
	public static final String MAIL_SUBJECT_INTRO = "subjectIntro";

	private Properties properties;

	protected void activate(ComponentContext componentContext) {
		properties = new Properties();

		properties.setProperty("mail.debug", "false");
		
		if (((String) componentContext.getProperties().get(MAIL_USE_TLS))
				.equals("true")) {

			properties.setProperty("mail.smtp.starttls.enable", "true");
			properties.setProperty("mail.smtp.auth", "true");
			properties.setProperty("mail.smtp.socketFactory.fallback", "false");
		}

		properties.setProperty("mail.transport.protocol", "smtp");
		properties.setProperty("mail.smtp.port", (String) componentContext
				.getProperties().get(MAIL_SMTP_PORT));
		properties.setProperty("emailRecipient", (String) componentContext
				.getProperties().get(MAIL_RECIPIENT));
		properties.setProperty("mail.from", (String) componentContext
				.getProperties().get(MAIL_SENDER));
		properties.setProperty("mail.smtp.host", (String) componentContext
				.getProperties().get(MAIL_SMTP_HOST));
		properties.setProperty("smtpUser", (String) componentContext
				.getProperties().get(MAIL_SMTP_USER));
		properties.setProperty("smtpPassword", (String) componentContext
				.getProperties().get(MAIL_SMTP_PASSWORD));
		properties.setProperty("subjectIntro", (String) componentContext
				.getProperties().get(MAIL_SUBJECT_INTRO));

	}

	@Override
	public void notifyAbnormalTermination(
			List<ExceptionDescription> exeptionDescriptionList) {

		StringWriter messageBody = new StringWriter();
		PrintWriter printWriter = new PrintWriter(messageBody);

		Date date = new Date();

		printWriter.println(date);

		for (ExceptionDescription exceptionDescription : exeptionDescriptionList) {
			printWriter.print("The test-thread ");
			printWriter.print(exceptionDescription.getTestThread().getName());
			printWriter.print(" got a ");
			printWriter.print(exceptionDescription.getException().getClass()
					.getName());
			printWriter.print(" after ");
			printWriter.print(exceptionDescription.getTimeInNanos());
			printWriter.println(" ns");
			exceptionDescription.getException().printStackTrace(printWriter);
		}

		printWriter.close();

		try {
			Address to = new InternetAddress(properties
					.getProperty("emailRecipient"));

			Authenticator auth = new Authenticator() {
				@Override
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(properties
							.getProperty("smtpUser"), properties
							.getProperty("smtpPassword"));
				}
			};

			Session session = Session.getInstance(properties, auth);

			MimeMessage message = new MimeMessage(session);
			message.setSentDate(date);
			message
					.setSubject(properties.getProperty("subjectIntro")
							+ " "
							+ exeptionDescriptionList.get(0).getException()
									.getClass().getName()
							+ " in "
							+ exeptionDescriptionList.get(0).getTestThread()
									.getName() + ".");
			message.setText(messageBody.toString());
			message.setFrom();
			message.addRecipient(Message.RecipientType.TO, to);

			Transport.send(message);

		} catch (MessagingException ex) {
			throw new RuntimeException(ex);
		}
	}
}