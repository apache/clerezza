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
import java.util.Iterator;
import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.RDFTerm;
import org.apache.clerezza.commons.rdf.Triple;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class BNodeCircleTest {

    final static int serverPort = findFreePort();
    static EmbeddedFusekiServer server;

    @BeforeClass
    public static void prepare() throws IOException {
        final String serviceURI = "http://localhost:" + serverPort + "/ds/data";
        final DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(serviceURI);
        final InputStream in = BNodeCircleTest.class.getResourceAsStream("bnode-circle.ttl");
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
    public void graphSize() {
        final Graph graph = new SparqlGraph("http://localhost:" + serverPort + "/ds/query");
        Assert.assertEquals("Graph not of the exepected size", 2, graph.size());
    }

    
    
    @Test
    public void nullFilter() {
        final Graph graph = new SparqlGraph("http://localhost:" + serverPort + "/ds/query");
        final Iterator<Triple> iter = graph.filter(null, null, null);
        Assert.assertTrue(iter.hasNext());
        final Triple triple1 = iter.next();
        final BlankNodeOrIRI subject = triple1.getSubject();
        final RDFTerm object = triple1.getObject();
        Assert.assertTrue(subject instanceof BlankNode);
        Assert.assertTrue(object instanceof BlankNode);
        Assert.assertNotEquals(subject, object);
        Assert.assertTrue(iter.hasNext());
    }
    
    @Test
    public void foafKnowsFilter() {
        final Graph graph = new SparqlGraph("http://localhost:" + serverPort + "/ds/query");
        
        final IRI foafKnows = new IRI("http://xmlns.com/foaf/0.1/knows");

        final Iterator<Triple> iter = graph.filter(null, foafKnows, null);
        Assert.assertTrue(iter.hasNext());
        final Triple triple1 = iter.next();
        final BlankNodeOrIRI subject = triple1.getSubject();
        final RDFTerm object = triple1.getObject();
        Assert.assertTrue(subject instanceof BlankNode);
        Assert.assertTrue(object instanceof BlankNode);
        Assert.assertNotEquals(subject, object);
        Assert.assertTrue(iter.hasNext());
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
