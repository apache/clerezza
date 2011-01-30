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

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.clerezza.rdf.core.sparql.SolutionMapping;

/**
 *
 * @author rbn
 */
class ResultSetWrapper implements org.apache.clerezza.rdf.core.sparql.ResultSet {

	private final Iterator<QuerySolution> solutionsIter;


	public ResultSetWrapper(final ResultSet jenaResultSet) {
		final List<QuerySolution> solutions = new ArrayList<QuerySolution>();
		while (jenaResultSet.hasNext()) {
			solutions.add(jenaResultSet.nextSolution());
		}
		solutionsIter = solutions.iterator();
	}

	@Override
	public boolean hasNext() {
		return solutionsIter.hasNext();
	}

	@Override
	public SolutionMapping next() {
		return new HashMapSolutionMapping(solutionsIter.next());
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
