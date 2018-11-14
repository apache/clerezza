package org.apache.clerezza.sparql;

import org.apache.clerezza.api.IRI;

import java.util.Set;

public interface GraphStore {

    /**
     * Lists the name of the <Code>Graph</code>s available through this <code>GraphStore</code>.
     *
     * @return the list of <Code>Graph</code>s
     */
    Set<IRI> listGraphs();

    /**
     * Lists the name of the named <Code>Graph</code>s available through this <code>GraphStore</code>.
     *
     * @return the list of named <Code>Graph</code>s
     */
    Set<IRI> listNamedGraphs();
}
