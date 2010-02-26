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
package org.apache.clerezza.web.fileserver.util;

import java.io.IOException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class TestMediaTypeGuesser {

	@Before
	public void setUp() {
		RuntimeDelegate.setInstance(new RuntimeDelegate() {

			@Override
			public UriBuilder createUriBuilder() {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			@Override
			public ResponseBuilder createResponseBuilder() {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			@Override
			public VariantListBuilder createVariantListBuilder() {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			@Override
			public <T> T createEndpoint(Application application, Class<T> endpointType) throws IllegalArgumentException, UnsupportedOperationException {
				throw new UnsupportedOperationException("Not supported yet.");
			}

			@Override
			public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
				if (type.equals(MediaType.class)) {
					return (HeaderDelegate<T>) new HeaderDelegate<MediaType>() {

						@Override
						public MediaType fromString(String value) throws IllegalArgumentException {
							String[] tokens = value.split("/");
							return new MediaType(tokens[0], tokens[1]);
						}

						@Override
						public String toString(MediaType value) {
							return value.getType()+"/"+value.getSubtype();
						}

					};
				}
				throw new UnsupportedOperationException("Not supported yet.");
			}
		});
	}

	@Test
	public void testExtension() throws IOException {
		MediaTypeGuesser guesser = MediaTypeGuesser.getInstance();
		Assert.assertEquals(MediaType.valueOf("text/plain"), guesser.getTypeForExtension("txt"));
	}
	
	@Test
	public void testFileName() throws IOException {
		MediaTypeGuesser guesser = MediaTypeGuesser.getInstance();
		Assert.assertEquals(MediaType.valueOf("text/plain"), guesser.guessTypeForName("foo/bla/bar.txt"));
	}
}
