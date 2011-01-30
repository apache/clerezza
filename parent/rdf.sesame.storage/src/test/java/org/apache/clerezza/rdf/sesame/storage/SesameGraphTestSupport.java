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
package org.apache.clerezza.rdf.sesame.storage;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.openrdf.repository.RepositoryException;
import org.apache.clerezza.rdf.core.Triple;


/**
 * Class that supports Sesame graph tests.
 *
 * @author thr
 */
public class SesameGraphTestSupport {
    
    private SesameMGraph graph;
    
    
    /**
     * Sets up a new <code>SesameMGraph</code>.
     * @param testName  Name of the test to support.
     * @throws RepositoryException  If it failed to activate the graph.
     */
    public SesameMGraph setUp(String testName) throws RepositoryException, IOException {
        final File dataDir= File.createTempFile("SesameGraph", "Test");
		dataDir.delete();
        dataDir.mkdirs();
        cleanDirectory(dataDir);
        

        
        graph= new SesameMGraph();
		graph.initialize(dataDir);
        //graph.activate(cCtx);
        return graph;
    }
    
    /**
     * Tears down the <code>SesameMGraph</code>.
     * @throws RepositoryException  If it failed to deactivate the graph.
     */
    public void tearDown() throws RepositoryException {
        graph.shutdown();

    }

    /**
     * Cleans the content of the specified directory recursively.
     * @param dir  Abstract path denoting the directory to clean.
     */
    private void cleanDirectory(File dir) {
        File[] files= dir.listFiles();
        if (files!=null && files.length>0) {
            for (File file: files) {
                delete(file);
            }
        }
    }
    
    /**
     * Deletes the specified file or directory.
     * @param file  Abstract path denoting the file or directory to clean.
     */
    private void delete(File file) {
        if (file.isDirectory()) {
            cleanDirectory(file);
        }
        file.delete();
    }
    
    /**
     * Converts the specified triple <code>Iterator</code> into a collection.
     * @param i  The iterator over <code>Triple</code> instances to convert.
     * @return A new collection of <code>Triple</code> objects.
     */
    public static Collection<Triple> toCollection(Iterator<Triple> i) {
        LinkedList<Triple> list= new LinkedList<Triple>();
        while (i.hasNext()) {
            list.add(i.next());
        }
        return list;
    }
}
