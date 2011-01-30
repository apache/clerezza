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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * @author rbn
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportedTypes {

	/**
	 *
	 * @return the URIs of the types supported by this TypeHandler
	 */
	public String[] types();

	/**
	 * Indicates if this TypeHandler has to be prepended or appended to the
	 * list of TypeHandlers. Normally a typehandler is to be prepended, this
	 * option is only set to false in the rare case when one is installing a
	 * fallback-handler.
	 *
	 * @return true is the typehandler has to be prepended to the list of TypeHandlers
	 */
	public boolean prioritize() default true;
}
