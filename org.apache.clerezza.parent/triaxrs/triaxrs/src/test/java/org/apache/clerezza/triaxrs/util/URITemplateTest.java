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

import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Assert;

import org.junit.Test;
import org.apache.clerezza.triaxrs.util.URITemplate;

public class URITemplateTest {
	@Test
	public void simpleTemplateMatching() {
		Assert.assertNull(new URITemplate("/foo/bar").match("/foo"));
		Assert.assertNotNull(new URITemplate("/foo").match("/foo/bar"));
		Assert.assertNotNull(new URITemplate("foo").match("/foo/bar"));
	}
	
	@Test
	public void simpleParameterizedeMatching() {
		URITemplate template1 = new URITemplate("widgets/{id}");
		Assert.assertEquals("Hello", template1.match(
				"widgets/Hello").getParameters().get("id"));
		URITemplate template2 = new URITemplate("widgets/{id}/{id2}");
		Assert.assertEquals("foo", template2.match(
				"widgets/foo/bar").getParameters().get("id"));
		Assert.assertEquals("foo", template2.match(
		"widgets/foo/bar/ignore").getParameters().get("id"));
		SortedSet<URITemplate> set = new TreeSet<URITemplate>();
		set.add(template1);
		set.add(template2);
		Assert.assertEquals(template2, set.first());
	}
	
}
