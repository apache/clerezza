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
package org.apache.clerezza.rdf.core.access;

import org.apache.clerezza.rdf.core.UriRef;

/**
 * is thrown on an attempt to delete an entity with a provider that
 * supports the delete operation when the specified entity cannot be deleted
 *
 * @author reto
 */
public class EntityUndeletableException extends RuntimeException {
    private UriRef entityName;

    /**
     * creates an exception indicating that the entity with the specified name
     * cannot be deleted
     * 
     * @param entityName the name of the entity which is undeletable
     */
    public EntityUndeletableException(UriRef entityName) {
        super("This entity is undeletable: "+entityName);
        this.entityName = entityName;
    }

    /**
     * 
     * @return the name of the entity which is undeletable
     */
    public UriRef getEntityName() {
        return entityName;
    }
}
