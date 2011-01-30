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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Providers;

import org.junit.Test;
import org.apache.clerezza.triaxrs.providers.ProvidersImpl;

public class TestProviders {

	public static class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

		@Override
		public Response toResponse(RuntimeException arg0) {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}
	
	public static class SecurityExceptionMapper implements ExceptionMapper<SecurityException> {

		@Override
		public Response toResponse(SecurityException arg0) {
			throw new UnsupportedOperationException("Not supported yet.");
		}
	}

	/**
	 * Returns mapper for exception and a direct subclass
	 */
	@Test
	public void getExceptionMapper() {
		Class[] mapperClasses = {RuntimeExceptionMapper.class};
		Providers providers = new ProvidersImpl(mapperClasses);
		assertNull(providers.getExceptionMapper(IOException.class));
		assertEquals(RuntimeExceptionMapper.class, providers.getExceptionMapper(
				RuntimeException.class).getClass());
		assertEquals(RuntimeExceptionMapper.class, providers.getExceptionMapper(
				SecurityException.class).getClass());
	}
	/**
	 * Testing with a mapper for SecurityException
	 */
	@Test
	public void getExceptionMapper2() {
		Class[] mapperClasses = {RuntimeExceptionMapper.class, SecurityExceptionMapper.class};
		Providers providers = new ProvidersImpl(mapperClasses);
		assertNull(providers.getExceptionMapper(IOException.class));
		assertEquals(RuntimeExceptionMapper.class, providers.getExceptionMapper(
				RuntimeException.class).getClass());
		assertEquals(RuntimeExceptionMapper.class, providers.getExceptionMapper(
				UnsupportedOperationException.class).getClass());
		assertEquals(SecurityExceptionMapper.class, providers.getExceptionMapper(
				SecurityException.class).getClass());
	}
}
