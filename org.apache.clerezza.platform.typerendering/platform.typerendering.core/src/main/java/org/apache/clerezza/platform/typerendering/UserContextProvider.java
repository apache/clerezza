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
package org.apache.clerezza.platform.typerendering;

import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * A implementation of <code>UserContextProvider</code> adds user context
 * information to a provided <code>GraphNode</code>. The added information should
 * be specifically for the currently logged in user.
 *
 * @author mir
 */
public interface UserContextProvider {

	/**
	 * Returns a <code>GraphNode</code> containing user context information in
	 * addition to the information already existing in the provided
	 * <code>GraphNode</code>.
	 * The information previously existing in the provided <code>GraphNode</code>
	 * are not changed by this method.
	 * The method may add the context information directly to the provided
	 * <code>GraphNode</code> or create a new <code>GraphNode</code> instance,
	 * in the latter the returned GraphNode must be modifiable.
	 * In both cases the resulting <code>GraphNode</code> is returned by the
	 * method.
	 * 
	 * @param node
	 * @return The GraphNode with additional user context information
	 */
	public GraphNode addUserContext(GraphNode node);
}
