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
package org.apache.clerezza.platform.dashboard;

import java.util.Set;

/**
 * An implementation of this interface provides a set of
 * <code>GlobalMenuItem</code>S. A <code>GlobalMenuItem</code> represents an
 * item in the global menu of the Clerezza Platform.
 *
 * @author mir
 */
public interface  GlobalMenuItemsProvider {

	/**
	 * Returns a <code>Set</code> of <code>GlobalMenuItem</code>S. If there is
	 * no <code>GlobalMenuItem</code> available (e.g. the current user has not
	 * the needed permissions) then the returned set is empty.
	 * @return
	 */
	public Set<GlobalMenuItem> getMenuItems();
}
