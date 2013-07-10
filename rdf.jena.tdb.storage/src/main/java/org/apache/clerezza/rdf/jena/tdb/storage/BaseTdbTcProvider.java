package org.apache.clerezza.rdf.jena.tdb.storage;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.access.QueryableTcProvider;
import org.apache.clerezza.rdf.core.sparql.query.Query;
import org.apache.clerezza.rdf.jena.sparql.ResultSetWrapper;
import org.apache.clerezza.rdf.jena.storage.JenaGraphAdaptor;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import org.apache.clerezza.rdf.core.UriRef;

abstract class BaseTdbTcProvider implements QueryableTcProvider{

	// ------------------------------------------------------------------------
	// Object attributes
	// ------------------------------------------------------------------------

	private Dataset dataset;
		
	// ------------------------------------------------------------------------
	// Implementing QueryableTcProvider
	// ------------------------------------------------------------------------

    @Override
    public Object executeSparqlQuery(final String query, UriRef defaultGraph) {
		// Missing permission (java.lang.RuntimePermission getClassLoader)
		// when calling QueryFactory.create causes ExceptionInInitializerError
		// to be thrown.
		// QueryExecutionFactory.create requires
		// (java.io.FilePermission [etc/]location-mapping.* read)
		// Thus, they are placed within doPrivileged
        getDataset().setDefaultModel(null);
		QueryExecution qexec = AccessController
				.doPrivileged(new PrivilegedAction<QueryExecution>() {

					@Override
					public QueryExecution run() {
						com.hp.hpl.jena.query.Query jenaQuery = QueryFactory
								.create(query);
						return QueryExecutionFactory.create(jenaQuery, getDataset());
					}
				});

		try {
			try {
				return new ResultSetWrapper(qexec.execSelect());
			} catch (QueryExecException e) {
				try {
					return Boolean.valueOf(qexec.execAsk());
				} catch (QueryExecException e2) {
					try {
						return new JenaGraphAdaptor(qexec.execDescribe()
								.getGraph()).getGraph();
					} catch (QueryExecException e3) {
						return new JenaGraphAdaptor(qexec.execConstruct()
								.getGraph()).getGraph();
					}
				}
			}
		} finally {
			qexec.close();
		}
    }
    

	// ------------------------------------------------------------------------
	// Getters / Setters
	// ------------------------------------------------------------------------
    
    public Dataset getDataset() {
    	if (dataset == null) {
    		throw new RuntimeException("Missing Dataset!");
    	}
		return dataset;
	}
    
    public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}
}
