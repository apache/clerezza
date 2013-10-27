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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.sparql.query.AlternativeGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.BasicGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.Expression;
import org.apache.clerezza.rdf.core.sparql.query.GraphGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.GraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.GroupGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.MinusGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.OptionalGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.PathSupportedBasicGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.PropertyPathPattern;
import org.apache.clerezza.rdf.core.sparql.query.SelectQuery;
import org.apache.clerezza.rdf.core.sparql.query.TriplePattern;
import org.apache.clerezza.rdf.core.sparql.query.UriRefOrVariable;

/**
 * This class implements {@link GroupGraphPattern}.
 *
 * @author hasan
 */
public class SimpleGroupGraphPattern implements GroupGraphPattern {

	private List<Expression> constraints = new ArrayList<Expression>();
	private List<GraphPattern> graphPatterns = new ArrayList<GraphPattern>();
    private SelectQuery subSelect = null;
    private boolean lastBasicGraphPatternIsComplete = true;

    @Override
    public boolean isSubSelect() {
        return subSelect != null;
    }

    @Override
    public SelectQuery getSubSelect() {
        return subSelect;
    }

    @Override
	public Set<GraphPattern> getGraphPatterns() {
		return subSelect == null ? new LinkedHashSet(graphPatterns) : null;
	}

	@Override
	public List<Expression> getFilter() {
		return subSelect == null ? constraints : null;
	}

    public void setSubSelect(SelectQuery subSelect) {
        this.subSelect = subSelect;
    }

    /**
	 * Adds a {@link GraphPattern} to the group.
	 *
	 * @param graphPattern
	 *		the GraphPattern to be added.
	 */
	public void addGraphPattern(GraphPattern graphPattern) {
        subSelect = null;
        graphPatterns.add(graphPattern);
        lastBasicGraphPatternIsComplete =
                !(graphPattern instanceof BasicGraphPattern || graphPattern instanceof PathSupportedBasicGraphPattern);
	}

	/**
	 * Adds a constraint to the {@link GroupGraphPattern}.
	 *
	 * @param constraint
	 *		an {@link Expression} as the constraint to be added.
	 */
	public void addConstraint(Expression constraint) {
        subSelect = null;
		constraints.add(constraint);
	}

    public void endLastBasicGraphPattern() {
        lastBasicGraphPatternIsComplete = true;
    }

    /**
	 * If the last {@link GraphPattern} added to the group is not a 
	 * {@link SimplePathSupportedBasicGraphPattern}, then creates one containing the 
	 * specified {@link PropertyPathPattern}s and adds it to the group.
	 * Otherwise, adds the specified {@link PropertyPathPattern}s to the last
	 * added {@link SimplePathSupportedBasicGraphPattern} in the group.
	 * 
	 * @param propertyPathPatterns
	 *		a set of {@link PropertyPathPattern}s to be added into a 
	 *		{@link SimplePathSupportedBasicGraphPattern} of the group.
	 */
	public void addPropertyPathPatterns(Set<PropertyPathPattern> propertyPathPatterns) {
        subSelect = null;
        if (lastBasicGraphPatternIsComplete) {
            graphPatterns.add(new SimplePathSupportedBasicGraphPattern(propertyPathPatterns));
            lastBasicGraphPatternIsComplete = false;
        } else {
            GraphPattern prevGraphPattern;
        	int size = graphPatterns.size();
			prevGraphPattern = graphPatterns.get(size-1);
            if (prevGraphPattern instanceof SimplePathSupportedBasicGraphPattern) {
                ((SimplePathSupportedBasicGraphPattern) prevGraphPattern).addPropertyPathPatterns(propertyPathPatterns);
            }
        }
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
        subSelect = null;
        GraphPattern prevGraphPattern;
		int size = graphPatterns.size();
		if (!lastBasicGraphPatternIsComplete && (size > 0)) {
			prevGraphPattern = graphPatterns.get(size-1);
			if (prevGraphPattern instanceof SimpleBasicGraphPattern) {
				((SimpleBasicGraphPattern) prevGraphPattern)
						.addTriplePatterns(triplePatterns);
				return;
			}
		}
		graphPatterns.add(new SimpleBasicGraphPattern(triplePatterns));
        lastBasicGraphPatternIsComplete = false;
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
        subSelect = null;
		GraphPattern prevGraphPattern = null;
		int size = graphPatterns.size();
		if (size > 0) {
			prevGraphPattern = graphPatterns.remove(size-1);
		}
		graphPatterns.add(new SimpleOptionalGraphPattern(prevGraphPattern, optional));
        lastBasicGraphPatternIsComplete = true;
	}

    public void addMinusGraphPattern(GroupGraphPattern subtrahend) {
        subSelect = null;
		GraphPattern prevGraphPattern = null;
		int size = graphPatterns.size();
		if (size > 0) {
			prevGraphPattern = graphPatterns.remove(size-1);
		}
		graphPatterns.add(new SimpleMinusGraphPattern(prevGraphPattern, subtrahend));
        lastBasicGraphPatternIsComplete = true;
	}

    @Override
    public Set<UriRef> getReferredGraphs() {
        Set<UriRef> referredGraphs = new HashSet<UriRef>();
        if (subSelect != null) {
            GroupGraphPattern queryPattern = subSelect.getQueryPattern();
            referredGraphs.addAll(queryPattern.getReferredGraphs());
        } else {
            for (GraphPattern graphPattern : graphPatterns) {
                referredGraphs.addAll(getReferredGraphs(graphPattern));
            }
        }
        return referredGraphs;
    }

    private Set<UriRef> getReferredGraphs(GraphPattern graphPattern) {
        Set<UriRef> referredGraphs = new HashSet<UriRef>();
        if (graphPattern instanceof GraphGraphPattern) {
            GraphGraphPattern graphGraphPattern = (GraphGraphPattern) graphPattern;
            UriRefOrVariable graph = graphGraphPattern.getGraph();
            if (!graph.isVariable()) {
                referredGraphs.add(graph.getResource());
            }
            referredGraphs.addAll(graphGraphPattern.getGroupGraphPattern().getReferredGraphs());
        } else if (graphPattern instanceof AlternativeGraphPattern) {
            List<GroupGraphPattern> alternativeGraphPatterns =
                    ((AlternativeGraphPattern) graphPattern).getAlternativeGraphPatterns();
            for (GroupGraphPattern groupGraphPattern : alternativeGraphPatterns) {
                referredGraphs.addAll(groupGraphPattern.getReferredGraphs());
            }
        } else if (graphPattern instanceof OptionalGraphPattern) {
            GraphPattern mainGraphPattern = ((OptionalGraphPattern) graphPattern).getMainGraphPattern();
            referredGraphs.addAll(getReferredGraphs(mainGraphPattern));
            GroupGraphPattern optionalGraphPattern = ((OptionalGraphPattern) graphPattern).getOptionalGraphPattern();
            referredGraphs.addAll(optionalGraphPattern.getReferredGraphs());
        } else if (graphPattern instanceof MinusGraphPattern) {
            GraphPattern minuendGraphPattern = ((MinusGraphPattern) graphPattern).getMinuendGraphPattern();
            referredGraphs.addAll(getReferredGraphs(minuendGraphPattern));
            GroupGraphPattern subtrahendGraphPattern = ((MinusGraphPattern) graphPattern).getSubtrahendGraphPattern();
            referredGraphs.addAll(subtrahendGraphPattern.getReferredGraphs());
        } else if (graphPattern instanceof GroupGraphPattern) {
            GroupGraphPattern groupGraphPattern = (GroupGraphPattern) graphPattern;
            referredGraphs.addAll(groupGraphPattern.getReferredGraphs());
        }
        return referredGraphs;
    }
}
