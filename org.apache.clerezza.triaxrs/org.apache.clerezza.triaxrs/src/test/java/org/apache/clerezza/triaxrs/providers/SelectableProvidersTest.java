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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.apache.clerezza.triaxrs.delegate.RuntimeDelegateImpl;
import org.apache.clerezza.triaxrs.providers.ProviderCriteria;
import org.apache.clerezza.triaxrs.providers.SelectableProviders;

public class SelectableProvidersTest {

	@Produces("text/html")
	public static class ExampleProvider {

	}
	
	@BeforeClass
	public static void setUp() {
		RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
	}

	@Test
	public void mediaType() {
		Set<ExampleProvider> providerSet = Collections
				.singleton(new ExampleProvider());
		ProviderCriteria<ExampleProvider> acceptTester = new ProviderCriteria<ExampleProvider>() {

			@Override
			public boolean isAcceptable(ExampleProvider producer, Class<?> c,
					Type t, Annotation[] as, MediaType m) {
				return true;
			}

			@Override
			public String[] getMediaTypeAnnotationValues(
					ExampleProvider producer) {
				Produces produces = producer.getClass().getAnnotation(
						Produces.class);
				if (produces == null) {
					return null;
				} else {
					return produces.value();
				}
			}

		};
		SelectableProviders<ExampleProvider> selectableProviders = new SelectableProviders<ExampleProvider>(
				providerSet, acceptTester);
		ExampleProvider result = selectableProviders.selectFor(null, null, null, new MediaType("text","html"));
		Assert.assertNotNull(result);
		result = selectableProviders.selectFor(null, null, null, new MediaType("application","xhtml+xml"));
		Assert.assertNull(result);
	}

}
