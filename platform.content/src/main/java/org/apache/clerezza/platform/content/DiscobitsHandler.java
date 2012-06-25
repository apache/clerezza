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
package org.apache.clerezza.platform.content;

import javax.ws.rs.core.MediaType;

import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.UriRef;

/**
 * Provides utility methods to create, retrieve and remove binary contents.
 * Binary contents are modeled as InfoDiscoBit from the discobit ontology at
 * http://discobits.org/ontology
 *
 * @author rbn
 */
public interface DiscobitsHandler {

	/**
	 * Creates an InfoDiscoBit
	 * 
	 * @param infoDiscoBitUri
	 * @param mediaType
	 * @param data
	 */
	public abstract void put(UriRef infoDiscoBitUri, MediaType mediaType,
			byte[] data);

	/**
	 * Removes InfoDiscoBits (aka binary contents), other DiscoBits and
	 * the context of the specified node. If it is in a hierarchy then it
	 * will be removed also form its container.
	 * 
	 * @param node
	 */
	public abstract void remove(NonLiteral node);

	/**
	 * 
	 * @param uriRef
	 * @return the media type of the InfoDiscoBit with the specified URI or null
	 *         if no MediaType for that URI is known
	 */
	public MediaType getMediaType(UriRef uriRef);

	/**
	 * 
	 * @param uriRef
	 * @return a byte[] with the data of the InfoDiscoBit with the specified URI
	 *         or null if no data for that URI is known
	 */
	public byte[] getData(UriRef uriRef);

}