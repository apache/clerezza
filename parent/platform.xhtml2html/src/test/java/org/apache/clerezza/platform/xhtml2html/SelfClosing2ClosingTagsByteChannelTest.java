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

package org.apache.clerezza.platform.xhtml2html;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import org.junit.Assert;
import org.junit.Test;

public class SelfClosing2ClosingTagsByteChannelTest  {

	final Charset UTF8 = Charset.forName("UTF-8");
	@Test
	public void simpleTest() throws Exception {
		final String someHtml = "<html>\n" +
				"<script/>\n" +
				"<test />\n" +
				"<foo id=\"bla\" />\n" +
				"<area/>\n" +
				"<base/>\n" +
				"<basefont/>\n" +
				"<br/>\n" +
				"<hr/>\n" +
				"<input/>\n" +
				"<img/>\n" +
				"<link/>\n" +
				"<meta/>\n" +
				"<body>\n" +
				"hello" +
				"</body>\n" +
				"</html>";
		final String expectedHtml = "<html>\n" +
				"<script></script>\n" +
				"<test ></test>\n" +
				"<foo id=\"bla\" ></foo>\n" +
				"<area/>\n" +
				"<base/>\n" +
				"<basefont/>\n" +
				"<br/>\n" +
				"<hr/>\n" +
				"<input/>\n" +
				"<img/>\n" +
				"<link/>\n" +
				"<meta/>\n" +
				"<body>\n" +
				"hello" +
				"</body>\n" +
				"</html>";
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final WritableByteChannel channel = new SelfClosing2ClosingTagsByteChannel(Channels.newChannel(baos),
				new ResponseStatusInfo() {

			@Override
			public boolean convertXhtml2Html() {
				return true;
			}

		});
		int bytesWritten = channel.write(ByteBuffer.wrap(someHtml.getBytes(UTF8)));
		Assert.assertEquals(someHtml.length(), bytesWritten);
		final String resultString = new String(baos.toByteArray(), UTF8);
		Assert.assertEquals(expectedHtml, resultString);
	}
}
