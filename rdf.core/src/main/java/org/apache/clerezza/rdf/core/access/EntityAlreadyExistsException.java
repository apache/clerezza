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
 * is thrown on an attempt to create an entity with a name which already exists
 *
 * @author hasan
 */
public class EntityAlreadyExistsException extends RuntimeException {

	private UriRef entityName;

	/**
	 * creates an exception indicating that an entity with the specified name
	 * already exists.
	 * 
	 * @param entityName the name of the entity which already exists
	 */
	public EntityAlreadyExistsException(UriRef entityName) {
		super("An entity with this name already exists: "+entityName);
		this.entityName = entityName;
	}

	/**
	 * 
	 * @return the name of the entity which already exists
	 */
	public UriRef getEntityName() {
		return entityName;
	}
}
