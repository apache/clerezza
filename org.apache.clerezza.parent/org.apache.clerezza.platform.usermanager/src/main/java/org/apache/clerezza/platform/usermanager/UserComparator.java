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
package org.apache.clerezza.platform.usermanager;

import org.apache.clerezza.rdf.utils.GraphNode;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.ontologies.PLATFORM;

public class UserComparator implements Comparator<GraphNode> {

	@Override
	public int compare(GraphNode user1, GraphNode user2) {
		Iterator<Literal> names1 = user1.getLiterals(PLATFORM.userName);
		Iterator<Literal> names2 = user2.getLiterals(PLATFORM.userName);
		if (names1.hasNext() && names2.hasNext()) {
			return names1.next().getLexicalForm()
					.compareTo(names2.next().getLexicalForm());
		}
		else {
			throw new RuntimeException("Cannot compare users!");
		}
	}

}
