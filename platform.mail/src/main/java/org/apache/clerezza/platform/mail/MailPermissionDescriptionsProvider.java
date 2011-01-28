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

import java.util.HashSet;
import java.util.Set;
import org.apache.clerezza.permissiondescriptions.PermissionDescriptionsProvider;
import org.apache.clerezza.permissiondescriptions.PermissionDescripton;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

/**
 * <code>PermissionDescriptionsProvider</code> implementation that provides
 * <code>PermissionDescripton</code>s for the most common <code>MailManPermission<code>s
 * used.
 * 
 * @author mir
 */
@Component
@Service(PermissionDescriptionsProvider.class)
public class MailPermissionDescriptionsProvider implements PermissionDescriptionsProvider {

	private static final Set<PermissionDescripton> MAIL_PERMISSION_DESCRIPTIONS = new HashSet<PermissionDescripton>();
	static {
		MAIL_PERMISSION_DESCRIPTIONS.add(new PermissionDescripton("Send Mails From Account Permission",
				"Grants permission to the user to send emails from his/her account. The sender email address will" +
				" be the address associated to the user account.", null, MailManPermission.class,
				"(org.apache.clerezza.platform.mail.MailManPermission \"" + MailManPermission.SELF_ACTION +
				"\" \"" + MailManPermission.SEND_FROM + "\")"));

		MAIL_PERMISSION_DESCRIPTIONS.add(new PermissionDescripton("Send Mails Permission",
				"Grants permission to send emails. The sender email address can be freely specified. " +
				"This permission does not grant permission to send emails from any accounts.", null, MailManPermission.class,
				"(org.apache.clerezza.platform.mail.MailManPermission \"\" \"" + MailManPermission.SEND_MAIL + "\")"));
	}

	@Override
	public Set<PermissionDescripton> getPermissionDescriptors() {
		return MAIL_PERMISSION_DESCRIPTIONS;
	}

}
