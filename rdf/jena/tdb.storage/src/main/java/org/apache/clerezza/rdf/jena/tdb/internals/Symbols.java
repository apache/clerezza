package org.apache.clerezza.rdf.jena.tdb.internals;

import org.apache.clerezza.commons.rdf.Iri;

/**
 * Provides static methods in order to represent classes used to
 * represent named graphs.
 * 
 * @author misl
 * 
 */
public class Symbols {

    public static Iri Index = new Iri("http://clerezza.apache.org/storage/Index");

    public static Iri Default = new Iri("http://clerezza.apache.org/storage/Default");
    
    public static Iri ImmutableGraph = new Iri("http://clerezza.apache.org/storage/ImmutableGraph");

    public static Iri Graph = new Iri("http://clerezza.apache.org/storage/Graph");

}
