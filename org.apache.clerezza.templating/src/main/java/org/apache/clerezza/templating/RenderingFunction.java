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

import java.io.IOException;

/**
 * a rendering fuction transforms one or several input values into a result
 *
 * @author rbn
 */
public interface RenderingFunction<T0, T1> {

	/**
	 * Applies the function represented by the instance to one or several
	 * values
	 *
	 * @param values the function arguments
	 * @return f(value1, ...)
	 */
	public T1 process(T0... values) throws IOException;
}
