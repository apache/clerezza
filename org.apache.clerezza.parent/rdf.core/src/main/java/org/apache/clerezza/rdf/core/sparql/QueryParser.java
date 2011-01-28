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
package org.apache.clerezza.rdf.core.sparql;

import java.io.StringReader;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.rdf.core.sparql.query.Query;

/**
 * This class implements an OSGi service to provide a method to parse a
 * SPARQL query and generate a {@link Query} object.
 *
 * @author hasan
 */

@Component
@Service(QueryParser.class)
public class QueryParser {

	private static volatile QueryParser instance;
	public QueryParser() {
		QueryParser.instance = this;
	}

	/**
	 * Returns an instance of this class.
	 * This method is provided due to backward compatibility.
	 */
	public static QueryParser getInstance() {
		if (instance == null) {
			synchronized (QueryParser.class) {
				if (instance == null) {
					new QueryParser();
				}
			}
		}
		return instance;
	}

	/**
	 * Parses a SPARQL query string into a {@link Query} object.
	 *
	 * @param queryString
	 *		SPARQL query string
	 * @return
	 *		{@link Query} object corresponding to the specified query string
	 *
	 * @throws org.apache.clerezza.rdf.core.sparql.ParseException
	 */
	public Query parse(final String queryString) throws ParseException {
		JavaCCGeneratedQueryParser parser = new JavaCCGeneratedQueryParser(
				new StringReader(queryString));
		return parser.parse();
	}
}
