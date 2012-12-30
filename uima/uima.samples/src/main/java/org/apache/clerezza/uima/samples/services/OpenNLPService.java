package org.apache.clerezza.uima.samples.services;

import javax.ws.rs.FormParam;

import org.apache.clerezza.rdf.core.Graph;

/**
 * Add javadoc here
 */
public interface OpenNLPService {

    public Graph extractPersons(String uriString);
}
