package org.apache.clerezza.rdf.jena.tdb.storage;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.apache.commons.rdf.Graph;
import org.apache.clerezza.rdf.core.access.QueryableTcProvider;
import org.apache.clerezza.rdf.core.sparql.query.Query;
import org.apache.clerezza.rdf.jena.sparql.ResultSetWrapper;
import org.apache.clerezza.rdf.jena.storage.JenaGraphAdaptor;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryExecException;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.hp.hpl.jena.update.UpdateAction;
import org.apache.commons.rdf.Iri;

abstract class BaseTdbTcProvider implements QueryableTcProvider{

	// ------------------------------------------------------------------------
	// Object attributes
	// ------------------------------------------------------------------------

	private Dataset dataset;
		
	// ------------------------------------------------------------------------
	// Implementing QueryableTcProvider
	// ------------------------------------------------------------------------

    @Override
    public Object executeSparqlQuery(final String query, Iri defaultGraph) {
		// Missing permission (java.lang.RuntimePermission getClassLoader)
		// when calling QueryFactory.create causes ExceptionInInitializerError
		// to be thrown.
		// QueryExecutionFactory.create requires
		// (java.io.FilePermission [etc/]location-mapping.* read)
		// Thus, they are placed within doPrivileged
		QueryExecution qexec = AccessController
				.doPrivileged(new PrivilegedAction<QueryExecution>() {

					@Override
					public QueryExecution run() {
						try {
							com.hp.hpl.jena.query.Query jenaQuery = QueryFactory
									.create(query);
							if (jenaQuery.isUnknownType()) {
								return null;
							}
							return QueryExecutionFactory.create(jenaQuery, getDataset());
						} catch (QueryException ex) {
							return null;
						}							
					}
				});

		if (qexec == null) {
			return executeUpdate(query);
		}

		try {
			try {
				return new ResultSetWrapper(qexec.execSelect());
			} catch (QueryExecException e) {
				try {
					return Boolean.valueOf(qexec.execAsk());
				} catch (QueryExecException e2) {
					try {
						return new JenaGraphAdaptor(qexec.execDescribe()
								.getGraph()).getImmutableGraph();
					} catch (QueryExecException e3) {
						return new JenaGraphAdaptor(qexec.execConstruct()
								.getGraph()).getImmutableGraph();
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

	// ------------------------------------------------------------------------
	// Private methods
	// ------------------------------------------------------------------------
    
	private Object executeUpdate(String query) {
        GraphStore graphStore = GraphStoreFactory.create(getDataset()) ;
        UpdateAction.parseExecute(query, graphStore) ;
        return true;
    }
}
