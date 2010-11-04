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
package org.apache.clerezza.triaxrs.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderUtils {

	private static final Logger logger =
			LoggerFactory.getLogger(ProviderUtils.class);
	private static final String DEFAULT_CHARSET = "UTF-8";
	private static SoftConcurrentMap<String, Boolean> validCharsets =
			new SoftConcurrentMap<String, Boolean>();
	private static SoftConcurrentMap<String, String> preferredCharsets =
			new SoftConcurrentMap<String, String>();

	public static String getCharsetOrNull(MediaType m) {
		String name = (m == null) ? null : m.getParameters().get("charset"); //$NON-NLS-1$
		return (name == null) ? null : name;
	}

	public static String getCharset(MediaType m) {
		return getCharset(m, null);
	}

	/**
	 * Returns the charset on the chosen media type or, if no charset parameter
	 * exists on the chosen media type, the most acceptable charset based on the
	 * request headers.
	 *
	 * @param m the chosen media type
	 * @param requestHeaders the request headers to inspect
	 * @return the charset
	 */
	public static String getCharset(MediaType m, HttpHeaders requestHeaders) {
		logger.debug("getCharset({}, {})", m, requestHeaders);
		String name = (m == null) ? null : m.getParameters().get("charset"); //$NON-NLS-1$
		if (name != null) {
			logger.debug("getCharset() returning {} since parameter was set", name);
			return name;
		}
		if (requestHeaders == null) {
			logger.debug("getCharset() returning {} since requestHeaders was null", DEFAULT_CHARSET);
			return DEFAULT_CHARSET;
		}

		List<String> acceptableCharsets =
				requestHeaders.getRequestHeader(HttpHeaders.ACCEPT_CHARSET);
		if (acceptableCharsets == null || acceptableCharsets.isEmpty()) {
			// HTTP spec says that no Accept-Charset header indicates that any
			// charset is acceptable so we'll stick with UTF-8 by default.
			logger.debug("getCharset() returning {} since no Accept-Charset header",
					DEFAULT_CHARSET);
			return DEFAULT_CHARSET;
		}

		StringBuilder acceptCharsetsTemp = new StringBuilder();
		acceptCharsetsTemp.append(acceptableCharsets.get(0));
		for (int c = 1; c < acceptableCharsets.size(); ++c) {
			acceptCharsetsTemp.append(","); //$NON-NLS-1$
			acceptCharsetsTemp.append(acceptableCharsets.get(c));
		}
		String acceptCharsets = acceptCharsetsTemp.toString();
		logger.debug("acceptCharsets combined value is {}", acceptCharsets);
		String cached = preferredCharsets.get(acceptCharsets);
		if (cached != null) {
			return cached;
		}
		AcceptCharset charsets = AcceptCharset.valueOf(acceptCharsets);

		if (charsets.isAnyCharsetAllowed()) {
			preferredCharsets.put(acceptCharsets, DEFAULT_CHARSET);
			return DEFAULT_CHARSET;
		}

		List<String> orderedCharsets = charsets.getAcceptableCharsets();
		logger.debug("orderedCharsets is {}", orderedCharsets);
		if (!orderedCharsets.isEmpty()) {
			for (int c = 0; c < orderedCharsets.size(); ++c) {
				String charset = orderedCharsets.get(c);
				try {
					Boolean b = validCharsets.get(charset);
					if (b != null && b.booleanValue()) {
						logger.debug("getCharset() returning {} since highest Accept-Charset value",
								charset);
						preferredCharsets.put(acceptCharsets, charset);
						return charset;
					}
					Charset.forName(charset);
					validCharsets.put(charset, Boolean.TRUE);
					logger.debug("getCharset() returning {} since highest Accept-Charset value",
							charset);
					preferredCharsets.put(acceptCharsets, charset);
					return charset;
				} catch (IllegalCharsetNameException e) {
					logger.debug("IllegalCharsetNameException for {}", charset, e);
					validCharsets.put(charset, Boolean.FALSE);
				} catch (UnsupportedCharsetException e) {
					logger.debug("UnsupportedCharsetException for {}", charset, e);
					validCharsets.put(charset, Boolean.FALSE);
				} catch (IllegalArgumentException e) {
					logger.debug("IllegalArgumentException for {}", charset, e);
					validCharsets.put(charset, Boolean.FALSE);
				}
			}
		}
		// At this point, it's either any charset is allowed (i.e. wildcard "*"
		// has a higher quality value than any other charset sent in the
		// Accept-Charset header), or we only have banned charsets. If there are
		// any banned charsets, then technically we should pick a non-banned
		// charset.
		logger.debug("getCharset() returning {} since no explicit charset required",
				DEFAULT_CHARSET);
		preferredCharsets.put(acceptCharsets, DEFAULT_CHARSET);
		return DEFAULT_CHARSET;
	}

	public static Reader createReader(InputStream stream, MediaType mediaType) {
		Charset charset = Charset.forName(getCharset(mediaType));
		return new InputStreamReader(stream, charset);
	}

	public static Writer createWriter(OutputStream stream, MediaType mediaType) {
		Charset charset = Charset.forName(getCharset(mediaType));
		return new OutputStreamWriter(stream, charset);
	}

	private static ByteArrayOutputStream readFromStream(InputStream stream) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
		copyStream(stream, os);
		return os;
	}

	public static byte[] readFromStreamAsBytes(InputStream stream) throws IOException {
		ByteArrayOutputStream os = readFromStream(stream);
		return os.toByteArray();
	}

	public static String readFromStreamAsString(InputStream stream, MediaType mt)
			throws IOException {
		ByteArrayOutputStream os = readFromStream(stream);
		return os.toString(ProviderUtils.getCharset(mt));
	}

	public static void writeToStream(String string, OutputStream os, MediaType mt)
			throws IOException {
		os.write(string.getBytes(getCharset(mt)));
	}

	public static void copyStream(InputStream src, OutputStream dst) throws IOException {
		byte[] bytes = new byte[1024];
		int read = 0;
		while ((read = src.read(bytes)) != -1) {
			dst.write(bytes, 0, read);
		}
	}

	public static String writeToString(Providers providers, Object object, MediaType mediaType)
			throws IOException {
		return writeToString(providers, object, object.getClass(), mediaType);
	}

	public static String writeToString(Providers providers,
			Object object,
			Class<?> type,
			MediaType mediaType) throws IOException {
		return writeToString(providers, object, type, type, mediaType);
	}

	public static String writeToString(Providers providers,
			Object object,
			Class<?> type,
			Type genericType,
			MediaType mediaType) throws IOException {
		return writeToString(providers,
				object,
				type,
				type,
				new MultivaluedMapImpl<String, Object>(),
				mediaType);
	}

	@SuppressWarnings("unchecked")
	public static String writeToString(Providers providers,
			Object object,
			Class<?> type,
			Type genericType,
			MultivaluedMap<String, Object> httpHeaders,
			MediaType mediaType) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		MessageBodyWriter writer =
				providers.getMessageBodyWriter(type, genericType, null, mediaType);
		if (writer == null) {
			return null;
		}
		writer.writeTo(object, type, genericType, new Annotation[0], mediaType, httpHeaders, os);
		String contentString = os.toString(getCharset(mediaType));
		return contentString;
	}

	public static <T> T readFromString(Providers providers,
			String input,
			Class<T> type,
			MediaType mediaType) throws IOException {
		return readFromString(providers, input, type, type, mediaType);
	}

	public static <T> T readFromString(Providers providers,
			String input,
			Class<T> type,
			Type genericType,
			MediaType mediaType) throws IOException {
		return readFromString(providers,
				input,
				type,
				genericType,
				new MultivaluedMapImpl<String, String>(),
				mediaType);
	}

	public static <T> T readFromString(Providers providers,
			String input,
			Class<T> type,
			Type genericType,
			MultivaluedMap<String, String> httpHeaders,
			MediaType mediaType) throws IOException {
		ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes(getCharset(mediaType)));
		MessageBodyReader<T> reader =
				providers.getMessageBodyReader(type, genericType, null, mediaType);
		if (reader == null) {
			return null;
		}
		return reader.readFrom(type, genericType, new Annotation[0], mediaType, httpHeaders, is);
	}
}
