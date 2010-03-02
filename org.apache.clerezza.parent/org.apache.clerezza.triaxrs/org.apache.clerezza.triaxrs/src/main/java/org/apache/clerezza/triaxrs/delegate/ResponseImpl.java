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
package org.apache.clerezza.triaxrs.delegate;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 *
 * @author reto
 */
class ResponseImpl extends Response {
	private int status;
	private MultivaluedMap<String, Object> headers;
	private Object entity;

	public ResponseImpl(int status,
			MultivaluedMap<String, Object> headers) {
		this.status = status;
		this.headers = headers;
	}
	
	public ResponseImpl(int status, Object entity,
			MultivaluedMap<String, Object> headers) {
		this.status = status;
		this.entity = entity;
		this.headers = headers;
	}

	@Override
	public Object getEntity() {
		return entity;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public MultivaluedMap<String, Object> getMetadata() {
		return headers;
	}

}
