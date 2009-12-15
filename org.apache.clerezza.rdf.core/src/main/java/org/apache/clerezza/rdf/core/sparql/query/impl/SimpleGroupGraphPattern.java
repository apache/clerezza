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
import org.apache.clerezza.rdf.core.sparql.query.OptionalGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.TriplePattern;

/**
 * This class implements {@link GroupGraphPattern}.
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

	/**
	 * Adds a {@link GraphPattern} to the group.
	 *
	 * @param graphPattern
	 *		the GraphPattern to be added.
	 */
	public void addGraphPattern(GraphPattern graphPattern) {
		graphPatterns.add(graphPattern);
	}

	/**
	 * Adds a constraint to the {@link GroupGraphPattern}.
	 *
	 * @param constraint
	 *		an {@link Expression} as the constraint to be added.
	 */
	public void addConstraint(Expression constraint) {
		constraints.add(constraint);
	}

	/**
	 * If the last {@link GraphPattern} added to the group is not a 
	 * {@link SimpleBasicGraphPattern}, then creates one containing the 
	 * specified {@link TriplePattern}s and adds it to the group.
	 * Otherwise, adds the specified {@link TriplePattern}s to the last
	 * added {@link SimpleBasicGraphPattern} in the group.
	 * 
	 * @param triplePatterns
	 *		a set of {@link TriplePattern}s to be added into a 
	 *		{@link SimpleBasicGraphPattern} of the group.
	 */
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

	/**
	 * Adds an {@link OptionalGraphPattern} to the group consisting of
	 * a main graph pattern and the specified {@link GroupGraphPattern} as
	 * the optional pattern.
	 * The main graph pattern is taken from the last added {@link GraphPattern}
	 * in the group, if it exists. Otherwise, the main graph pattern is null.
	 *
	 * @param optional
	 *		a {@link GroupGraphPattern} as the optional pattern of
	 *		an {@link OptionalGraphPattern}.
	 */
	public void addOptionalGraphPattern(GroupGraphPattern optional) {

		GraphPattern prevGraphPattern = null;
		int size = graphPatterns.size();
		if (size > 0) {
			prevGraphPattern = graphPatterns.remove(size-1);
		}
		graphPatterns.add(new SimpleOptionalGraphPattern(prevGraphPattern, optional));
	}
}
