/*******************************************************************************
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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *  
 *******************************************************************************/
package org.apache.clerezza.triaxrs.providers.provided;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.clerezza.triaxrs.util.MediaTypeUtils;
import org.apache.clerezza.triaxrs.util.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class SourceProvider implements MessageBodyWriter<Source> {

	private static TransformerFactory transformerFactory;
	private static DocumentBuilderFactory documentBuilderFactory;
	private static final Logger logger =
			LoggerFactory.getLogger(SourceProvider.class);

	static {
		transformerFactory = TransformerFactory.newInstance();
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
	}

	@Provider
	@Consumes({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD})
	@Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD})
	public static class StreamSourceProvider extends SourceProvider implements
			MessageBodyReader<Source> {

		@Override
		public boolean isReadable(Class<?> type,
				Type genericType,
				Annotation[] annotations,
				MediaType mediaType) {
			return (type.isAssignableFrom(StreamSource.class) && super.isReadable(mediaType));
		}

		@Override
		public StreamSource readFrom(Class<Source> type,
				Type genericType,
				Annotation[] annotations,
				MediaType mediaType,
				MultivaluedMap<String, String> httpHeaders,
				InputStream entityStream) throws IOException,
				WebApplicationException {
			return new StreamSource(entityStream);
		}
	}

	@Provider
	@Consumes({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD})
	@Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD})
	public static class SAXSourceProvider extends SourceProvider implements
			MessageBodyReader<SAXSource> {

		@Override
		public boolean isReadable(Class<?> type,
				Type genericType,
				Annotation[] annotations,
				MediaType mediaType) {
			return (SAXSource.class == type && super.isReadable(mediaType));
		}

		@Override
		public SAXSource readFrom(Class<SAXSource> type,
				Type genericType,
				Annotation[] annotations,
				MediaType mediaType,
				MultivaluedMap<String, String> httpHeaders,
				InputStream entityStream) throws IOException,
				WebApplicationException {
			return new SAXSource(new InputSource(entityStream));
		}
	}

	@Provider
	@Consumes({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD})
	@Produces({MediaType.TEXT_XML, MediaType.APPLICATION_XML, MediaType.WILDCARD})
	public static class DOMSourceProvider extends SourceProvider implements
			MessageBodyReader<DOMSource> {

		@Override
		public boolean isReadable(Class<?> type,
				Type genericType,
				Annotation[] annotations,
				MediaType mediaType) {
			return (DOMSource.class == type && super.isReadable(mediaType));
		}

		private void setupDocumentBuilderToFilterDTD(DocumentBuilder dbuilder) {
			/*
			 * You might think you could just do this to prevent entity expansion:
			 *    documentBuilderFactory.setExpandEntityReferences(false);
			 * In fact, you should not do that, because it will just increase the size
			 * of your DOMSource.  We want to actively reject XML when a DTD is present, so...
			 */
			dbuilder.setEntityResolver(new EntityResolver() {

				@Override
				public InputSource resolveEntity(String name, String baseURI)
						throws SAXException, IOException {
					// we don't support entity resolution here
					throw new SAXParseException(Messages.getMessage("entityRefsNotSupported"), null, null);  //$NON-NLS-1$
				}
			});
			try {
				// important: keep this order
				documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			} catch (ParserConfigurationException e) {
				// this should never happen if you run the SourceProviderTest unittests
				logger.error(e.getMessage());
			}
			try {
				// workaround for JDK5 bug that causes NPE in checking done due to above FEATURE_SECURE_PROCESSING
				documentBuilderFactory.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
			} catch (ParserConfigurationException e) {
				// possible if not on apache parser?  ignore...
			}
		}

		@Override
		public DOMSource readFrom(Class<DOMSource> type,
				Type genericType,
				Annotation[] annotations,
				MediaType mediaType,
				MultivaluedMap<String, String> httpHeaders,
				InputStream entityStream) throws IOException,
				WebApplicationException {
			try {
				DocumentBuilder dbuilder = documentBuilderFactory.newDocumentBuilder();
//                RuntimeContext runtimeContext = RuntimeContextTLS.getRuntimeContext();
//                WinkConfiguration winkConfig = runtimeContext.getAttribute(WinkConfiguration.class);
//                if (winkConfig != null) {
//                    Properties props = winkConfig.getProperties();
//                    if (props != null) {
//                        // use valueOf method to require the word "true"
//                        if (!Boolean.valueOf(props.getProperty("wink.supportDTDEntityExpansion"))) {
//                            setupDocumentBuilderToFilterDTD(dbuilder);
//                        }
//                    }
//                }
				return new DOMSource(dbuilder.parse(entityStream));
			} catch (SAXException e) {
				logger.error(Messages.getMessage("saxParseException", type.getName()), e); //$NON-NLS-1$
				throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
			} catch (ParserConfigurationException e) {
				logger.error(Messages.getMessage("saxParserConfigurationException"), e); //$NON-NLS-1$
				throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
			}
		}
	}

	protected boolean isReadable(MediaType mediaType) {
		return MediaTypeUtils.isXmlType(mediaType);
	}

	@Override
	public long getSize(Source t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType) {
		return (Source.class.isAssignableFrom(type) && MediaTypeUtils.isXmlType(mediaType));
	}

	@Override
	public void writeTo(Source t,
			Class<?> type,
			Type genericType,
			Annotation[] annotations,
			MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException {
		StreamResult sr = new StreamResult(entityStream);
		Transformer transformer;
		try {
			transformer = transformerFactory.newTransformer();
			transformer.transform(t, sr);
		} catch (TransformerException e) {
			throw asIOException(e);
		}
	}

	private static IOException asIOException(Exception e) throws IOException {
		IOException exception = new IOException();
		exception.initCause(e);
		return exception;
	}
}
