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
package org.apache.clerezza.triaxrs.blackbox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.mock.RequestImpl;
import org.apache.clerezza.triaxrs.mock.RequestURIImpl;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.apache.clerezza.triaxrs.testutils.HandlerCreator;
import org.junit.Ignore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wymiwyg.wrhapi.HeaderName;
import org.wymiwyg.wrhapi.MessageBody;
import org.wymiwyg.wrhapi.Method;
import org.wymiwyg.wrhapi.util.MessageBody2Read;


/**
 * 
 * @author ali
 * 
 */
public class SourceProviderTest {
	private JaxRsHandler handler = HandlerCreator.getHandler(MyResource.class);
	private RequestURIImpl uri = new RequestURIImpl();
	private TransformerFactory transformerFac = TransformerFactory.newInstance();
	private Transformer transformer;
	private RequestImpl request;
	private ResponseImpl response;
	private Result output;
	private StringWriter stringWriter;
	private static final String NL = System.getProperty("line.separator");
	private static final String xmlString =
	"<PHONEBOOK>" + NL +
	"  <PERSON>" + NL +
	"    <NAME>Joe Wang</NAME>" + NL +
	"    <EMAIL>joe@yourserver.com</EMAIL>" + NL +
	"    <TELEPHONE>202-999-9999</TELEPHONE>" + NL +
	"    <WEB>www.java2s.com</WEB>" + NL +
	"  </PERSON>" + NL +
	"</PHONEBOOK>";
	
	@Path("/")
	public static class MyResource {
		static Source streamObject;
		static Source domObject;
		static Source saxObject;

		@GET
		@Path("stream")
		public Object reciveStreamSource(StreamSource source) throws Exception {
			streamObject = source;
			return streamObject;
		}

		@GET
		@Path("sax")
		public Object reciveSAXSource(SAXSource source) {
			saxObject = source;
			return saxObject;
		}

		@GET
		@Path("dom")
		public Object reciveXML(DOMSource source) {
			domObject = source;
			return domObject;
		}

		@GET
		public Object receive(){
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try {
				Document doc = dbf.newDocumentBuilder().newDocument();
				// adding root element
				Element root = doc.createElement("sparql");
				root.setAttribute("xmlns", "http://www.w3.org/2005/sparql-results#");
				doc.appendChild(root);
				Element head = doc.createElement("head");
				root.appendChild(head);
				Element varElement = doc.createElement("variable");
				varElement.setAttribute("name", "var1");
				head.appendChild(varElement);
				Element result = doc.createElement("result");
				root.appendChild(result);
				Element bindingElement = doc.createElement("binding");
				bindingElement.setAttribute("name", "name1");
				Element value = doc.createElement("uri");
				value.appendChild(doc.createTextNode("textNode"));
				bindingElement.appendChild(value);
				result.appendChild(bindingElement);
				
				DOMSource source = new DOMSource(doc);
				return source;

			} catch (ParserConfigurationException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private void setup(String path) throws Exception{
		transformer = transformerFac.newTransformer();
		//new request and response
		request = new RequestImpl();
		response = new ResponseImpl();
		
		uri.setPath(path);
		String[] headervalues = new String[2];
		//headervalues[0] = "text/html";
		headervalues[1] = "application/xhtml+xml";
		//headervalues[2] = "application/xml;q=0.9";
		headervalues[0] = "*/*";
		
		request.setHeader(HeaderName.CONTENT_TYPE, headervalues);

		MessageBody messageBody = new MessageBody2Read() {

			@Override
			public ReadableByteChannel read() throws IOException {
				return Channels.newChannel(new ByteArrayInputStream(xmlString.getBytes()));
			}
		};
		request.setMessageBody(messageBody);
		request.setRequestURI(uri);
		request.setMethod(Method.GET);
		stringWriter = new StringWriter();
		output = new StreamResult(stringWriter);
	}

	private String getContentType() {
		Map<HeaderName,String[]> headers = response.getHeaders();
		String[] contentType = headers.get(HeaderName.CONTENT_TYPE);
		return contentType[0];
	}

	@Test
	public void readStreamSource() throws Exception {
		setup("/stream");		
		handler.handle(request, response);
		Assert.assertNotNull(MyResource.streamObject);				
		transformer.transform(MyResource.streamObject, output);		
		String result = stringWriter.toString();
		Assert.assertTrue(result.endsWith(xmlString));
		
	}
	
	@Test
	public void readSAXSource() throws Exception {
		setup("/sax");
		handler.handle(request, response);
		Assert.assertNotNull(MyResource.saxObject != null);
		transformer.transform(MyResource.saxObject, output);
		String result = stringWriter.toString();
		Assert.assertTrue(result.endsWith(xmlString));
		
	}
	
	@Test
	public void readDOMSource() throws Exception {
		setup("/dom");
		handler.handle(request, response);
		Assert.assertNotNull(MyResource.domObject != null);
		transformer.transform(MyResource.domObject, output);
		String result = stringWriter.toString();
		Assert.assertTrue(result.endsWith(xmlString));
	}
	
	@Test
	public void writeStreamSource() throws Exception {
		setup("/stream");
		handler.handle(request, response);
		response.consumeBody();
		String contentType = getContentType();
		Assert.assertTrue(contentType.equals("application/xml"));
		String result = new String(response.getBodyBytes());
		Assert.assertTrue(result.endsWith(xmlString));
	}
	
	@Test
	public void writeSAXSource() throws Exception {
		setup("/sax");
		handler.handle(request, response);
		response.consumeBody();
		String contentType = getContentType();
		Assert.assertTrue(contentType.equals("application/xml"));
		String result = new String(response.getBodyBytes());
		Assert.assertTrue(result.endsWith(xmlString));
	}
	
	@Test
	public void writeDOMSource() throws Exception {
		setup("/dom");
		handler.handle(request, response);
		response.consumeBody();
		String contentType = getContentType();
		Assert.assertTrue(contentType.equals("application/xml"));
		String result = new String(response.getBodyBytes());
		Assert.assertTrue(result.endsWith(xmlString));
	}

	@Test
	public void writeDomSource() throws Exception {
		setup("/");
		handler.handle(request, response);
		response.consumeBody();
		String contentType = getContentType();
		Assert.assertTrue(contentType.equals("application/xml"));
	}
}
