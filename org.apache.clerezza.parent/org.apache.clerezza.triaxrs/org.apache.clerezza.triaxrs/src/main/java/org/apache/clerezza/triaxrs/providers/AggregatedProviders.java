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
import java.lang.reflect.Type;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Providers;

/**
 * An implementtaion of {@link Providers} that delegates to a list of providers
 * (delegates) so that the provider returned by the first delegate not answering
 * null will be returned.
 *
 * @author reto
 */
public class AggregatedProviders implements Providers {

	private volatile Providers[] delegates;
	private final ReentrantReadWriteLock configLock;

	public AggregatedProviders(ReentrantReadWriteLock configLock, Providers... delegates) {
		this.delegates = delegates;
		this.configLock = configLock;
	}

	public void reset(Providers... delegates) {
		configLock.writeLock().lock();
		this.delegates = delegates;
		configLock.writeLock().unlock();
	}

	public Providers[] getDelegates() {
		configLock.readLock().lock();
		try {
			return delegates;
		} finally {
			configLock.readLock().unlock();
		}
	}

	@Override
	public <T> MessageBodyReader<T> getMessageBodyReader(Class<T> type,
			Type genericType, Annotation[] annotations, MediaType mediaType) {
		configLock.readLock().lock();
		try {
			for (Providers providers : delegates) {
				MessageBodyReader<T> result = providers.getMessageBodyReader(type,
						genericType, annotations, mediaType);
				if (result != null) {
					return result;
				}
			}
		} finally {
			configLock.readLock().unlock();
		}
		return null;
	}

	@Override
	public <T> MessageBodyWriter<T> getMessageBodyWriter(Class<T> type,
			Type genericType, Annotation[] annotations, MediaType mediaType) {
		configLock.readLock().lock();
		try {
			for (Providers providers : delegates) {
				MessageBodyWriter<T> result = providers.getMessageBodyWriter(type,
						genericType, annotations, mediaType);
				if (result != null) {
					return result;
				}
			}
		} finally {
			configLock.readLock().unlock();
		}
		return null;
	}

	@Override
	public <T extends java.lang.Throwable> ExceptionMapper<T> getExceptionMapper(Class<T> type) {
		configLock.readLock().lock();
		try {
			for (Providers providers : delegates) {
				ExceptionMapper<T> result = providers.getExceptionMapper(type);
				if (result != null) {
					return result;
				}
			}
		} finally {
			configLock.readLock().unlock();
		}
		return null;
	}

	@Override
	public <T> ContextResolver<T> getContextResolver(Class<T> contextType,
			MediaType mediaType) {
		configLock.readLock().lock();
		try {
			for (Providers providers : delegates) {
				ContextResolver<T> result = providers.getContextResolver(contextType,
						mediaType);
				if (result != null) {
					return result;
				}
			}
		} finally {
			configLock.readLock().unlock();
		}
		return null;
	}
}
