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

import java.util.Set;

import org.apache.clerezza.rdf.core.UriRef;

/**
 * Implementations of this interface map RDF-types to type handlers. A type
 * handler is a JAX-RS resource object, which is provided as OSGi service. This
 * service is marked as type handler through the service property
 * "org.apache.clerezza.platform.typehandler=true".
 * 
 * @author rbn
 */
public interface TypeHandlerDiscovery {

	/**
	 * Returns the type handler for handling requests against resource of the
	 * most important rdf-type in the types set.
	 * 
	 * @param rdfTypes
	 *            Set of RDF-Types of the resource against which a request is to
	 *            be handled
	 * @return the type handler for most important rdf-type in the set
	 */
	public Object getTypeHandler(Set<UriRef> rdfTypes);

}
