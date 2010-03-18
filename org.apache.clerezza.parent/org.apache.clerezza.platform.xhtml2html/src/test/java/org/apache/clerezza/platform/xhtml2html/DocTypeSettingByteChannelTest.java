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

public class DocTypeSettingByteChannelTest  {

	final Charset UTF8 = Charset.forName("UTF-8");
	@Test
	public void simpleTest() throws Exception {
		final String someHtml = "<html>\n" +
				"<body>\n" +
				"hello" +
				"</body>\n" +
				"</html>";
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final WritableByteChannel channel = createDocTypeFilteringByteChannel(baos);
		channel.write(ByteBuffer.wrap(someHtml.getBytes(UTF8)));
		final String resultString = new String(baos.toByteArray(), UTF8);
		Assert.assertTrue(resultString.startsWith("<!DOCTYPE"));
		
	}

	/**
	 * XML declaration allowed only at the start of the document
	 */
	@Test
	public void removeXmlDeclarationTest() throws Exception {
		final String someHtml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>" +
				"<html>\n" +
				"<body>\n" +
				"hello" +
				"</body>\n" +
				"</html>";
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final WritableByteChannel channel = createDocTypeFilteringByteChannel(baos);
		int bytesWritten = channel.write(ByteBuffer.wrap(someHtml.substring(0, 20).getBytes(UTF8)));
		bytesWritten += channel.write(ByteBuffer.wrap(someHtml.substring(20).getBytes(UTF8)));
		final String resultString = new String(baos.toByteArray(), UTF8);
		Assert.assertEquals(someHtml.length(), bytesWritten);
		Assert.assertTrue(resultString.contains("<!DOCTYPE"));
		/* The test fails iff the ?xml is at another position than 0, not
		 * if its removed*/
		if (resultString.contains("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")) {
			Assert.assertTrue(resultString.startsWith("<?xml"));
		}
	}

	@Test
	public void removeXmlDeclarationAndNotAddedTwiceTest() throws Exception {
		final String someHtml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
				"<!DOCTYPE something>\n" +
				"<html>\n" +
				"<body>\n" +
				"hello" +
				"</body>\n" +
				"</html>";
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final WritableByteChannel channel = createDocTypeFilteringByteChannel(baos);
		channel.write(ByteBuffer.wrap(someHtml.getBytes(UTF8)));
		final String resultString = new String(baos.toByteArray(), UTF8);
		Assert.assertTrue(resultString.contains("<!DOCTYPE"));
		/* The test fails iff the ?xml is at another position than 0, not
		 * if its removed*/
		if (resultString.contains("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")) {
			Assert.assertTrue(resultString.startsWith("<?xml"));
		}
		Assert.assertTrue(resultString.startsWith("\n<!DOCTYPE something"));
		Assert.assertFalse(resultString.substring(8).contains("<!DOCTYPE"));
	}

	@Test
	public void notAddedTwiceTest() throws Exception {
		final String someHtml = "<!DOCTYPE something>\n" +
				"<html>\n" +
				"<body>\n" +
				"hello" +
				"</body>\n" +
				"</html>";
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final WritableByteChannel channel = createDocTypeFilteringByteChannel(baos);
		channel.write(ByteBuffer.wrap(someHtml.getBytes(UTF8)));
		final String resultString = new String(baos.toByteArray(), UTF8);
		Assert.assertTrue(resultString.startsWith("<!DOCTYPE something"));
		Assert.assertFalse(resultString.substring(8).contains("<!DOCTYPE"));
	}

	@Test
	public void notAddedTwiceSplittedTest() throws Exception {
		final String someHtml = "<!DOCTYPE something>\n" +
				"<html>\n" +
				"<body>\n" +
				"hello" +
				"</body>\n" +
				"</html>";
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final WritableByteChannel channel = createDocTypeFilteringByteChannel(baos);
		final byte[] bytes = someHtml.getBytes(UTF8);
		for (int i = 0; i < bytes.length; i++) {
			ByteBuffer buf = ByteBuffer.allocate(1);
			buf.put(bytes[i]);
			buf.rewind();
			channel.write(buf);
		}
		final String resultString = new String(baos.toByteArray(), UTF8);
		Assert.assertTrue(resultString.startsWith("<!DOCTYPE something"));
		Assert.assertFalse(resultString.substring(8).contains("<!DOCTYPE"));
	}

	@Test
	public void notAddDoctypeIfHtmlNotComplete() throws Exception {
		final String someHtmlSnippet =	"<body>\n" +
				"hello" +
				"</body>\n";
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final WritableByteChannel channel = createDocTypeFilteringByteChannel(baos);
		channel.write(ByteBuffer.wrap(someHtmlSnippet.getBytes(UTF8)));
		final String resultString = new String(baos.toByteArray(), UTF8);
		Assert.assertEquals(someHtmlSnippet, resultString);
	}

	@Test
	public void removeXmlDeclarationAndDotnotAddDoctypeIfHtmlNotComplete() throws Exception {
		final String someHtmlSnippet =	"<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n\n\n\n\n<body>\n" +
				"hello" +
				"</body>\n";
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final WritableByteChannel channel = createDocTypeFilteringByteChannel(baos);
		channel.write(ByteBuffer.wrap(someHtmlSnippet.getBytes(UTF8)));
		final String resultString = new String(baos.toByteArray(), UTF8);
		/* The test fails iff the ?xml is at another position than 0, not
		 * if its removed*/
		if (resultString.contains("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")) {
			Assert.assertTrue(resultString.startsWith("<?xml"));
		}
		Assert.assertFalse(resultString.contains("<!DOCTYPE"));
	}

	private WritableByteChannel createDocTypeFilteringByteChannel(ByteArrayOutputStream baos) {
		return new DocTypeFilteringByteChannel(Channels.newChannel(baos),
				new ResponseStatusInfo() {

			@Override
			public boolean convertXhtml2Html() {
				return true;
			}
		});
	}

}
