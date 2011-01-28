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
package org.apache.clerezza.triaxrs.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

import junit.framework.Assert;
import org.apache.clerezza.triaxrs.headerDelegate.MediaTypeHeaderDelegate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MediaTypeComparatorTest {
	
	@Before
	public void setUp() {
		RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
	}
	
	@After
	public void tearDown() {
		RuntimeDelegate.setInstance(new org.apache.clerezza.triaxrs.delegate.RuntimeDelegateImpl());
	}

	@Test
	public void apiTest() {

		MediaType type1 = new MediaType("image", "jpeg");
		Assert.assertTrue(type1.isCompatible(type1));
		Assert.assertTrue(MediaType.WILDCARD_TYPE.isCompatible(
				MediaType.WILDCARD_TYPE));
		Assert.assertTrue(MediaType.WILDCARD_TYPE.isCompatible(type1));
	//Assert.assertFalse(type1.isCompatible(MediaType.WILDCARD_TYPE));
	}

	@Test
	public void testInSet() {
		SortedSet<MediaType> set = new TreeSet<MediaType>(
				new MediaTypeComparator());
		Map<String, String> attrib = new HashMap<String, String>();
		attrib.put("q", ".4");
		MediaType type1 = new MediaType("image", "jpeg", attrib);
		MediaType type2 = new MediaType("image", "png");
		MediaType type3 = new MediaType("image", "*");
		set.add(type1);
		set.add(type2);
		set.add(type3);
		Iterator<MediaType> iter = set.iterator();
		Assert.assertEquals(type2, iter.next());
		Assert.assertEquals(type1, iter.next());
		Assert.assertEquals(type3, iter.next());
	}

	private static class RuntimeDelegateImpl extends RuntimeDelegate {

		public RuntimeDelegateImpl() {
		}

		@Override
		public UriBuilder createUriBuilder() {
			throw new UnsupportedOperationException("not supported in test");
		}

		@Override
		public ResponseBuilder createResponseBuilder() {
			throw new UnsupportedOperationException("not supported in test");
		}

		@Override
		public VariantListBuilder createVariantListBuilder() {
			throw new UnsupportedOperationException("not supported in test");
		}

		@Override
		public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> clazz) {
			if (!clazz.equals(MediaType.class)) {
				throw new UnsupportedOperationException("not supported in test");
			}
			return (HeaderDelegate<T>) new MediaTypeHeaderDelegate();
		}

		@Override
		public <T> T createEndpoint(Application arg0,
				Class<T> arg1) throws IllegalArgumentException, UnsupportedOperationException {
			throw new UnsupportedOperationException("not supported in test");
		}
	}
}
