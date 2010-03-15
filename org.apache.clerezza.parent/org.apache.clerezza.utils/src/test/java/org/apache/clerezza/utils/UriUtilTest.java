/*
 *  Copyright 2010 mir.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.clerezza.utils;

import junit.framework.Assert;
import org.junit.Test;

/**
 *
 * @author mir
 */
public class UriUtilTest {

	@Test
	public void encodePartlyEncodedPath() throws UriException {
		Assert.assertEquals("!@%23$%25%5E&*()%7B%7D+", UriUtil.encodePartlyEncodedPath("!@#$%^&*(){}+", "UTF-8"));
		Assert.assertEquals("!@%23$%25%5E&*()%7B%7D+", UriUtil.encodePartlyEncodedPath("!@%23$%25%5E&*()%7B%7D+", "UTF-8"));
	}
}
