/*
 * Copyright (c) 2008 basis06 AG.
 * All rights reserved.
 * 
 * Unless required by applicable law or agreed to in writing, this
 * software is distributed without warranties or conditions of any
 * kind, either express or implied.
 */
package org.apache.clerezza.rdf.sesame.storage;

import java.io.File;
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
    public SesameMGraph setUp(String testName) throws RepositoryException {
        final File userDir= new File(System.getProperty("user.dir"));
        final File dataDir= new File(userDir, "tmp/SesameGraphTest");
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
