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
package org.apache.clerezza.platform.concepts.core;

import java.util.List;

/**
 * An implementation of this class manages {@link ConceptProvider}s.
 *
 * @author hasan
 */
public interface ConceptProviderManager {

	/**
	 * Returns a list of {@link ConceptProvider}s. The list order determines
	 * the priority of a {@link ConceptProvider}. The meaning of the priority
	 * is left to the implementation and application.
	 * 
	 * @return
	 *		a list of managed {@link ConceptProvider}s.
	 */
	public List<ConceptProvider> getConceptProviders();
}
