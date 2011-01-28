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
package org.apache.clerezza.platform.security.conditions;

import org.osgi.framework.Bundle;
import org.osgi.service.condpermadmin.BundleLocationCondition;
import org.osgi.service.condpermadmin.Condition;
import org.osgi.service.condpermadmin.ConditionInfo;

/**
 *
 * @author mir
 */
public class NotBundleLocationCondition {

	/**
	 * Returns the negated <code>Condition</code> of 
	 * <code>BundleLocationCondition</code>.
	 * 
	 * @param bundle The Bundle being evaluated.
	 * @param info The ConditionInfo to construct the condition for. 
	 * @return Condition object for the requested condition.
	 */
	static public Condition getCondition(final Bundle bundle, ConditionInfo info) {
		ConditionInfo bundleLocationInfo = new ConditionInfo(
				BundleLocationCondition.class.getName(), info.getArgs());
		Condition result = BundleLocationCondition.getCondition(bundle,
				bundleLocationInfo);
		return result == Condition.TRUE ? Condition.FALSE: Condition.TRUE;
	}
}
