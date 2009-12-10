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
package org.apache.clerezza.platform.usermanager;

import java.util.Iterator;
import java.util.List;

import org.apache.clerezza.rdf.core.NonLiteral;

/**
 * An implementation of this interface provides methods to manage data about 
 * users and their roles.
 * Data managed are needed for authentication and for setting permissions.
 * Those data include user names, email addresses, passwords, roles, permissions, 
 * etc.
 * A user is uniquely identified by a user name.
 * Each user has an email address and an email address can only belong to a user.
 * 
 * @author hasan
 */
public interface UserManagementProvider {

	/**
	 * 
	 * @param title
	 *		the title of the role, may not be null
	 */
	public void storeRole(String title);

	public boolean roleExists(String title);

	public NonLiteral getRoleByTitle(String title);
	
	public Iterator<NonLiteral> getRoles();
	
	/**
	 * 
	 * @param user	
	 *		
	 * @return Iterator 	defining all the Roles the specified user owns
	 */
	public Iterator<NonLiteral> getRolesOfUser(NonLiteral user);

	/**
	 * 
	 * @param title
	 *		the title of the role to be deleted, may not be null
	 */
	public void deleteRole(String title);

	public void assignPermissionsToRole(String title,
			List<String> permissionEntries);

	public Iterator<NonLiteral> getPermissionsOfRole(NonLiteral role);

	public void deletePermissionsOfRole(String title,
			List<String> permissionEntries);

	public void deleteAllPermissionsOfRole(String title);

	/**
	 *
	 * @param name
	 *		the name of the user, may not be null
	 * @param email
	 * @param password
	 * @param assignedRoles
	 * @param pathPrefix
	 * @throws java.security.NoSuchAlgorithmException
	 * @throws java.io.UnsupportedEncodingException
	 */
	public void storeUser(String name, String email, String password,
			List<String> assignedRoles, String pathPrefix);

	public void updateUser(String name, String email, String password,
			List<String> assignedRoles, String pathPrefix);

	public boolean nameExists(String name);

	public boolean emailExists(String email);

	/**
	 *
	 * @param email
	 * @return
	 *		null if the email is null or the email does not exist in the
	 *		storage, otherwise returns the name of the user who owns the email
	 * @throws org.apache.clerezza.platform.usermanager.UserHasNoNameException
	 */
	public String getNameByEmail(String email) throws UserHasNoNameException;

	public NonLiteral getUserByName(String name);

	public Iterator<NonLiteral> getUsers();

	public void deleteUser(String name);

	public void assignPermissionsToUser(String name,
			List<String> permissionEntries);

	public Iterator<NonLiteral> getPermissionsOfUser(NonLiteral user);

	public void deletePermissionsOfUser(String name,
			List<String> permissionEntries);

	public void deleteAllPermissionsOfUser(String name);
}
