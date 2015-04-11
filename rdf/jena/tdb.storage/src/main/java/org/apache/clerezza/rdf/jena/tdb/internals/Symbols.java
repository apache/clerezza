package org.apache.clerezza.rdf.jena.tdb.internals;

import org.apache.clerezza.commons.rdf.IRI;

/**
 * Provides static methods in order to represent classes used to
 * represent named graphs.
 * 
 * @author misl
 * 
 */
public class Symbols {

    public static IRI Index = new IRI("http://clerezza.apache.org/storage/Index");

    public static IRI Default = new IRI("http://clerezza.apache.org/storage/Default");
    
    public static IRI ImmutableGraph = new IRI("http://clerezza.apache.org/storage/ImmutableGraph");

    public static IRI Graph = new IRI("http://clerezza.apache.org/storage/Graph");

}
