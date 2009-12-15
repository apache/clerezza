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

import org.apache.clerezza.rdf.core.sparql.query.AskQuery;
import org.apache.clerezza.rdf.core.sparql.query.Query;
import org.apache.clerezza.rdf.core.sparql.query.ConstructQuery;
import org.apache.clerezza.rdf.core.sparql.query.DescribeQuery;
import org.apache.clerezza.rdf.core.sparql.query.SelectQuery;

/**
 * This abstract class provides a method to generate a {@link String}
 * representation of a {@link Query}.
 *
 * @author hasan
 */
public abstract class StringQuerySerializer {

	/**
	 * Serializes a {@link Query} object to a {@link String}.
	 *
	 * @param query
	 *		the Query object to be serialized
	 * @return
	 *		a String representation of the specified Query object.
	 */
	public String serialize(Query query) {
		if (query instanceof SelectQuery) {
			return serialize((SelectQuery) query);
		} else if (query instanceof ConstructQuery) {
			return serialize((ConstructQuery) query);
		} else if (query instanceof DescribeQuery) {
			return serialize((DescribeQuery) query);
		} else {
			return serialize((AskQuery) query);
		}
	}

	/**
	 * Serializes a {@link SelectQuery} object to a {@link String}.
	 *
	 * @param selectQuery
	 *		the SelectQuery object to be serialized
	 * @return
	 *		a String representation of the specified SelectQuery object.
	 */
	public abstract String serialize(SelectQuery selectQuery);

	/**
	 * Serializes a {@link ConstructQuery} object to a {@link String}.
	 *
	 * @param constructQuery
	 *		the ConstructQuery object to be serialized
	 * @return
	 *		a String representation of the specified ConstructQuery object.
	 */
	public abstract String serialize(ConstructQuery constructQuery);

	/**
	 * Serializes a {@link DescribeQuery} object to a {@link String}.
	 *
	 * @param describeQuery
	 *		the DescribeQuery object to be serialized
	 * @return
	 *		a String representation of the specified DescribeQuery object.
	 */
	public abstract String serialize(DescribeQuery describeQuery);

	/**
	 * Serializes an {@link AskQuery} object to a {@link String}.
	 *
	 * @param askQuery
	 *		the AskQuery object to be serialized
	 * @return
	 *		a String representation of the specified AskQuery object.
	 */
	public abstract String serialize(AskQuery askQuery);
}
