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

import java.util.List;
import java.util.Set;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.sparql.StringQuerySerializer;
import org.apache.clerezza.rdf.core.sparql.query.AlternativeGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.AskQuery;
import org.apache.clerezza.rdf.core.sparql.query.BasicGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.BinaryOperation;
import org.apache.clerezza.rdf.core.sparql.query.BuiltInCall;
import org.apache.clerezza.rdf.core.sparql.query.ConstructQuery;
import org.apache.clerezza.rdf.core.sparql.query.DataSet;
import org.apache.clerezza.rdf.core.sparql.query.DescribeQuery;
import org.apache.clerezza.rdf.core.sparql.query.Expression;
import org.apache.clerezza.rdf.core.sparql.query.FunctionCall;
import org.apache.clerezza.rdf.core.sparql.query.GraphGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.GraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.GroupGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.LiteralExpression;
import org.apache.clerezza.rdf.core.sparql.query.OptionalGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.OrderCondition;
import org.apache.clerezza.rdf.core.sparql.query.ResourceOrVariable;
import org.apache.clerezza.rdf.core.sparql.query.SelectQuery;
import org.apache.clerezza.rdf.core.sparql.query.TriplePattern;
import org.apache.clerezza.rdf.core.sparql.query.UnaryOperation;
import org.apache.clerezza.rdf.core.sparql.query.UriRefExpression;
import org.apache.clerezza.rdf.core.sparql.query.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements abstract methods of {@link StringQuerySerializer}
 * to serialize specific {@link Query} types.
 *
 * @author hasan
 */
public class SimpleStringQuerySerializer extends StringQuerySerializer {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public String serialize(SelectQuery selectQuery) {
		StringBuffer s = new StringBuffer("SELECT ");
		if (selectQuery.isDistinct()) {
			s.append("DISTINCT\n");
		}
		if (selectQuery.isReduced()) {
			s.append("REDUCED\n");
		}
		if (selectQuery.isSelectAll()) {
			s.append("*");
		} else {
			for (Variable v : selectQuery.getSelection()) {
				appendVariable(s, v);
				s.append(" ");
			}
		}
		s.append("\n");

		appendDataSet(s, (SimpleQuery) selectQuery);
		appendWhere(s, (SimpleQuery) selectQuery);
		appendModifier(s, (SimpleQueryWithSolutionModifier) selectQuery);

		return s.toString();
	}

	private void appendVariable(StringBuffer s, Variable v) {
		s.append("?").append(v.getName());
	}

	private void appendDataSet(StringBuffer s, SimpleQuery q) {
		DataSet dataSet = q.getDataSet();
		if (dataSet != null) {
			for (UriRef dg : dataSet.getDefaultGraphs()) {
				s.append("FROM ").append(dg.toString()).append("\n");
			}
			for (UriRef ng : dataSet.getNamedGraphs()) {
				s.append("FROM NAMED ").append(ng.toString()).append("\n");
			}
		}
	}

	private void appendWhere(StringBuffer s, SimpleQuery q) {
		GroupGraphPattern queryPattern = q.getQueryPattern();
		if (queryPattern == null) {
			return;
		}
		s.append("WHERE\n");
		appendGroupGraphPattern(s, q.getQueryPattern());
	}

	private void appendGroupGraphPattern(StringBuffer s,
			GroupGraphPattern groupGraphPattern) {

		s.append("{ ");
		for (GraphPattern graphPattern : groupGraphPattern.getGraphPatterns()) {
			appendGraphPattern(s, graphPattern);
		}
		for (Expression e : groupGraphPattern.getFilter()) {
			boolean brackettedExpr = !((e instanceof BuiltInCall)
					|| (e instanceof FunctionCall));
			s.append("FILTER ");
			if (brackettedExpr) {
				s.append("(");
			}
			appendExpression(s, e);
			if (brackettedExpr) {
				s.append(")");
			}
			s.append("\n");
		}
		s.append("} ");
	}

	private void appendGraphPattern(StringBuffer s, GraphPattern graphPattern) {
		if (graphPattern instanceof BasicGraphPattern) {
			appendTriplePatterns(s,
					((BasicGraphPattern) graphPattern).getTriplePatterns());
		} else if (graphPattern instanceof GroupGraphPattern) {
			appendGroupGraphPattern(s, (GroupGraphPattern) graphPattern);
		} else if (graphPattern instanceof OptionalGraphPattern) {
			appendGraphPattern(s,
					((OptionalGraphPattern) graphPattern).getMainGraphPattern());
			s.append(" OPTIONAL ");
			appendGroupGraphPattern(s,
					((OptionalGraphPattern) graphPattern).getOptionalGraphPattern());
		} else if (graphPattern instanceof AlternativeGraphPattern) {
			List<GroupGraphPattern> alternativeGraphPatterns =
					((AlternativeGraphPattern) graphPattern).getAlternativeGraphPatterns();
			if ((alternativeGraphPatterns != null) &&
					(!alternativeGraphPatterns.isEmpty())) {
				appendGroupGraphPattern(s, alternativeGraphPatterns.get(0));
				int size = alternativeGraphPatterns.size();
				int i = 1;
				while (i < size) {
					s.append(" UNION ");
					appendGroupGraphPattern(s, alternativeGraphPatterns.get(i));
					i++;
				}
			}
		} else if (graphPattern instanceof GraphGraphPattern) {
			s.append("GRAPH ");
			appendResourceOrVariable(s, ((GraphGraphPattern) graphPattern).getGraph());
			s.append(" ");
			appendGroupGraphPattern(s, ((GraphGraphPattern) graphPattern).getGroupGraphPattern());
		} else {
			logger.warn("Unsupported GraphPattern {}", graphPattern.getClass());
		}
	}

	private void appendTriplePatterns(StringBuffer s,
			Set<TriplePattern> triplePatterns) {

		for (TriplePattern p : triplePatterns) {
			appendResourceOrVariable(s, p.getSubject());
			s.append(" ");
			appendResourceOrVariable(s, p.getPredicate());
			s.append(" ");
			appendResourceOrVariable(s, p.getObject());
			s.append(" .\n");
		}
	}

	private void appendResourceOrVariable(StringBuffer s, ResourceOrVariable n) {
		if (n.isVariable()) {
			appendVariable(s, n.getVariable());
		} else {
			Resource r = n.getResource();
			if (r instanceof BNode) {
				s.append("_:").append(r.toString().replace("@", "."));
			} else {
				s.append(r.toString());
			}
		}
	}

	private void appendExpression(StringBuffer s, Expression e) {
		if (e instanceof Variable) {
			appendVariable(s, (Variable) e);
		} else if (e instanceof BinaryOperation) {
			BinaryOperation bo = (BinaryOperation) e;
			s.append("(");
			appendExpression(s, bo.getLhsOperand());
			s.append(") ").append(bo.getOperatorString()).append(" (");
			appendExpression(s, bo.getRhsOperand());
			s.append(")");
		} else if (e instanceof UnaryOperation) {
			UnaryOperation uo = (UnaryOperation) e;
			s.append(uo.getOperatorString()).append(" (");
			appendExpression(s, uo.getOperand());
			s.append(")");
		} else if (e instanceof BuiltInCall) {
			BuiltInCall b = (BuiltInCall) e;
			appendCall(s, b.getName(), b.getArguements());
		} else if (e instanceof FunctionCall) {
			FunctionCall f = (FunctionCall) e;
			appendCall(s, f.getName().getUnicodeString(), f.getArguements());
		} else if (e instanceof LiteralExpression) {
			appendLiteralExpression(s, (LiteralExpression) e);
		} else if (e instanceof UriRefExpression) {
			s.append(((UriRefExpression) e).getUriRef().toString());
		}
	}

	private void appendCall(StringBuffer s, String name, List<Expression> expr) {
		s.append(name).append("(");
		for (Expression e : expr) {
			appendExpression(s, e);
			s.append(",");
		}
		if (expr.isEmpty()) {
			s.append(")");
		} else {
			s.setCharAt(s.length()-1, ')');
		}
	}

	private void appendLiteralExpression(StringBuffer s, LiteralExpression le) {
		s.append(le.getLiteral().toString());
	}

	private void appendModifier(StringBuffer s, SimpleQueryWithSolutionModifier q) {
		List<OrderCondition> orderConditions = q.getOrderConditions();
		if ((orderConditions != null) && (!orderConditions.isEmpty())) {
			s.append("ORDER BY ");
			for (OrderCondition oc : orderConditions) {
				appendOrderCondition(s, oc);
				s.append("\n");
			}
		}
		if (q.getOffset() > 0) {
			s.append("OFFSET ").append(q.getOffset()).append("\n");
		}
		if (q.getLimit() >= 0) {
			s.append("LIMIT ").append(q.getLimit()).append("\n");
		}
	}

	private void appendOrderCondition(StringBuffer s, OrderCondition oc) {
		if (!oc.isAscending()) {
			s.append("DESC(");
		}
		appendExpression(s, oc.getExpression());
		if (!oc.isAscending()) {
			s.append(")");
		}
		s.append(" ");
	}

	@Override
	public String serialize(ConstructQuery constructQuery) {
		StringBuffer s = new StringBuffer("CONSTRUCT\n");
		Set<TriplePattern> triplePatterns = constructQuery.getConstructTemplate();
		s.append("{ ");
		if (triplePatterns != null && !triplePatterns.isEmpty()) {
			appendTriplePatterns(s, triplePatterns);
		}
		s.append("}\n");

		appendDataSet(s, (SimpleQuery) constructQuery);
		appendWhere(s, (SimpleQuery) constructQuery);
		appendModifier(s, (SimpleQueryWithSolutionModifier) constructQuery);

		return s.toString();
	}

	@Override
	public String serialize(DescribeQuery describeQuery) {
		StringBuffer s = new StringBuffer("DESCRIBE\n");

		if (describeQuery.isDescribeAll()) {
			s.append("*");
		} else {
			for (ResourceOrVariable n : describeQuery.getResourcesToDescribe()) {
				appendResourceOrVariable(s, n);
				s.append(" ");
			}
		}
		appendDataSet(s, (SimpleQuery) describeQuery);
		appendWhere(s, (SimpleQuery) describeQuery);
		appendModifier(s, (SimpleQueryWithSolutionModifier) describeQuery);

		return s.toString();
	}

	@Override
	public String serialize(AskQuery askQuery) {
		StringBuffer s = new StringBuffer("ASK\n");
		appendDataSet(s, (SimpleQuery) askQuery);
		appendWhere(s, (SimpleQuery) askQuery);

		return s.toString();
	}
}
