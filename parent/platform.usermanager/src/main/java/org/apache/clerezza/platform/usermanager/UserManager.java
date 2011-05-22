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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.utils.GraphNode;

import javax.security.auth.Subject;

/**
 * An implementation of this interface provides methods to manage data about
 * users and their roles.
 * Data managed are needed for authentication and for setting permissions.
 * Those data include user names, email addresses, passwords, roles, permissions,
 * etc.
 * A user is uniquely identified by a user name.
 * Each user has an email address and an email address can only belong to a user.
 *
 * @author hasan, tio
 */
public interface UserManager {

	/**
	 *
	 * @param title
	 *		the title of the role, may not be null
	 */
	public void storeRole(String title);

	/**
	 * Checks if a role with this title exists
	 *
	 * @param title specifies the title of the role
	 *
	 * @return true if the role exists otherwise false
	 */
	public boolean roleExists(String title);

	/**
	 *
	 * @param title
	 * @return NonLiteral which is either a BNode or a UriRef
	 */
	public NonLiteral getRoleByTitle(String title);

	/**
	 *
	 * @return Iterator defining all roles, except base roles
	 */
	public Iterator<NonLiteral> getRoles();

	/**
	 *
	 * @param user
	 *			the user is either a BNode or a UriRef
	 *
	 * @return Iterator defining all the Roles the specified user owns
	 */
	public Iterator<NonLiteral> getRolesOfUser(NonLiteral user);

	/**
	 *
	 * @param title
	 *		the title of the role to be deleted, may not be null
	 */
	public void deleteRole(String title);

	/**
	 * Assigns a permission to a role
	 *
	 * @param title specifies the title of the role, may not be null
	 * @param permissionEntries specifies a list of permissions
	 */
	public void assignPermissionsToRole(String title,
			List<String> permissionEntries);

	/**
	 *
	 * @param role
	 *			the role is either a BNode or an UriRef
	 *
	 * @return Iterator defining all permissions of a role
	 */
	public Iterator<NonLiteral> getPermissionsOfRole(NonLiteral role);

	/**
	 *  Deletes the defined permissions of the role
	 *
	 * @param title specifies the title of the role, may not be null
	 * @param permissionEntries
	 */
	public void deletePermissionsOfRole(String title,
			List<String> permissionEntries);
	/**
	 * Deletes all permission of a role
	 *
	 * @param title specifies the title of the role, may not be null
	 */
	public void deleteAllPermissionsOfRole(String title);

	/**
	 *
	 * @param name
	 *		the username of the user, may not be null
	 * @param email
	 * @param password
	 * @param assignedRoles
	 * @param pathPrefix
	 * @throws java.security.NoSuchAlgorithmException
	 * @throws java.io.UnsupportedEncodingException
	 */
	public void storeUser(String name, String email, String password,
			List<String> assignedRoles, String pathPrefix);

	/**
	 * Updates the user with the specified userName
	 * 
	 * @param name, may not be null
	 * @param email the email address (note that this is not the mailto-uri)
	 * @param password
	 * @param assignedRoles
	 * @param pathPrefix
	 */
	public void updateUser(String name, String email, String password,
			Collection<String> assignedRoles, String pathPrefix);

	/**
	 *	Checks if the username exists
	 *
	 * @param name specifies the username, may not be null
	 * @return true if exists otherwise false
	 */
	public boolean nameExists(String name);

	/**
	 * Checks if thereis already an agent with that email address.
	 *
	 * @param email
	 * @return true if exists otherwise false
	 */
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

	/**
	 *
	 * @param name specifies the username of the user
	 * @return NonLiteral representing the user in the system Graph
	 */
	@Deprecated
	public NonLiteral getUserByName(String name);

	/**
	 * Returns the user with the specified name in an (editable) MGraph
	 * (i.e. a SimpleGraph but this is implementation specific) with the context
	 * of the user node, editing the graphnode('s Mgraph) doesn't cause any
	 * changes elsewhere. Returns null if the user does not exist.
	 * The caller of this method needs the permission to read the system graph,
	 * otherwise a AccessControlException will be thrown.
	 *
	 * @param name The username of the user
	 * @return GraphNode representing the user (WebID or blank node) with some context in a dedicated MGraph
	 */
	public GraphNode getUserGraphNode(Subject name);

	/**
	 * Returns the <code>GraphNode</code> pointing to the user with the
	 * specified name in the system graph. Returns null if the user does not
	 * exist.
	 *
	 * @param name The username of the user
	 * @return GraphNode representing the user in the system graph
	 */
	public GraphNode getUserInSystemGraph(String name);

	/**
	 * Returns the <code>GraphNode</code> pointing to the user with the
	 * specified name in the content graph. If the user does not exist in the
	 * content graph, but in the system graph, then the it is created with the
	 * PLATFORM.userName property copied from the system graph. Returns null if
	 * the user does not exist.
	 *
	 * @param name The username of the user
	 * @return GraphNode representing the user in the content graph
	 */
	public GraphNode getUserInContentGraph(String name);

	/**
	 * Returns all users.
	 *
	 * @return Iterator of users in the system Graph.
	 */
	public Iterator<NonLiteral> getUsers();

	/**
	 *
	 * @param name specifies the username of the user, may not be null
	 */
	public void deleteUser(String name);

	/**
	 *
	 * @param name specifies the username of the user, may not be null
	 * @param permissionEntries
	 */
	public void assignPermissionsToUser(String name,
			List<String> permissionEntries);
	/**
	 *
	 * @param user
	 *			the user is either a BNode or a UriRef
	 * @return  Iterator defining all permissions of the specified user
	 */
	public Iterator<NonLiteral> getPermissionsOfUser(NonLiteral user);

	/**
	 *
	 * @param name specifies the username of the user, may not be null
	 * @param permissionEntries
	 */
	public void deletePermissionsOfUser(String name,
			List<String> permissionEntries);

	/**
	 * Deletes all permission of a user
	 *
	 * @param name specifies the username of the user, may not be null
	 */
	public void deleteAllPermissionsOfUser(String name);
}
