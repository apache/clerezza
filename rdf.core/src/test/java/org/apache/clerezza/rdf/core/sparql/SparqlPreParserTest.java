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

import java.util.Set;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.sparql.update.Update;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author hasan
 */
public class SparqlPreParserTest {

    @Test
    public void testDefaultGraphInSelectQuery() throws ParseException {

        StringBuilder queryStrBuilder = new StringBuilder();
        queryStrBuilder.append(
                "PREFIX : <http://example/>\n" +
                "SELECT ?x \n" +
                "{\n" +
                ":order :item/:price ?x\n" +
                "}\n");

        UriRef defaultGraph = new UriRef("http://example.org/default");
        SparqlPreParser parser = new SparqlPreParser();
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStrBuilder.toString(), defaultGraph);
        Assert.assertTrue(referredGraphs == null);
    }

    @Test
    public void testSelectQuery() throws ParseException {

        StringBuilder queryStrBuilder = new StringBuilder();
        queryStrBuilder.append(
                "PREFIX : <http://example/>\n" +
                "SELECT ?x (foo(2*3, ?x < ?y) AS ?f) (GROUP_CONCAT(?x ; separator=\"|\") AS ?gc) (sum(distinct *) AS ?total)\n" +
                "FROM <http://example.org/test>\n" +
                "{\n" +
                ":order :item/:price ?x\n" +
                "}\n");

        UriRef defaultGraph = new UriRef("http://example.org/default");
        SparqlPreParser parser = new SparqlPreParser();
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStrBuilder.toString(), defaultGraph);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(new UriRef("http://example.org/test")));
    }
}
