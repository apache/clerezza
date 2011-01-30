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

import junit.framework.Assert;

import org.junit.Test;

public class TemplateEncoderTest {
	@Test
	public void simpleEncoding() throws Exception {
		String s = "/föö/{blüü}/blä{euo}+{";
		String encoded = TemplateEncoder.encode(s, "utf-8");
		System.out.println(encoded);
		Assert.assertEquals("/f%C3%B6%C3%B6/{blüü}/bl%C3%A4{euo}+%7B", encoded);
	}

	@Test
	public void spaceEncoding() throws Exception {
		String s = "/foo bar";
		String encoded = TemplateEncoder.encode(s, "utf-8");
		Assert.assertEquals("/foo%20bar", encoded);
	}

	@Test
	public void containsAlreadyEncodedCharsTest() throws Exception {
		String s = "/++/%20 %20/äöü%GG";
		String encoded = TemplateEncoder.encode(s, "utf-8");
		Assert.assertEquals("/++/%20%20%20/%C3%A4%C3%B6%C3%BC%25GG", encoded);
	}
}
