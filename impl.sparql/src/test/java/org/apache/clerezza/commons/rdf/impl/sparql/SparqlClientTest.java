/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.clerezza.commons.rdf.impl.sparql;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import java.io.IOException;
import java.net.ServerSocket;
import org.apache.jena.fuseki.EmbeddedFusekiServer;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class SparqlClientTest {

    final static int serverPort = findFreePort();
    static EmbeddedFusekiServer server;

    @BeforeClass
    public static void prepare() throws IOException {
        final String serviceURI = "http://localhost:" + serverPort + "/ds/data";
        final DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(serviceURI);
        final InputStream in = SparqlClientTest.class.getResourceAsStream("grounded.ttl");
        final Model m = ModelFactory.createDefaultModel();
        String base = "http://example.org/";
        m.read(in, base, "TURTLE");
        server = EmbeddedFusekiServer.memTDB(serverPort, "/ds");//dataSet.getAbsolutePath());
        server.start();
        System.out.println("Started fuseki on port " + serverPort);
        accessor.putModel(m);
    }

    @AfterClass
    public static void cleanup() {
        server.stop();
    }
    
    @Test
    public void select() throws IOException {
        final SparqlClient sparqlClient = new SparqlClient(
                "http://localhost:" + serverPort + "/ds/query");
        List<Map<String, RDFTerm>> result = sparqlClient.queryResultSet(
                "SELECT ?name WHERE { "
                        + "<http://example.org/#spiderman> "
                        + "<http://xmlns.com/foaf/0.1/name> ?name}");
        Assert.assertEquals("There should be two names", 2, result.size());
    }
    
    @Test
    public void ask() throws IOException {
        final SparqlClient sparqlClient = new SparqlClient(
                "http://localhost:" + serverPort + "/ds/query");
        Object result = sparqlClient.queryResult(
                "ASK { "
                        + "<http://example.org/#spiderman> "
                        + "<http://xmlns.com/foaf/0.1/name> ?name}");
        Assert.assertEquals("ASK should result to true", Boolean.TRUE, result);
    }

    @Test
    public void desribe() throws IOException {
        final SparqlClient sparqlClient = new SparqlClient(
                "http://localhost:" + serverPort + "/ds/query");
        Object result = sparqlClient.queryResult(
                "DESCRIBE <http://example.org/#spiderman>");
        Assert.assertTrue("DESCRIBE should return a graph", result instanceof Graph);
    }

    public static int findFreePort() {
        int port = 0;
        try (ServerSocket server = new ServerSocket(0);) {
            port = server.getLocalPort();
        } catch (Exception e) {
            throw new RuntimeException("unable to find a free port");
        }
        return port;
    }

}
