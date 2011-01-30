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
package org.apache.clerezza.triaxrs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.clerezza.jaxrs.extensions.HttpRequest;
import org.apache.clerezza.triaxrs.util.AcceptHeader;
import org.wymiwyg.wrhapi.Request;

/**
 * This interface represents a request that can be handled 
 * by Triaxrs.
 * 
 * @author mir
 *
 */
public interface WebRequest extends javax.ws.rs.core.Request, HttpRequest {

	/**
	 * Returns the wrhapi request which was given the the 
	 * <code>JaxRsHandle.handle</code> method.
	 * 
	 * @return
	 */
	public Request getWrhapiRequest();

	/**
	 * Returns a map that contains the header key-value pairs
	 * of the headers.
	 * @return
	 */
	public MultivaluedMap<String, String> getHeaders();

	/**
	 * Returns the chosen accept header of the request
	 * @return the accept header
	 */
	public AcceptHeader getAcceptHeader();

	/**
	 * Returns the body of the request.
	 * 
	 * @param <T>
	 * @param type
	 * @param genericType
	 * @param transformationAnnotation
	 * @return
	 */
	public <T> T getBodyObject(Class<T> type, Type genericType,
			Annotation[] transformationAnnotation);

}