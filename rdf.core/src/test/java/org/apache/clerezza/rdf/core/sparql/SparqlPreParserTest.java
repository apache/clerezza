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

import java.util.HashSet;
import java.util.Set;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author hasan
 */
public class SparqlPreParserTest {

    private final static UriRef DEFAULT_GRAPH = new UriRef("http://example.org/default.graph"); 
    private final static UriRef NAMED_GRAPH = new UriRef("http://example.org/dummy.graph"); 
    private final static UriRef TEST_GRAPH = new UriRef("http://example.org/test.graph"); 

    class MyTcManager extends TcManager {
        @Override
        public Set<UriRef> listTripleCollections() {
            Set<UriRef> result = new HashSet<UriRef>();
            result.add(NAMED_GRAPH);
            return result;
        }
    }

    @Test
    public void testDefaultGraphInSelectQuery() throws ParseException {

        StringBuilder queryStrBuilder = new StringBuilder();
        queryStrBuilder.append(
                "PREFIX : <http://example.org/>\n" +
                "SELECT ?x \n" +
                "{\n" +
                ":order :item/:price ?x\n" +
                "}\n");

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStrBuilder.toString(), DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(DEFAULT_GRAPH));
    }

    @Test
    public void testAllGraphReferenceInSelectQuery() throws ParseException {

        StringBuilder queryStrBuilder = new StringBuilder();
        queryStrBuilder.append("SELECT DISTINCT ?g { GRAPH ?g { ?s ?p ?o } }\n");

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStrBuilder.toString(), DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs == null);
    }

    @Test
    public void testSelectQuery() throws ParseException {

        StringBuilder queryStrBuilder = new StringBuilder();
        queryStrBuilder.append(
                "PREFIX : <http://example.org/>\n" +
                "SELECT ?x (foo(2*3, ?x < ?y) AS ?f) (GROUP_CONCAT(?x ; separator=\"|\") AS ?gc) (sum(distinct *) AS ?total)\n" +
                "FROM " + TEST_GRAPH.toString() + "\n" +
                "{\n" +
                ":order :item/:price ?x\n" +
                "}\n");

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStrBuilder.toString(), DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(TEST_GRAPH));
    }

    @Test
    public void testLoadingToDefaultGraph() throws ParseException {

        String queryStr = "LOAD SILENT <http://example.org/mydata>";

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(DEFAULT_GRAPH));
    }

    @Test
    public void testLoadingToGraph() throws ParseException {

        String queryStr = "LOAD SILENT <http://example.org/mydata> INTO GRAPH " + TEST_GRAPH.toString();

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(TEST_GRAPH));
    }

    @Test
    public void testClearingDefaultGraph() throws ParseException {

        String queryStr = "CLEAR SILENT DEFAULT";

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(DEFAULT_GRAPH));
    }

    @Test
    public void testClearingNamedGraph() throws ParseException {

        String queryStr = "CLEAR SILENT NAMED";

        SparqlPreParser parser;
        parser = new SparqlPreParser(new MyTcManager());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(NAMED_GRAPH));
    }

    @Test
    public void testClearingGraph() throws ParseException {

        String queryStr = "CLEAR SILENT GRAPH " + TEST_GRAPH.toString();

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(TEST_GRAPH));
    }

    @Test
    public void testDroppingDefaultGraph() throws ParseException {

        String queryStr = "DROP SILENT DEFAULT";

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(DEFAULT_GRAPH));
    }

    @Test
    public void testDroppingNamedGraph() throws ParseException {

        String queryStr = "DROP SILENT NAMED";

        SparqlPreParser parser;
        parser = new SparqlPreParser(new MyTcManager());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(NAMED_GRAPH));
    }

    @Test
    public void testDroppingGraph() throws ParseException {

        String queryStr = "DROP SILENT GRAPH " + TEST_GRAPH.toString();

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(TEST_GRAPH));
    }

    @Test
    public void testCreatingGraph() throws ParseException {

        String queryStr = "CREATE SILENT GRAPH " + TEST_GRAPH.toString();

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(TEST_GRAPH));
    }

    @Test
    public void testAddingTriplesFromDefaultGraphToNamedGraph() throws ParseException {

        String queryStr = "ADD SILENT DEFAULT TO GRAPH " + TEST_GRAPH.toString();

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Set<UriRef> expected = new HashSet<UriRef>();
        expected.add(DEFAULT_GRAPH);
        expected.add(TEST_GRAPH);
        Assert.assertTrue(referredGraphs.containsAll(expected));
    }

    @Test
    public void testAddingTriplesFromNamedGraphToDefaultGraph() throws ParseException {

        String queryStr = "ADD SILENT GRAPH " + TEST_GRAPH.toString() + " TO DEFAULT";

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Set<UriRef> expected = new HashSet<UriRef>();
        expected.add(DEFAULT_GRAPH);
        expected.add(TEST_GRAPH);
        Assert.assertTrue(referredGraphs.containsAll(expected));
    }

    @Test
    public void testMovingTriplesFromDefaultGraphToNamedGraph() throws ParseException {

        String queryStr = "MOVE SILENT DEFAULT TO GRAPH " + TEST_GRAPH.toString();

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Set<UriRef> expected = new HashSet<UriRef>();
        expected.add(DEFAULT_GRAPH);
        expected.add(TEST_GRAPH);
        Assert.assertTrue(referredGraphs.containsAll(expected));
    }

    @Test
    public void testMovingTriplesFromNamedGraphToDefaultGraph() throws ParseException {

        String queryStr = "MOVE SILENT GRAPH " + TEST_GRAPH.toString() + " TO DEFAULT";

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Set<UriRef> expected = new HashSet<UriRef>();
        expected.add(DEFAULT_GRAPH);
        expected.add(TEST_GRAPH);
        Assert.assertTrue(referredGraphs.containsAll(expected));
    }

    @Test
    public void testCopyingTriplesFromDefaultGraphToNamedGraph() throws ParseException {

        String queryStr = "COPY SILENT DEFAULT TO GRAPH " + TEST_GRAPH.toString();

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Set<UriRef> expected = new HashSet<UriRef>();
        expected.add(DEFAULT_GRAPH);
        expected.add(TEST_GRAPH);
        Assert.assertTrue(referredGraphs.containsAll(expected));
    }

    @Test
    public void testCopyingTriplesFromNamedGraphToDefaultGraph() throws ParseException {

        String queryStr = "COPY SILENT GRAPH " + TEST_GRAPH.toString() + " TO DEFAULT";

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Set<UriRef> expected = new HashSet<UriRef>();
        expected.add(DEFAULT_GRAPH);
        expected.add(TEST_GRAPH);
        Assert.assertTrue(referredGraphs.containsAll(expected));
    }

    @Test
    public void testInsertDataToDefaultGraph() throws ParseException {

        String queryStr = "PREFIX dc: <http://purl.org/dc/elements/1.1/> INSERT DATA { \n" +
                "<http://example/book1> dc:title \"A new book\" ; dc:creator \"A.N.Other\" . }";

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(DEFAULT_GRAPH));
    }

    @Test
    public void testInsertDataToNamedGraph() throws ParseException {

        String queryStr = "PREFIX ns: <http://example.org/ns#>\n" +
                "INSERT DATA { GRAPH " + TEST_GRAPH.toString() + " { <http://example/book1>  ns:price  42 } }";
        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(TEST_GRAPH));
    }

    @Test
    public void testDeleteDataInDefaultGraph() throws ParseException {

        String queryStr = "PREFIX dc: <http://purl.org/dc/elements/1.1/> DELETE DATA { \n" +
                "<http://example/book1> dc:title \"A new book\" ; dc:creator \"A.N.Other\" . }";

        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(DEFAULT_GRAPH));
    }

    @Test
    public void testDeleteDataInNamedGraph() throws ParseException {

        String queryStr = "PREFIX ns: <http://example.org/ns#>\n" +
                "DELETE DATA { GRAPH " + TEST_GRAPH.toString() + " { <http://example/book1>  ns:price  42 } }";
        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);
        Assert.assertTrue(referredGraphs.toArray()[0].equals(TEST_GRAPH));
    }

    @Test
    public void testInsertAndDeleteData() throws ParseException {

        String queryStr = "PREFIX ns: <http://example.org/ns#> " +
                "INSERT DATA { <http://example/book1>  ns:price  42 }; " +
                "DELETE DATA { GRAPH " + TEST_GRAPH.toString() + " { <http://example/book1>  ns:price  42 } }";
        SparqlPreParser parser;
        parser = new SparqlPreParser(TcManager.getInstance());
        Set<UriRef> referredGraphs = parser.getReferredGraphs(queryStr, DEFAULT_GRAPH);

        Set<UriRef> expected = new HashSet<UriRef>();
        expected.add(DEFAULT_GRAPH);
        expected.add(TEST_GRAPH);
        Assert.assertTrue(referredGraphs.containsAll(expected));
    }
}
