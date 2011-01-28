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
package org.apache.clerezza.permissiondescriptions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to annotate {@link Permission}s to provide additional
 * information about the permission
 *
 * @author mir
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionInfo {
	
	/**
	 * The name of the permission in a human comprehensible form.
	 */
	String value();
	
	/**
	 * A description about the permission.
	 */
	String description() default "";

	/**
	 * A relative path to the icon resource that describes pictorgraphically what 
	 * the permission does. E.g. the icon of a file read permission may depict an eye,
	 * while the icon for a file write permission might be a pencil.
	 */
	String icon() default "";

}
