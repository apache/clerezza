package org.apache.clerezza.rdf.jena.tdb.storage;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.access.QueryableTcProvider;
import org.apache.clerezza.rdf.core.sparql.query.Query;

abstract class BaseTdbTcProvider implements QueryableTcProvider{

	// ------------------------------------------------------------------------
	// Implementing QueryableTcProvider
	// ------------------------------------------------------------------------

    @Override
    public Object executeSparqlQuery(String query, TripleCollection defaultGraph) {
    	// TODO Auto-generated method stub
    	return null;
    }
    
    @Override
    public Object executeSparqlQuery(Query query, TripleCollection defaultGraph) {
    	return executeSparqlQuery(query.toString(), defaultGraph);
    }
}
