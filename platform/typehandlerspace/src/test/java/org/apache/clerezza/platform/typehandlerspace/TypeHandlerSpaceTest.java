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
package org.apache.clerezza.platform.typehandlerspace;

import java.io.IOException;
import org.junit.Test;

/**
 * @author mir
 *
 */
public class TypeHandlerSpaceTest {
    
    @Test
    public void dummy() throws IOException {
    }
    
//    
//    private static LockableGraph mGraph = new LockableGraphWrapper(new SimpleGraph());
//
//    IRI myType = new IRI("org.example/myType");
//    
//    @Path("/myTypeHandler")
//    public static class MyTypeHandler {
//
//        @GET
//        public String handleGet() {
//            return "handleGet";
//        }
//        
//        @PUT
//        @POST
//        public String handlePut() {
//            return "handlePut";
//        }
//    }
//    
//    @Path("/myTypeHandler2")
//    public static class MyTypeHandler2 {
//
//        @GET
//        public String handleGet() {
//            return "handleGet2";
//        }
//        
//        @PUT
//        public String handlePut() {
//            return "handlePut2";
//        }
//    }
//    
//    /**
//     * Tests if the correct method of the correct TypeHandler is called,
//     * when using the http-method PUT.
//     */
//    @Test
//    public void testPut() throws IOException {
//        
//        int port = createTestWebServer().getPort();
//
//        IRI uri = new IRI("http://localhost:" + port + "/test");
//        // Setup mGraph
//        Triple triple = new TripleImpl(uri, RDF.type, myType);
//        mGraph.add(triple);
//
//        URL serverURL = new URL(uri.getUnicodeString());
//        HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
//        connection.setDoOutput(true);
//        connection.setRequestMethod("PUT");
//        connection.addRequestProperty("Content-type", "application/x-test");
//        //java sends and invalid *; q=.2 in the default accept-header
//        connection.addRequestProperty("Accept", "*/*; q=.2");
//        OutputStream requestStream = connection.getOutputStream();
//        byte[] putData = {1, 6, 8};
//        requestStream.write(putData);
//        requestStream.flush();
//        //now we actually open the connection
//        connection.connect();
//        
//        InputStream requestInput = connection.getInputStream();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        for (int ch = requestInput.read(); ch != -1; ch = requestInput.read()) {
//            baos.write(ch);
//        }
//        Assert.assertEquals("handlePut", baos.toString());
//    }
//    
//    /**
//     * Tests if the correct method of the correct TypeHandler is called,
//     * when using the http-method GET.
//     */
//    @Test
//    public void testGet() throws IOException {
//        
//        int port = createTestWebServer().getPort();        
//        IRI uri = new IRI("http://localhost:" + port + "/test");
//        
//        // Setup mGraph
//        Triple triple = new TripleImpl(uri, RDF.type, myType);
//        mGraph.add(triple);
//        
//        URL serverURL = new URL(uri.getUnicodeString());
//        HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
//        
//        connection = (HttpURLConnection) serverURL.openConnection();
//        connection.setRequestMethod("GET");
//
//        connection.addRequestProperty("Accept", "text/html, */*; q=.2");
//        InputStream requestInput = connection.getInputStream();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        for (int ch = requestInput.read(); ch != -1; ch = requestInput.read()) {
//            baos.write(ch);
//        }
//        Assert.assertEquals("handleGet", baos.toString());
//    }
//    
//    /**
//     * Tests if the correct description of the resource is returned when 
//     * request with "-description" appended to the URL.
//     */
//    @Test
//    public void testGetDescription() throws IOException {
//        
//        int port = createTestWebServer().getPort();
//        IRI uri = new IRI("http://localhost:" + port + "/test");
//        
//        // Setup mGraph
//        Triple triple = new TripleImpl(uri, RDF.type, myType);
//        mGraph.add(triple);
//
//        URL serverURL = new URL(uri.getUnicodeString() + "-description");
//        HttpURLConnection connection = (HttpURLConnection) serverURL.openConnection();
//        
//        connection = (HttpURLConnection) serverURL.openConnection();
//        connection.setRequestMethod("GET");
//
//        connection.addRequestProperty("Accept", "text/html, */*; q=.2");
//        InputStream requestInput = connection.getInputStream();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        for (int ch = requestInput.read(); ch != -1; ch = requestInput.read()) {
//            baos.write(ch);
//        }
//        Assert.assertEquals("ImmutableGraph writer", baos.toString());
//    }
//
//    private TestWebServer createTestWebServer() {
//        return new TestWebServer(new Application() {
//
//            @Override
//            public Set<Class<?>> getClasses() {
//                Set<Class<?>> result = new HashSet<Class<?>>();
//                return result;
//            }
//
//            @Override
//            public Set<Object> getSingletons() {
//                Set<Object> result = new HashSet<Object>();
//                final MyTypeHandler typeHandler = new MyTypeHandler();
//                final MyTypeHandler2 fallbackHandler = new MyTypeHandler2();
//                
//                TypeHandlerSpace typeHandlerSpace = new TypeHandlerSpace();
//                typeHandlerSpace.tcManager = new TcManager(){
//                    @Override
//                    public LockableGraph getGraph(IRI name) {
//                        return new LockableGraph() {
//
//                            @Override
//                            public ReadWriteLock getLock() {
//                                return mGraph.getLock();
//                            }
//
//                            @Override
//                            public ImmutableGraph getGraph() {
//                                return mGraph.getGraph();
//                            }
//
//                            @Override
//                            public Iterator<Triple> filter(BlankNodeOrIRI subject, IRI predicate, RDFTerm object) {
//                                return mGraph.filter(subject, predicate, object);
//                            }
//
//                            @Override
//                            public int size() {
//                                return mGraph.size();
//                            }
//
//                            @Override
//                            public boolean isEmpty() {
//                                return mGraph.isEmpty();
//                            }
//
//                            @Override
//                            public boolean contains(Object o) {
//                                return mGraph.contains(o);
//                            }
//
//                            @Override
//                            public Iterator<Triple> iterator() {
//                                return mGraph.iterator();
//                            }
//
//                            @Override
//                            public Object[] toArray() {
//                                throw new UnsupportedOperationException("Not supported yet.");
//                            }
//
//                            @Override
//                            public <T> T[] toArray(T[] a) {
//                                throw new UnsupportedOperationException("Not supported yet.");
//                            }
//
//                            @Override
//                            public boolean add(Triple e) {
//                                return mGraph.add(e);
//                            }
//
//                            @Override
//                            public boolean remove(Object o) {
//                                return mGraph.remove(o);
//                            }
//
//                            @Override
//                            public boolean containsAll(Collection<?> c) {
//                                return mGraph.containsAll(c);
//                            }
//
//                            @Override
//                            public boolean addAll(Collection<? extends Triple> c) {
//                                return mGraph.addAll(c);
//                            }
//
//                            @Override
//                            public boolean removeAll(Collection<?> c) {
//                                return mGraph.removeAll(c);
//                            }
//
//                            @Override
//                            public boolean retainAll(Collection<?> c) {
//                                return mGraph.retainAll(c);
//                            }
//
//                            @Override
//                            public void clear() {
//                                mGraph.clear();
//                            }
//
//                            @Override
//                            public void addGraphListener(GraphListener listener, FilterTriple filter, long delay) {
//                                throw new UnsupportedOperationException("Not supported yet.");
//                            }
//
//                            @Override
//                            public void addGraphListener(GraphListener listener, FilterTriple filter) {
//                                throw new UnsupportedOperationException("Not supported yet.");
//                            }
//
//                            @Override
//                            public void removeGraphListener(GraphListener listener) {
//                                throw new UnsupportedOperationException("Not supported yet.");
//                            }
//                        };
//                    }
//                };
//                
//                typeHandlerSpace.typeHandlerDiscovery = new TypeHandlerDiscovery() {
//
//                    @Override
//                    public Object getTypeHandler(Set<IRI> rdfTypes) {
//                        if (rdfTypes.contains(myType)){
//                            return typeHandler;
//                        }
//                        return fallbackHandler;
//                    }
//                };
//                result.add(typeHandlerSpace);
//                result.add(typeHandler);
//                result.add(new GraphWriterDummy());
//                return result;
//            }
//        });
//    }
//
}
