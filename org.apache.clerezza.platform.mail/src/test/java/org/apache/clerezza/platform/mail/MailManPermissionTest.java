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

import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.Collections;
import javax.security.auth.Subject;
import org.junit.Test;
import org.apache.clerezza.platform.security.auth.PrincipalImpl;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
/**
 *
 * @author mir
 */
public class MailManPermissionTest  {

	@Test
	public void testImpliesSendFrom() {
		Permission userX1 = new MailManPermission("userNameX", MailManPermission.SEND_FROM);
		Permission userX2 = new MailManPermission("userNameX", MailManPermission.SEND_FROM);
		Permission userY = new MailManPermission("userNameY", MailManPermission.SEND_FROM);
		Permission userAll = new MailManPermission("userName[A-Z]", MailManPermission.SEND_FROM);

		assertTrue(userX1.implies(userX2));
		assertTrue(userX1.equals(userX2));
		assertFalse(userX1.implies(userY));
		assertTrue(userAll.implies(userX1));
		assertTrue(userAll.implies(userY));
		assertFalse(userY.implies(userAll));
	}
	
	@Test
	public void testImpliesSelf() {
		final Permission userX = new MailManPermission("userNameX", MailManPermission.SEND_FROM);
		final Permission self = new MailManPermission(MailManPermission.SELF_ACTION, MailManPermission.SEND_FROM);

		Subject subject = new Subject(true,
				Collections.singleton(new PrincipalImpl("userNameX")),
				Collections.EMPTY_SET,
				Collections.EMPTY_SET);
		Subject.doAs(subject, new PrivilegedAction<Object>() {
			@Override
			public Object run() {
				assertTrue(self.implies(userX));
				return null;
			}

		});
		assertFalse(self.implies(userX));
	}

	@Test
	public void testImpliesSendMail() {
		Permission sendMail1 = new MailManPermission("userNameX", MailManPermission.SEND_MAIL);
		Permission sendMail2 = new MailManPermission("", MailManPermission.SEND_MAIL);
		Permission sendMailFrom = new MailManPermission("userNameY", MailManPermission.SEND_FROM +
				"," + MailManPermission.SEND_MAIL);
		Permission userY = new MailManPermission("userNameY", MailManPermission.SEND_FROM);
		assertTrue(sendMail1.implies(sendMail2));
		assertTrue(sendMailFrom.implies(sendMail1));
		assertFalse(sendMail1.implies(sendMailFrom));
		assertTrue(sendMailFrom.implies(userY));
	}

	@Test
	public void testGetActions() {
		Permission sendMailFrom1 = new MailManPermission("userNameY", MailManPermission.SEND_FROM +
				"," + MailManPermission.SEND_MAIL);
		Permission sendMailFrom2 = new MailManPermission("userNameY", MailManPermission.SEND_MAIL +
				"," + MailManPermission.SEND_FROM);

		assertEquals(sendMailFrom1.getActions(), sendMailFrom2.getActions());
	}

}
