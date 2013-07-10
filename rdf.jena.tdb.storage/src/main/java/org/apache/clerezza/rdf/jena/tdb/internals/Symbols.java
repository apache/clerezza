package org.apache.clerezza.rdf.jena.tdb.internals;

import org.apache.clerezza.rdf.core.UriRef;

/**
 * Provides static methods in order to represent classes used to
 * represent named graphs.
 * 
 * @author misl
 * 
 */
public class Symbols {

    public static UriRef Index = new UriRef("http://clerezza.apache.org/storage/Index");

    public static UriRef Default = new UriRef("http://clerezza.apache.org/storage/Default");
    
    public static UriRef Graph = new UriRef("http://clerezza.apache.org/storage/Graph");

    public static UriRef MGraph = new UriRef("http://clerezza.apache.org/storage/MGraph");

}
