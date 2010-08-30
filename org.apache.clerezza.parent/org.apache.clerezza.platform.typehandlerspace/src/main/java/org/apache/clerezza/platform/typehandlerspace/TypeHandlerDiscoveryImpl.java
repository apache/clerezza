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
package org.apache.clerezza.platform.typehandlerspace;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.ontologies.RDFS;
import org.apache.clerezza.rdf.utils.RdfList;
import org.apache.felix.scr.annotations.ReferencePolicy;

/**
 * @author rbn
 */
@Component
@Service(value=TypeHandlerDiscovery.class)
@References({
	@Reference(name="typeHandler",
		cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
		referenceInterface=Object.class,
		target="(org.apache.clerezza.platform.typehandler=true)",
		policy=ReferencePolicy.DYNAMIC),
	@Reference(name="systemGraph",
		cardinality=ReferenceCardinality.MANDATORY_UNARY,
		referenceInterface=MGraph.class,
		target=SystemConfig.SYSTEM_GRAPH_FILTER)})
public class TypeHandlerDiscoveryImpl implements TypeHandlerDiscovery {

	/**
	 * type-handlers that are bound while the this component was not activated
	 * yet. Stored for later registration.
	 */
	private Set<Object> typeHandlerStore = new HashSet<Object>();

	private List<Resource> typePriorityList;
	private final Map<UriRef, Object> typeHandlerMap = Collections.synchronizedMap(
			new HashMap<UriRef, Object>());
	
	LockableMGraph systemGraph;

	protected void bindTypeHandler(Object typeHandler) {
		if (typePriorityList != null) {
			registerTypeHandler(typeHandler);
		} else {
			typeHandlerStore.add(typeHandler);
		}
	}
		
	protected void unbindTypeHandler(Object typeHandler) {
		if(!typeHandlerStore.remove(typeHandler)) {
			unregisterTypeHandler(typeHandler);
		}
	}

	protected void bindSystemGraph(MGraph systemGraph) {
		typePriorityList = new RdfList(
				new UriRef("http://tpf.localhost/typePriorityList"), systemGraph);
		this.systemGraph = (LockableMGraph) systemGraph;
	}

	protected void unbindSystemGraph(MGraph systemGraph) {
		typePriorityList = null;
		this.systemGraph = null;
	}

	protected void activate(ComponentContext context) throws Exception {
		Iterator<Object> handers = typeHandlerStore.iterator();
		while (handers.hasNext()) {
			Object object = handers.next();
			registerTypeHandler(object);
		}
		typeHandlerStore.clear();
	}

	@Override
	public Object getTypeHandler(final Set<UriRef> types) {
		return AccessController.doPrivileged(new PrivilegedAction<Object>() {

			@Override
			public Object run() {
				Lock readLock = systemGraph.getLock().readLock();
				readLock.lock();
				try {
					for (Resource type : typePriorityList) {
						if (types.contains(type)) {
							Object result = typeHandlerMap.get(type);
							if (result != null) {
								return result;
							}
						}
					}
				} finally {
					readLock.unlock();
				}
				return typeHandlerMap.get(RDFS.Resource);
			}
		});		
	}

	private void registerTypeHandler(Object component) {
		SupportedTypes supportedTypes = component.getClass()
				.getAnnotation(SupportedTypes.class);
		if (supportedTypes == null) {
			return;
		}		
		for (String typeUriString : supportedTypes.types()) {
			UriRef typeUri = new UriRef(typeUriString);
			Lock writeLock = systemGraph.getLock().writeLock();
			writeLock.lock();
			try {
				if (!typePriorityList.contains(typeUri)) {
					if (supportedTypes.prioritize()) {
						typePriorityList.add(0, typeUri);
					} else {
						typePriorityList.add(typeUri);
					}
				}
			} finally {
				writeLock.unlock();
			}
			typeHandlerMap.put(typeUri, component);
		}
	}

	private void unregisterTypeHandler(Object component) {
		Iterator<UriRef> keys = typeHandlerMap.keySet().iterator();
		Set<UriRef> toRemove = new HashSet<UriRef>(typeHandlerMap.size());
		synchronized(typeHandlerMap) {
			while (keys.hasNext()) {
				UriRef uriRef = keys.next();
				if(typeHandlerMap.get(uriRef)==component) {
					toRemove.add(uriRef);
				}
			}
		}
		keys = toRemove.iterator();
		while (keys.hasNext()) {
			typeHandlerMap.remove(keys.next());
		}
	}
}
