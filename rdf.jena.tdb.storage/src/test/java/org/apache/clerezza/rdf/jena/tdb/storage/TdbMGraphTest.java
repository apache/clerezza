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
package org.apache.clerezza.rdf.jena.tdb.storage;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.test.MGraphTest;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class TdbMGraphTest extends MGraphTest {
    private static File tempFile;
    final private UriRef MGRAPHNAME = new UriRef("http://text.example.org/");
    static private TdbTcProvider tdbTcProvider;

    @BeforeClass
    public static void setupDirectory() throws IOException {
        tempFile = File.createTempFile("tdbtest", null);
        tempFile.delete();
        tempFile.mkdirs();
        tdbTcProvider = new TdbTcProvider(tempFile);
    }

    @AfterClass
    public static void cleanUpDirectory() throws IOException {
        TdbTcProvider.delete(tempFile);
    }

    @After
    public void cleanUpGraph() {
        tdbTcProvider.deleteTripleCollection(MGRAPHNAME);
    }

    @Override
    protected MGraph getEmptyMGraph() {
        return tdbTcProvider.createMGraph(MGRAPHNAME);
    }

    @Test
    public void dateStorage() {
        MGraph graph = getEmptyMGraph();
        Date date = new Date(0);
        LiteralFactory literalFactory = LiteralFactory.getInstance();
        TypedLiteral dateLiteral = literalFactory.createTypedLiteral(date);
        Triple triple = new TripleImpl(new BNode(), new UriRef("http://example.com/property"), dateLiteral);
        graph.add(triple);
        Assert.assertTrue(graph.contains(triple));
    }

    @Test
    public void dateStorage2() {
        MGraph graph = getEmptyMGraph();
        Date date = new Date(0);
        LiteralFactory literalFactory = LiteralFactory.getInstance();
        TypedLiteral dateLiteral = literalFactory.createTypedLiteral(date);
        System.out.println(dateLiteral);
        UriRef property = new UriRef("http://example.com/property");
        Triple triple = new TripleImpl(new BNode(), property, dateLiteral);
        graph.add(triple);

        Triple tripleFromGraph = null;
        Iterator<Triple> propertyTriples = graph.filter(null, property, dateLiteral);
        if (propertyTriples.hasNext()) {
            tripleFromGraph = propertyTriples.next();
        } else {
            Assert.assertTrue(false);
        }
        Assert.assertEquals(dateLiteral, tripleFromGraph.getObject());
    }

}
