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
package org.apache.clerezza.rdf.core.sparql.query.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.clerezza.rdf.core.sparql.query.Expression;
import org.apache.clerezza.rdf.core.sparql.query.GraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.GroupGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.TriplePattern;

/**
 *
 * @author hasan
 */
public class SimpleGroupGraphPattern implements GroupGraphPattern {

	private List<Expression> constraints = new ArrayList<Expression>();
	private List<GraphPattern> graphPatterns = new ArrayList<GraphPattern>();

	@Override
	public Set<GraphPattern> getGraphPatterns() {
		return new HashSet(graphPatterns);
	}

	@Override
	public List<Expression> getFilter() {
		return constraints;
	}

	public void addGraphPattern(GraphPattern graphPattern) {
		graphPatterns.add(graphPattern);
	}

	public void addConstraint(Expression constraint) {
		constraints.add(constraint);
	}

	public void addTriplePatterns(Set<TriplePattern> triplePatterns) {
		GraphPattern prevGraphPattern;
		int size = graphPatterns.size();
		if (size > 0) {
			prevGraphPattern = graphPatterns.get(size-1);
			if (prevGraphPattern instanceof SimpleBasicGraphPattern) {
				((SimpleBasicGraphPattern) prevGraphPattern)
						.addTriplePatterns(triplePatterns);
				return;
			}
		}
		graphPatterns.add(new SimpleBasicGraphPattern(triplePatterns));
	}

	public void addOptionalGraphPattern(GroupGraphPattern optional) {

		GraphPattern prevGraphPattern = null;
		int size = graphPatterns.size();
		if (size > 0) {
			prevGraphPattern = graphPatterns.remove(size-1);
		}
		graphPatterns.add(new SimpleOptionalGraphPattern(prevGraphPattern, optional));
	}
}
