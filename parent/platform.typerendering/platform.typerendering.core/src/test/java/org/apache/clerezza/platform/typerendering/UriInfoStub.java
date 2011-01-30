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
package org.apache.clerezza.platform.typerendering;

import java.net.URI;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.triaxrs.util.MultivaluedMapImpl;

/**
 *
 * @author mir
 */
public class UriInfoStub implements UriInfo{

	@Override
	public String getPath() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public String getPath(boolean decode) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<PathSegment> getPathSegments() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<PathSegment> getPathSegments(boolean decode) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public URI getRequestUri() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public UriBuilder getRequestUriBuilder() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public URI getAbsolutePath() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public UriBuilder getAbsolutePathBuilder() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public URI getBaseUri() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public UriBuilder getBaseUriBuilder() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters(boolean decode) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters() {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String, String>();
		queryParams.add(GenericGraphNodeMBW.MODE, GenericMBWTest.TEST_MODE);
		return queryParams;
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<String> getMatchedURIs() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<String> getMatchedURIs(boolean decode) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public List<Object> getMatchedResources() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
