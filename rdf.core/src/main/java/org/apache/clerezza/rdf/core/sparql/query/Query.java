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
package org.apache.clerezza.rdf.core.sparql.query;

/**
 * <p>This interface represents a SPARQL query.</p>
 * <p>There are four types of SPARQL queries: {@link SelectQuery},
 * {@link ConstructQuery}, {@link DescribeQuery}, and {@link AskQuery}.</p>
 *
 * @author hasan
 */
public interface Query {

    /**
     * <p>Gets {@link DataSet} containing the specification of the default
     * graph and named graphs, if any.</p>
     * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#specifyingDataset">
     * SPARQL Query Language: 8.2 Specifying RDF Datasets</a>
     * @return
     *        null if no data set is specified, indicating the use of
     *        system default graph. Otherwise a {@link DataSet} object is returned.
     */
    public DataSet getDataSet();

    /**
     * <p>Gets the query pattern of the WHERE clause for the query.</p>
     * @see <a href="http://www.w3.org/TR/rdf-sparql-query/#GraphPattern">
     * SPARQL Query Language: 5 Graph Patterns</a>
     * @return
     *        the {@link GroupGraphPattern} of the WHERE clause for this query.
     *        If the WHERE clause is not specified, null is returned.
     */
    public GroupGraphPattern getQueryPattern();
    
    public InlineData getInlineData();

    /**
     * 
     * @return A valid String representation of the query.
     */
    @Override
    public abstract String toString();
}
