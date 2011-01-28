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
package org.apache.clerezza.jaxrs.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.clerezza.jaxrs.utils.form.MultiPartFormMessageBodyReader;
import org.apache.clerezza.triaxrs.delegate.RuntimeDelegateImpl;

/**
 * @author mir
 * 
 */
public class MultiPartFormMBRTest {
	
	private static String paramName1 = "field1";
	private static String paramName2 = "field2";
	private static String paramName3 = "userfile";
	private static String field1Value = "foo";
	private static String field2Value = "bar";
	private static String binaryData = "010111100\n100110101\n001100101";
	private static String binaryMimeType = "application/octet-stream";
	private static String binaryFilename = "example.bin";
	
	@Test
	public void testMultiPartForm() throws Exception {
		
		RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
		StringBuilder sb = new StringBuilder();
		sb.append("--AaB03x\r\n");
		sb.append("Content-Disposition: form-data; name=\""+ paramName1 +"\"\r\n");
		sb.append("\r\n");
		sb.append(field1Value + "\r\n");
		sb.append("--AaB03x\r\n");
		sb.append("Content-Disposition: form-data; name=\""+ paramName2 +"\"\r\n");
		sb.append("\r\n");
		sb.append(field2Value +"\r\n");
		sb.append("--AaB03x\r\n");
		sb.append("Content-Disposition: form-data; name=\"userfile\"; filename=\""+ binaryFilename +"\"\r\n");
		sb.append("Content-Type: "+ binaryMimeType +"\r\n");
		sb.append("Content-Transfer-Encoding: binary\r\n");
		sb.append("\r\n");
		sb.append(binaryData + "\r\n");
		sb.append("--AaB03x--\r\n");

		final byte[] message = sb.toString().getBytes();
		InputStream entityStream = new ByteArrayInputStream(message);
		
		MultiPartFormMessageBodyReader multiPartFormMBR = new MultiPartFormMessageBodyReader();
		MediaType mediaType = MediaType.valueOf("multipart/form-data; boundary=AaB03x");
		
		MultiPartBody multiPartBody = multiPartFormMBR.readFrom(null, null, null, mediaType, null, entityStream);
		
		String[] paramNames = multiPartBody.getParameterNames();
		String[] expectedParamNames = {paramName1, paramName2, paramName3};
		System.out.println(Arrays.toString(paramNames));
		Assert.assertArrayEquals(expectedParamNames, paramNames);
		Assert.assertEquals(field1Value, multiPartBody.getParameteValues(paramName1)[0].toString());
		Assert.assertEquals(field2Value, multiPartBody.getParameteValues(paramName2)[0].toString());
		
		String[] fileParamNames = multiPartBody.getFileParameterNames();			
		String[] expectedFileParamNames = {paramName3};
		Assert.assertArrayEquals(expectedFileParamNames, fileParamNames);
		FormFile[] formFiles = multiPartBody.getFormFileParameterValues(paramName3);
		Assert.assertArrayEquals(binaryData.getBytes(), formFiles[0].getContent());
		Assert.assertEquals(MediaType.valueOf(binaryMimeType), formFiles[0].getMediaType());
		Assert.assertEquals(binaryFilename, formFiles[0].getFileName());
	}
}
