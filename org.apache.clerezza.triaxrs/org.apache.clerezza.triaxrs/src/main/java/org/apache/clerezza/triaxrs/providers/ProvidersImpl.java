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
package org.apache.clerezza.triaxrs.providers;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link Providers} that allows setting providers
 * by instance and by class at construction or afterwards
 * 
 * @author reto
 */
public class ProvidersImpl implements Providers {

	private Set<MessageBodyReader<?>> bodyReaders;
	private SelectableProviders<MessageBodyReader<?>> selectableBodyReaders;
	private Set<MessageBodyWriter<?>> bodyWriters;
	private SelectableProviders<MessageBodyWriter<?>> selectableBodyWriters;
	private Set<ExceptionMapper<?>> exceptionMappers;
	private Set<ContextResolver<?>> contextResolvers;
	private Logger logger = LoggerFactory.getLogger(ProvidersImpl.class);

	public ProvidersImpl() {
		bodyReaders =
				new HashSet<MessageBodyReader<?>>();
		bodyWriters =
				new HashSet<MessageBodyWriter<?>>();
		exceptionMappers = new HashSet<ExceptionMapper<?>>();
		contextResolvers = new HashSet<ContextResolver<?>>();
		this.selectableBodyReaders = selectableBodyReaderFromSet(bodyReaders);
		this.selectableBodyWriters = selectableBodyWritersFromSet(bodyWriters);
	}

	@SuppressWarnings("unchecked")
	public ProvidersImpl(Class<?>[] providerClasses) {
		bodyReaders =
				new HashSet<MessageBodyReader<?>>();
		bodyWriters =
				new HashSet<MessageBodyWriter<?>>();
		exceptionMappers = new HashSet<ExceptionMapper<?>>();
		contextResolvers = new HashSet<ContextResolver<?>>();
		Map<Class<?>, Set> typeSetMap = new HashMap<Class<?>, Set>();
		typeSetMap.put(MessageBodyReader.class, bodyReaders);
		typeSetMap.put(MessageBodyWriter.class, bodyWriters);
		typeSetMap.put(ExceptionMapper.class, exceptionMappers);
		typeSetMap.put(ContextResolver.class, contextResolvers);
		for (Class<?> providerClass : providerClasses) {
			for (Class<?> supportedClass : typeSetMap.keySet()) {
				if (supportedClass.isAssignableFrom(providerClass)) {
					try {
						(typeSetMap.get(supportedClass)).add(providerClass.
								newInstance());
					} catch (InstantiationException ex) {
						logger.error("Exception {}", ex);
					} catch (IllegalAccessException ex) {
						logger.error("Exception {}", ex);
					}
				}
			}

		}
		this.selectableBodyReaders = selectableBodyReaderFromSet(bodyReaders);
		this.selectableBodyWriters = selectableBodyWritersFromSet(bodyWriters);
	}

	public ProvidersImpl(Set<MessageBodyReader<?>> bodyReaders,
			Set<MessageBodyWriter<?>> bodyWriters,
			Set<ContextResolver<?>> contextResolvers,
			Set<ExceptionMapper<?>> exceptionMappers) {
		this.bodyReaders = bodyReaders;
		this.bodyWriters = bodyWriters;
		this.selectableBodyReaders = selectableBodyReaderFromSet(bodyReaders);
		this.selectableBodyWriters = selectableBodyWritersFromSet(bodyWriters);
		this.contextResolvers = contextResolvers;
		this.exceptionMappers = exceptionMappers;
	}

	public void addClass(Class<?> componentClass) {
		try {
			addInstance(componentClass.newInstance());
		} catch (InstantiationException ex) {
			logger.error("Exception {}", ex);
		} catch (IllegalAccessException ex) {
			logger.error("Exception {}", ex);
		}
	}

	public void addInstance(Object component) {
		if (component instanceof MessageBodyWriter) {
			bodyWriters.add((MessageBodyWriter<?>) component);
			selectableBodyWriters = selectableBodyWritersFromSet(bodyWriters);
			return;
		}
		if (component instanceof MessageBodyReader) {
			bodyReaders.add((MessageBodyReader<?>) component);
			selectableBodyReaders = selectableBodyReaderFromSet(bodyReaders);
			return;
		}
		if (component instanceof ExceptionMapper) {
			exceptionMappers.add((ExceptionMapper<?>) component);
			return;
		}
		throw new RuntimeException("unsupported provider component "+component);
	}


	@Override
	public <T> ContextResolver<T> getContextResolver(Class<T> contextType,
			MediaType mediaType) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Set<ContextResolver<?>> getContextResolvers() {
		return contextResolvers;
	}

	@Override
	public <T extends Throwable> ExceptionMapper<T> getExceptionMapper(final Class<T> clazz) {
		//Get an exception mapping provider for a particular class of exception. 
		//Returns the provider whose generic type is the nearest superclass of type.
		if (!Exception.class.isAssignableFrom(clazz)) {
			return null;
		}
		Class<?> checkedClass = clazz;
		while (true) {
			ExceptionMapper exceptionMapper = getExceptionMapperForExactType(
					checkedClass);
			if (exceptionMapper != null) {
				return exceptionMapper;
			}
			if (checkedClass == Throwable.class) {
				return null;
			}
			checkedClass = checkedClass.getSuperclass();
		}
	}

	boolean isEmpty() {
		return (bodyReaders.isEmpty() && bodyWriters.isEmpty() &&
				exceptionMappers.isEmpty() && contextResolvers.isEmpty());
	}

	void removeInstance(Object component) {
		if (component instanceof MessageBodyWriter) {
			bodyWriters.remove((MessageBodyWriter<?>) component);
			selectableBodyWriters = selectableBodyWritersFromSet(bodyWriters);
			return;
		}
		if (component instanceof MessageBodyReader) {
			bodyReaders.remove((MessageBodyReader<?>) component);
			selectableBodyReaders = selectableBodyReaderFromSet(bodyReaders);
			return;
		}
		if (component instanceof ExceptionMapper) {
			exceptionMappers.remove((ExceptionMapper<?>) component);
			return;
		}
		throw new RuntimeException("unsupported provider component "+component);
	}

	private ExceptionMapper getExceptionMapperForExactType(Class<?> clazz) {
		for (ExceptionMapper<?> exceptionMapper : exceptionMappers) {
			for (Type type : exceptionMapper.getClass().getGenericInterfaces()) {
				if (type instanceof ParameterizedType) {
					ParameterizedType parameterizedType =
							(ParameterizedType) type;
					if (parameterizedType.getRawType() == ExceptionMapper.class) {
						if (parameterizedType.getActualTypeArguments()[0].equals(
								clazz)) {
							return exceptionMapper;
						}
					}
				}
			}
		}
		return null;
	}



	@SuppressWarnings("unchecked")
	@Override
	public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> c, Type t,
			Annotation[] as, MediaType mediaType) {
		return (MessageBodyReader<T>) selectableBodyReaders.selectFor(c, t, as,
				mediaType);

	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> c,
			Type t, Annotation[] as, MediaType mediaType) {
		return (MessageBodyWriter<T>) selectableBodyWriters.selectFor(c, t, as,
				mediaType);
	}

	private SelectableProviders<MessageBodyReader<?>> selectableBodyReaderFromSet(
			Set<MessageBodyReader<?>> bodyReaders) {
		return new SelectableProviders<MessageBodyReader<?>>(
				bodyReaders, new ProviderCriteria<MessageBodyReader<?>>() {

			@Override
			public boolean isAcceptable(MessageBodyReader<?> messageBodyReader,
					Class<?> c, Type t, Annotation[] as, MediaType m) {
				return messageBodyReader.isReadable(c, t, as, m);
			}

			@Override
			public String[] getMediaTypeAnnotationValues(
					MessageBodyReader<?> producer) {
				Consumes consumes = producer.getClass().getAnnotation(
						Consumes.class);
				if (consumes == null) {
					return null;
				} else {
					return consumes.value();
				}
			}
		});
	}

	private SelectableProviders<MessageBodyWriter<?>> selectableBodyWritersFromSet(
			Set<MessageBodyWriter<?>> bodyWriters) {
		return new SelectableProviders<MessageBodyWriter<?>>(
				bodyWriters, new ProviderCriteria<MessageBodyWriter<?>>() {

			@Override
			public boolean isAcceptable(MessageBodyWriter<?> messageBodyWriter,
					Class<?> c, Type t, Annotation[] as, MediaType m) {
				return messageBodyWriter.isWriteable(c, t, as, m);
			}
			
			@Override
			public String[] getMediaTypeAnnotationValues(
					MessageBodyWriter<?> producer) {
				Produces produces = producer.getClass().getAnnotation(
						Produces.class);
				if (produces == null) {
					return null;
				} else {
					return produces.value();
				}
			}
		});
	}
}
