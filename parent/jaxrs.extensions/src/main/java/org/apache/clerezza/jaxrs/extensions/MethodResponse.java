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
package org.apache.clerezza.jaxrs.extensions;

import java.lang.reflect.Method;
import javax.ws.rs.core.Response;

/**
 * This class extends {@link javax.ws.rs.core.Response} to contain a reference to
 * the method generating this response (or an object wrapped therein) so that an
 * implementation can base selection of the <code>MessageBodyWriter</code> 
 * basing on annotations/declared return type of this method.
 *
 * @author rbn
 */
public abstract class MethodResponse extends Response {

	/**
	 * Return the method generating the response or the entity wrapped therein.
	 *
	 * @return the method
	 */
	public abstract Method getGeneratingMethod();
}
