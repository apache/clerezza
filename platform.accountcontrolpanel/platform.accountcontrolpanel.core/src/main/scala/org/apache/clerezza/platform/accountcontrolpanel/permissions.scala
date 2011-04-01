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

package org.apache.clerezza.platform.accountcontrolpanel

import java.security.Permission
import org.slf4j.{Logger, LoggerFactory}

//note: file has lower class name, as it contains many classes

/**
 *
 *
 * @author bblfish
 */

object AbstractPermission {
	private  val logger: Logger = LoggerFactory.getLogger(classOf[AbstractPermission])
}

/**
 * superclass for the permission classes, to avoid a lot of boilerplate code
 *
 * @author bblfish
 */


abstract
class AbstractPermission(val accountName: String, val actions: String ="") extends Permission(accountName)  {
	import AbstractPermission.logger

	def getActions: String = actions

	/**
	 * this overriding this method, one should create a canEquals method as described in "Programming in Scala" Book
	 * by Martin Odersky, Lex Spoon, Bill Venners
	 */
	def canEqual(other: Any): Boolean

   override
	def implies(permission: Permission): Boolean = {
		logger.debug("checking for {} in {}", permission, this)
		var result: Boolean = equals(permission)
		logger.debug("result {}", result)
		return result
	}

	override
	def equals(other: Any): Boolean =
	    other match {
			case that:  AbstractPermission  =>  ( that eq this ) || ( that.canEqual(this) && accountName == that.accountName )
			case _ => false
	    }

  //todo: the hashes for same named account names of different types would be the same here
	override
	def hashCode: Int = {
		return  41 * (41 + (if (accountName != null) accountName.hashCode else 0))
	}
}

/**
 * Permission to change the password
 * @author ali
 *
 */
class ChangePasswordPermission(accountName: String, actions: String ="")
	extends AbstractPermission(accountName, actions) {

	def canEqual(other: Any): Boolean = other.isInstanceOf[ChangePasswordPermission]


}

/**
 * Permission to access the account control panel
 *
 * @author ali
 *
 */
class AccountControlPanelAppPermission(accountName: String, actions: String ="")
	extends AbstractPermission(accountName)  {

	def canEqual(other: Any): Boolean = other.isInstanceOf[AccountControlPanelAppPermission]

}

/**
 * Permission for user to have own bundles
 *
 * @author mir
 *
 */
class UserBundlePermission( accountName: String, actions: String ="")
	extends AbstractPermission(accountName)  {

	def canEqual(other: Any): Boolean = other.isInstanceOf[UserBundlePermission]

}