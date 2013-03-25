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

import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.sparql.query.DataSet;
import org.apache.clerezza.rdf.core.sparql.query.GroupGraphPattern;
import org.apache.clerezza.rdf.core.sparql.query.InlineData;
import org.apache.clerezza.rdf.core.sparql.query.Query;

/**
 *
 * @author hasan
 */
public abstract class SimpleQuery implements Query {
	private SimpleDataSet dataSet = null;
	private GroupGraphPattern queryPattern = null;
    private InlineData inlineData = null;

	@Override
	public DataSet getDataSet() {
		return dataSet;
	}

	@Override
	public GroupGraphPattern getQueryPattern() {
		return queryPattern;
    }

    @Override
    public InlineData getInlineData() {
        return inlineData;
    }

    public void addDefaultGraph(UriRef defaultGraph) {
		if (dataSet == null) {
			dataSet = new SimpleDataSet();
		}
		dataSet.addDefaultGraph(defaultGraph);
	}

	public void addNamedGraph(UriRef namedGraph) {
		if (dataSet == null) {
			dataSet = new SimpleDataSet();
		}
		dataSet.addNamedGraph(namedGraph);
	}

	public void setQueryPattern(GroupGraphPattern queryPattern) {
		this.queryPattern = queryPattern;
	}

    public void setInlineData(InlineData inlineData) {
        this.inlineData = inlineData;
    }

    @Override
	public abstract String toString();
}
