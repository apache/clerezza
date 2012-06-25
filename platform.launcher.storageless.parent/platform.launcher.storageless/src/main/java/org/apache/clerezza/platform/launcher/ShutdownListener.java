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

package org.apache.clerezza.platform.launcher;

import org.osgi.framework.FrameworkEvent;

/**
 * A listener that gets notified when the Felix Framework is shut down.
 *
 * @author daniel
 */
public interface ShutdownListener {

	/**
	 * Notify listener of complete Clerezza shut down.
	 *
	 * @param event
	 *		What event caused the shutdown. Note: event may be null.
	 */
	public void notify(FrameworkEvent event);
}
