/*
 *  Copyright 2010 mir.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.apache.clerezza.rdf.web.core.utils;

import java.util.Iterator;
import java.util.Set;
import org.apache.clerezza.rdf.core.sparql.ResultSet;
import org.apache.clerezza.rdf.core.sparql.SolutionMapping;

/**
 * Wrapps a set of <code>ResultSet</code>s so it acts like a single ResultSet.
 *
 * @author mir
 */
public class ResultSetsWrapper implements ResultSet {

	private Iterator<ResultSet> resultSetsIter;
	private ResultSet currentResultSet;

	public ResultSetsWrapper(Set<ResultSet> resultSets) {
		this.resultSetsIter = resultSets.iterator();
		currentResultSet = resultSetsIter.next();
	}

	@Override
	public boolean hasNext() {
		if (currentResultSet.hasNext()) {
			return true;
		} else {
			if (resultSetsIter.hasNext()) {
				currentResultSet = resultSetsIter.next();
				return hasNext();
			}
		}
		return false;
	}

	@Override
	public SolutionMapping next() {
		hasNext();
		return currentResultSet.next();
	}

	@Override
	public void remove() {
		currentResultSet.remove();
	}
}
