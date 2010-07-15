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
package org.apache.clerezza.platform.usermanager.webinterface.ontology;

import org.apache.clerezza.rdf.core.UriRef;

public class USERMANAGER {
	//Classes

	/**
	 * An RDF type denoting that the subject of this type is connected to 
	 * information needed to present a list of users on a web page.
	 */
	public static final UriRef UserOverviewPage = new UriRef("http://clerezza.org/2009/05/usermanager#UserOverviewPage");

	/**
	 * An RDF type denoting that the subject of this type is connected to 
	 * information needed to present a list of roles on a web page.
	 */
	public static final UriRef RoleOverviewPage = new UriRef("http://clerezza.org/2009/05/usermanager#RoleOverviewPage");

	/**
	 * An RDF type denoting that the subject of this type is connected to 
	 * information for presenting and managing permissions of a specific user.
	 */
	public static final UriRef UserPermissionPage = new UriRef("http://clerezza.org/2009/05/usermanager#UserPermissionPage");

	/**
	 * An RDF type denoting that the subject of this type is connected to 
	 * information for presenting and managing permissions of a specific role.
	 */
	public static final UriRef RolePermissionPage = new UriRef("http://clerezza.org/2009/05/usermanager#RolePermissionPage");

	/**
	 * An RDF type denoting that the subject of this type is connected to 
	 * information needed to present a web page for adding a new user.
	 */
	public static final UriRef AddUserPage = new UriRef("http://clerezza.org/2009/05/usermanager#AddUserPage");

	/**
	 * An RDF type denoting that the subject of this type is connected to 
	 * information needed to present a web page for updating user data.
	 */
	public static final UriRef UpdateUserPage = new UriRef("http://clerezza.org/2009/05/usermanager#UpdateUserPage");
	
	/**
	 * * An RDF type denoting that the subject of this type is connected to 
	 * information needed to present a web page for adding a customproperty to a role.
	 */
	public static final UriRef AddCustomPropertyPage = new UriRef("http://clerezza.org/2009/05/usermanager#AddCustomPropertyPage");
	
	/**
	 * An RDF type denoting that the subject of this type is connected to 
	 * information needed to present a web page for updating user data.
	 */
	public static final UriRef CustomUserInformationPage = new UriRef("http://clerezza.org/2009/05/usermanager#CustomUserInformationPage");
	
	/**
	 * An RDF type denoting that the subject of this type is connected to 
	 * information needed to present a web page for managing custom fields.
	 */
	public static final UriRef CustomFieldPage = new UriRef("http://clerezza.org/2009/05/usermanager#CustomFieldPage");

	/**
	 * An RDF type denoting that the subject, which is a custom field, 
	 * is used as one of the form fields to describe a user
	 */
	public static final UriRef UserFormField = new UriRef("http://clerezza.org/2009/05/usermanager#UserFormField");
	
	/**
	 * An RDF type denoting that the subject of this type is connected to
	 * information needed to present a web page for resetting a users password.
	 */
	public static final UriRef PasswordResetPage = new UriRef("http://clerezza.org/2009/05/usermanager#PasswordResetPage");
 
	/**
	 * An RDF type denoting that the subject of this type is connected to
	 * information needed to present a web page for informing a user that
	 * his/her password was successfully resetted.
	 */
	public static final UriRef PasswordResetSuccessPage = new UriRef("http://clerezza.org/2009/05/usermanager#PasswordResetSuccessPage");

	/**
	 * An RDF type denoting that the subject of this type is connected to
	 * information needed to compose an email containing the new password of
	 * a user.
	 */
	public static final UriRef PasswordResetMail = new UriRef("http://clerezza.org/2009/05/usermanager#PasswordResetMail");

	//Properties

	/**
	 * Points to an object defining a user.
	 */
	public static final UriRef user = new UriRef("http://clerezza.org/2009/05/usermanager#user");

	/**
	 * Points to an object defining a role.
	 */
	public static final UriRef role = new UriRef("http://clerezza.org/2009/05/usermanager#role");
	
	/**
	 * Points to a value of a custom field.
	 */
	public static final UriRef value = new UriRef("http://clerezza.org/2009/05/usermanager#value");
	
	/**
	 * Points to an object describing a permission.
	 */
	public static final UriRef permission = new UriRef("http://clerezza.org/2009/05/usermanager#permission");

	/**
	 * Points to an Agent.
	 */
	public static final UriRef recipient = new UriRef("http://clerezza.org/2009/05/usermanager#recipient");
	
	public static final UriRef custominformation = new UriRef("http://clerezza.org/2009/05/usermanager#custominformation");
}
