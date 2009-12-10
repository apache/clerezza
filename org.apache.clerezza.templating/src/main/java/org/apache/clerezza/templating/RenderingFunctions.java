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
package org.apache.clerezza.templating;

import java.util.Map;

/**
 * Represents a set of functions used for the customizable conversion of values
 * that have been retrieved from the <code>GraphNode</code>s. It generates
 * modified values used as the result of template processing as well as in
 * comparisons.
 *
 * @author rbn
 */
public interface RenderingFunctions {

	/**
	 *
	 * @return the function that is the outermost function of all access to values
	 */
	public RenderingFunction<Object, String> getDefaultFunction();

	/**
	 *
	 * @return a set of named functions to be used in templates to have values
	 *         rendered in non default-ways
	 */
	public Map<String, RenderingFunction> getNamedFunctions();

}
