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
import org.slf4j.scala.Logging

//note: file has lower class name, as it contains many classes


/**
 * superclass for the permission classes, to avoid a lot of boilerplate code
 *
 * @author bblfish, reto
 */
abstract
class AbstractPermission(val accountName: String, val actions: String ="")
  extends Permission(accountName) with Logging  {

  if (actions != "") {
    throw new RuntimeException(getClass.getName+": actions must be an empty String "+
      "(second argument only in constructor for supporting building from canonical form")
  }

  def getActions: String = actions

  /**
   * A subclass implies another permission if and only if they are equals
   */
   override
  def implies(permission: Permission): Boolean = {
    logger.debug("checking for "+permission+" is implied by "+ this)
    var result: Boolean = equals(permission)
    return result
  }

  override
  def equals(other: Any): Boolean =
      other match {
      case that:  AbstractPermission  =>  
        (that eq this ) || ((this.getClass == that.getClass) && accountName == that.accountName )
      case _ => false
      }

  /**
   * For the hashCode the class and the accountName is considered
   */
  override
  def hashCode: Int = {
    return  getClass.hashCode + (if (accountName != null) accountName.hashCode else 0)
  }
}

/**
 * Permission to change the password
 * @author ali
 *
 */
class ChangePasswordPermission(accountName: String, actions: String ="")
  extends AbstractPermission(accountName, actions) {

}

/**
 * Permission to access the account control panel
 *
 * @author ali
 *
 */
class AccountControlPanelAppPermission(accountName: String, actions: String ="")
  extends AbstractPermission(accountName)  {


}

/**
 * Permission for user to have own bundles
 *
 * @author mir
 *
 */
class UserBundlePermission( accountName: String, actions: String ="")
  extends AbstractPermission(accountName)  {


}