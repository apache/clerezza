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
package org.apache.clerezza.rdf.web.core;

import org.apache.clerezza.permissiondescriptions.PermissionInfo;

import java.security.Permission;

/**
 * Permission to retrieve a backup copy of all graphs provided by TcManager
 *
 * @author reto
 */
@PermissionInfo(value="Backup Permission", description="Permission " +
    "to retrieve a backup of all Triple Collections")
public class BackupPermission extends Permission {


    public BackupPermission() {
        super("Graph Management permission");
    }
    /**
     *
     * @param target ignored
     * @param action ignored
     */
    public BackupPermission(String target, String actions) {
        super("Graph Management permission");
    }

    @Override
    public boolean implies(Permission permission) {
        return equals(permission);
    }

    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }

    @Override
    public int hashCode() {
        return 77988;
    }

    @Override
    public String getActions() {
        return "";
    }

}
