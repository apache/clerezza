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
package org.apache.clerezza.rdf.jena.sparql;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.sparql.SolutionMapping;
import org.apache.clerezza.rdf.core.sparql.query.Variable;
import org.apache.clerezza.rdf.jena.commons.Jena2TriaUtil;

/**
 *
 * @author rbn
 */
class HashMapSolutionMapping extends HashMap<Variable, Resource> implements SolutionMapping {

	Jena2TriaUtil convertor = new Jena2TriaUtil(new HashMap<Node,BNode>());
	public HashMapSolutionMapping(QuerySolution querySolution) {
		final Iterator<String> varNames = querySolution.varNames();
		while (varNames.hasNext()) {
			final String varName = varNames.next();
			put(new Variable(varName), toResource(querySolution.get(varName)));
		}
	}
	@Override
	public Resource get(String name) {
		return get(new Variable(name));
	}

	private Resource toResource(RDFNode node) {
		return convertor.convertJenaNode2Resource(node.asNode());
	}
}
