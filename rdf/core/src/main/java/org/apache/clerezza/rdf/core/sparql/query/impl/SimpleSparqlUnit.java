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

import org.apache.clerezza.rdf.core.sparql.query.Query;
import org.apache.clerezza.rdf.core.sparql.query.SparqlUnit;
import org.apache.clerezza.rdf.core.sparql.update.Update;

/**
 *
 * @author hasan
 */
public class SimpleSparqlUnit implements SparqlUnit {

    private final Query query;
	private final Update update;

	public SimpleSparqlUnit(Query query) {
		if (query == null) {
			throw new IllegalArgumentException("Invalid query: null");
		}
		this.query = query;
		update = null;
	}

	public SimpleSparqlUnit(Update update) {
		if (update == null) {
			throw new IllegalArgumentException("Invalid update: null");
		}
		this.update = update;
		query = null;
	}


    @Override
    public boolean isQuery() {
        return update == null;
    }

    @Override
    public Query getQuery() {
        return query;
    }

    @Override
    public Update getUpdate() {
        return update;
    }
}
