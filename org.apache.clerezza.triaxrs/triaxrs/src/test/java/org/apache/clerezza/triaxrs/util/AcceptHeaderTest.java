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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.apache.clerezza.triaxrs.delegate.RuntimeDelegateImpl;
import org.apache.clerezza.triaxrs.util.AcceptHeader;


public class AcceptHeaderTest {
	@Before
	public void setUp() {
		RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
	}
	@Test
	public void gettingQualityWithWildCard() {
		List<String> entryStrings = new ArrayList<String>();
		entryStrings.add("*/*;q=.1");
		entryStrings.add("image/png;q=1");
		entryStrings.add("image/jpeg");
		entryStrings.add("image/*;q=.3");
		entryStrings.add("text/*;q=.3");
		AcceptHeader acceptHeader = new AcceptHeader(entryStrings);
		Assert.assertEquals(1000, acceptHeader.getAcceptedQuality(MediaType.valueOf("image/jpeg")));
		Assert.assertEquals(1000, acceptHeader.getAcceptedQuality(MediaType.valueOf("image/png")));
		Assert.assertEquals(300, acceptHeader.getAcceptedQuality(MediaType.valueOf("image/x-foo")));
		Assert.assertEquals(300, acceptHeader.getAcceptedQuality(MediaType.valueOf("text/plain")));
		Assert.assertEquals(100, acceptHeader.getAcceptedQuality(MediaType.valueOf("application/pdf")));
	}
	@Test
	public void gettingQualityWithoutWildCard() {
		List<String> entryStrings = new ArrayList<String>();
		entryStrings.add("image/png;q=1");
		entryStrings.add("image/jpeg");
		entryStrings.add("image/*;q=.3");
		entryStrings.add("text/*;q=.3");
		AcceptHeader acceptHeader = new AcceptHeader(entryStrings);
		Assert.assertEquals(1000, acceptHeader.getAcceptedQuality(MediaType.valueOf("image/jpeg")));
		Assert.assertEquals(1000, acceptHeader.getAcceptedQuality(MediaType.valueOf("image/png")));
		Assert.assertEquals(300, acceptHeader.getAcceptedQuality(MediaType.valueOf("image/x-foo")));
		Assert.assertEquals(300, acceptHeader.getAcceptedQuality(MediaType.valueOf("text/plain")));
		Assert.assertEquals(0, acceptHeader.getAcceptedQuality(MediaType.valueOf("application/pdf")));
	}
}
