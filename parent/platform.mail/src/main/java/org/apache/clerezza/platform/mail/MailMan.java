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

import java.util.List;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.ws.rs.core.MediaType;
import org.apache.clerezza.rdf.utils.GraphNode;


/**
 * Implementations provide methods for sending emails, in particular to platform
 * users.
 * 
 * @author mir
 */
public interface MailMan {

	/**
	 * Sends the specified message to the email address of the specified platform
	 * user toUser. As sender address the email address of the fromUser is used.
	 * The subject of the email will be the specified subject.
	 * @param fromUser the user, which email address will be used as sender address ("from")
	 * @param toUser the user to whose email address the message will be sent ("to")
	 * @param subject the subject of the email
	 * @param message the message of the email
	 */
	public void sendEmailToUser(String fromUser, String toUser, String subject,
			String message) throws MessagingException;

	/**
	 * Sends the rendered <code>GraphNode</code> to the email address of the specified platform
	 * user toUser. As sender address the email address of the fromUser is used.
	 * The subject of the email will be the specified subject.
	 * An appropriate renderer for the <code>GraphNode</code> is selected on the
	 * basis of the specified <code>mode</code>, <code>acceptableMediaTypes</code> 
	 * and the rdf-type of the graph node itself.
	 * The <code>acceptableMediaTypes</code> list represent the media
	 * types that are acceptable for the rendered <code>GraphNode</code>. The list has a
	 * order where the most desirable media type is at the beginning of the list.
	 * The media type of the rendered <code>GraphNode</code> will be compatible to at least one
	 * media types in the list. <code>acceptableMediaTypes</code> and <code>mode</code> can
	 * be null. <code>acceptableMediaTypes = null</code> is equivalent to a list containing
	 * the media type "*\/*".
	 * @param fromUser the user, which email address will be used as sender address ("from")
	 * @param toUser the user to whose email address the message will be sent ("to")
	 * @param subject the subject of the email
	 * @param message the message of the email
	 */
	public void sendEmailToUser(String fromUser, String toUser, String subject,
			GraphNode graphNode, List<MediaType> acceptableMediaTypes, String mode)
			throws MessagingException ;

	/**
	 * Sends the specified message to the email addresses of the specified platform
	 * users toUsers. As sender address the email address of the fromUser is used.
	 * The subject of the email will be the specified subject. A recipient wont
	 * see the addresses or names of other recipients.
	 * @param fromUser the user, which email address will be used as sender address ("from")
	 * @param toUsers the users to whose email addresses the message will be sent ("to")
	 * @param subject the subject of the email
	 * @param graphNode will be rendered and sent as message body
	 * @param acceptableMediaTypes acceptable media types for the rendered output
	 * @param mode the rendering mode
	 */
	public void sendEmailToUsers(String fromUser, String[] toUsers,String subject,
			String message) throws MessagingException;

	/**
	 * Sends the rendered <code>GraphNode</code> to the email addresses of the specified platform
	 * users toUsers. As sender address the email address of the fromUser is used.
	 * The subject of the email will be the specified subject. A recipient wont
	 * see the addresses or names of other recipients.
	 * An appropriate renderer for the <code>GraphNode</code> is selected on the
	 * basis of the specified <code>mode</code>, <code>acceptableMediaTypes</code>
	 * and the rdf-type of the graph node itself.
	 * The <code>acceptableMediaTypes</code> list represent the media
	 * types that are acceptable for the rendered <code>GraphNode</code>. The list has a
	 * order where the most desirable media type is at the beginning of the list.
	 * The media type of the rendered <code>GraphNode</code> will be compatible to at least one
	 * media types in the list. <code>acceptableMediaTypes</code> and <code>mode</code> can
	 * be null. <code>acceptableMediaTypes = null</code> is equivalent to a list containing
	 * the media type "*\/*".
	 * @param fromUser the user, which email address will be used as sender address ("from")
	 * @param toUser the user to whose email address the message will be sent ("to")
	 * @param subject the subject of the email
	 * @param graphNode will be rendered and sent as message body
	 * @param acceptableMediaTypes acceptable media types for the rendered output
	 * @param mode the rendering mode
	 */
	public void sendEmailToUsers(String fromUser, String[] toUsers, String subject,
			GraphNode graphNode, List<MediaType> acceptableMediaTypes, String mode)
			throws MessagingException ;

	/**
	 * Sends an message with the specified subject to the specified
	 * <code>Adress</code> to and carbon copies to the <code>Adress<code>es
	 * cc and bcc (Not visible to other recipients). Cc and bcc can be null, all
	 * other parameter has to be specified.
	 * @param from sender address
	 * @param to recipient address
	 * @param cc addresess to which copies of the message will be sent to
	 * @param bcc addreses to which copies of the message will be sent to,
	 * but not visible to other recipients.
	 * @param subject the subject of the message
	 * @param message the message to be sent
	 * @deprecated use javax.mail with the session returned by MailSessionFactory
	 */
	@Deprecated
	public void sendEmail(InternetAddress from, InternetAddress to,
			InternetAddress[] cc, InternetAddress[] bcc, String subject,
			String message) throws MessagingException;

	/**
     * Sends an message with the specified subject to the specified
     * <code>Adress</code> to and carbon copies to the <code>Adress<code>es
     * cc and bcc (Not visible to other recipients). Cc and bcc can be null, all
     * other parameter has to be specified.
     * @author oliver straesser
     * @param from sender address
     * @param to recipient address
     * @param cc addresess to which copies of the message will be sent to
     * @param bcc addreses to which copies of the message will be sent to,
     * but not visible to other recipients.
     * @param subject the subject of the message
     * @param message the message to be sent
	 * @deprecated use javax.mail with the session returned by MailSessionFactory
     */
	@Deprecated
    public void sendEmail(InternetAddress from, InternetAddress to,
            InternetAddress[] cc, InternetAddress[] bcc, String subject,
            String message, String mediaType) throws MessagingException;

	/**
	 * Sends the rendered <code>GraphNode</code> with the specified subject to the specified
	 * <code>Adress</code> to and carbon copies to the <code>Adress<code>es
	 * cc and bcc (Not visible to other recipients). Cc and bcc can be null.
	 * The <code>acceptableMediaTypes</code> list represent the media
	 * types that are acceptable for the rendered <code>GraphNode</code>. The list has a
	 * order where the most desirable media type is at the beginning of the list.
	 * The media type of the rendered <code>GraphNode</code> will be compatible to at least one
	 * media types in the list. <code>acceptableMediaTypes</code> and <code>mode</code> can
	 * be null. <code>acceptableMediaTypes = null</code> is equivalent to a list containing
	 * the media type "*\/*".
	 * @param from sender address
	 * @param to recipient address
	 * @param cc addresess to which copies of the message will be sent to
	 * @param bcc addreses to which copies of the message will be sent to,
	 * but not visible to other recipients.
	 * @param subject the subject of the message
	 * @param graphNode will be rendered and sent as message body
	 * @param acceptableMediaTypes acceptable media types for the rendered output
	 * @param mode the rendering mode
	 */
	public void sendEmail(InternetAddress from, InternetAddress to,
			InternetAddress[] cc, InternetAddress[] bcc, String subject,
			GraphNode graphNode, List<MediaType> acceptableMediaTypes, String mode)
			throws MessagingException;
	
}
