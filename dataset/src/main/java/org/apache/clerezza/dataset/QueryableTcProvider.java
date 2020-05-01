/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor  license  agreements.  See the NOTICE file distributed
 * with this work  for  additional  information  regarding  copyright
 * ownership.  The ASF  licenses  this file to you under  the  Apache
 * License, Version 2.0 (the "License"); you may not  use  this  file
 * except in compliance with the License.  You may obtain  a copy  of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless  required  by  applicable law  or  agreed  to  in  writing,
 * software  distributed  under  the  License  is  distributed  on an
 * "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR  CONDITIONS  OF ANY KIND,
 * either  express  or implied.  See  the License  for  the  specific
 * language governing permissions and limitations under  the License.
 */
package org.apache.clerezza.dataset;

import org.apache.clerezza.IRI;

/**
 * Extends the TcProvider interface for providers that support sparql queries.
 */
public interface QueryableTcProvider extends TcProvider {

    /**
     * Executes any sparql query. The type of the result object will vary
     * depending on the type of the query.
     * 
     * @param query
     *            the sparql query to execute
     * @param defaultGraphUri
     *            the default ImmutableGraph against which to execute the query if not
     *            FROM clause is present
     * @return the resulting ResultSet, ImmutableGraph or Boolean value
     */
    public Object executeSparqlQuery(String query, IRI defaultGraphUri);

}
