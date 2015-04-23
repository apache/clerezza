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
package org.apache.clerezza.rdf.stable.serializer;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.junit.Assert;
import org.junit.Test;


public class DocumentationStabilityTest {
    
    @Test
    public void RDFTestCases() throws Exception {
        
        Parser parser = Parser.getInstance();
        ImmutableGraph tc1 = parser.parse(
                getClass().getResourceAsStream("documentation-example.nt"), SupportedFormat.N_TRIPLE);
        final Set<String> lines1 = serializeToLines(tc1);
        Graph tc2 = new SimpleGraph();
        tc2.addAll(tc1);
        //add <bundle:///intro> <http://clerezza.org/2009/08/documentation#after> <bundle://org.apache.clerezza.platform.documentation/intro> .
        tc2.add(new TripleImpl(new IRI("bundle:///intro"), 
                new IRI("http://clerezza.org/2009/08/documentation#after"), 
                new IRI("bundle://org.apache.clerezza.platform.documentation/intro")));
        final Set<String> lines2 = serializeToLines(tc2);
        lines2.removeAll(lines1);
        Assert.assertEquals(1, lines2.size());
    }
    
    private Set<String> serializeToLines(Graph tc) throws UnsupportedEncodingException {
        StableSerializerProvider ssp = new StableSerializerProvider();
        final ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        ssp.serialize(os1, tc, SupportedFormat.N_TRIPLE);
        return new HashSet<String>(Arrays.asList(new String(os1.toByteArray(), "utf-8").split("\n")));
        
    }

}
