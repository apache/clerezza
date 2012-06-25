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
package org.apache.clerezza.rdf.core.access;

import java.util.concurrent.locks.ReadWriteLock;
import org.apache.clerezza.rdf.core.MGraph;

/**
 *
 * Represents an <code>MGraph</code> that can be locked for reading/writing.
 *
 * @author rbn
 */
public interface  LockableMGraph extends MGraph {

	/**
	 * The lock provided by this methods allows to create read- and write-locks
	 * that span individual method calls. Having a read locks prevents other
	 * threads from writing to this MGraph, having a write-lock prevents other
	 * threads from reading and writing.
	 *
	 * @return the lock of this MGraph
	 */
	ReadWriteLock getLock();
}
