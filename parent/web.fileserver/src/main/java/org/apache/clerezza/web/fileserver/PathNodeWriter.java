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
package org.apache.clerezza.web.fileserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.web.fileserver.util.MediaTypeGuesser;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 *
 * @author reto
 */
@Component(metatype=true)
@Service(value = Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Provider
public class PathNodeWriter implements MessageBodyWriter<PathNode> {

	@Property(value="600", label="Max-Age", description="Specifies the value of the max-age field"
		+ "set in the cache-control header, as per RFC 2616 this is a number of "
		+ "seconds")
	public static final String MAX_AGE = "max-age";

	private final Logger logger = LoggerFactory.getLogger(PathNodeWriter.class);
	private String cacheControlHeaderValue;

	protected void activate(ComponentContext context) {
		cacheControlHeaderValue = "max-age="+(String) context.getProperties().get(MAX_AGE);
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return PathNode.class.isAssignableFrom(type);
	}

	@Override
	public long getSize(PathNode t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return t.getLength();
	}

	@Override
	public void writeTo(PathNode t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream entityStream) throws IOException, WebApplicationException {
		logger.debug("Invoked with media type: {}", mediaType);
		logger.debug("Invoked with pathnode: {}", t);
		if (mediaType.equals(MediaType.APPLICATION_OCTET_STREAM_TYPE)) {
			MediaType guessedMediaType = MediaTypeGuesser.getInstance().guessTypeForName(t.getPath());
			if (guessedMediaType != null) {
				httpHeaders.putSingle(HttpHeaders.CONTENT_TYPE, guessedMediaType);
				logger.debug("Set media-type to: {}", guessedMediaType);
			}
		}
		if (!httpHeaders.containsKey(HttpHeaders.CACHE_CONTROL)) {
			httpHeaders.putSingle(HttpHeaders.CACHE_CONTROL, cacheControlHeaderValue);
		} else {
			logger.debug("httpHeaders already contain CACHE_CONTROL");
		}
		InputStream in = t.getInputStream();
		for (int ch = in.read(); ch != -1; ch = in.read()) {
			entityStream.write(ch);
		}
	}
}
