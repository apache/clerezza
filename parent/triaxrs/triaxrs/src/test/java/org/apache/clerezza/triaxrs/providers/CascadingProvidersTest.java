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
package org.apache.clerezza.triaxrs.providers;

import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.RuntimeDelegate;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.clerezza.triaxrs.delegate.RuntimeDelegateImpl;
import org.apache.clerezza.triaxrs.providers.CascadingProviders;

import static org.junit.Assert.*;

/**
 *
 * @author hasan
 */
public class CascadingProvidersTest {

	@BeforeClass
	public static void setUp() {
		RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
	}

	/**
	 * Test of getMessageBodyReader method of class CascadingProviders.
	 */
	@Test
	public void testGetMessageBodyReader() {
		CascadingProviders cascadingProviders = new CascadingProviders();
		MessageBodyReader<String> mbr1 = new MessageBodyReaderImpl("mbr1");
		MessageBodyReader<String> mbr2 = new MessageBodyReaderImpl("mbr2");
		MessageBodyReader<String> mbr3 = new MessageBodyReaderImpl("mbr3");
		cascadingProviders.addInstance(mbr1, "foo");
		cascadingProviders.addInstance(mbr2, "foo/bar");
		cascadingProviders.addInstance(mbr3, "");
		assertSame(mbr1, getMbr("foo/test", cascadingProviders));
		assertSame(mbr1, getMbr("foo/", cascadingProviders));
		assertSame(mbr3, getMbr("bar", cascadingProviders));
		assertSame(mbr3, getMbr("/", cascadingProviders));
		assertSame(mbr2, getMbr("foo/bar/test", cascadingProviders));
		assertSame(mbr2, getMbr("foo/bar/", cascadingProviders));
	}

	private MessageBodyReader getMbr(String path, CascadingProviders cascadingProviders) {
		return cascadingProviders.getMessageBodyReader(String.class, String.class, null,
				new MediaType("text", "plain"), path);
	}

	private static class MessageBodyReaderImpl implements MessageBodyReader<String> {
		private String name;

		private MessageBodyReaderImpl(String string) {
			name = string;
		}

		@Override
		public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
			return true;
		}

		@Override
		public String readFrom(Class<String> arg0, Type arg1, Annotation[] arg2, MediaType arg3, MultivaluedMap<String, String> arg4, InputStream arg5) throws IOException, WebApplicationException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		@Override
		public String toString() {
			return name;
		}
	}
}