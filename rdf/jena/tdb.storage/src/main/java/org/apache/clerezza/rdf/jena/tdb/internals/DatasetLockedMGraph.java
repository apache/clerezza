package org.apache.clerezza.rdf.jena.tdb.internals;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.ReadWriteLock;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.access.LockableMGraphWrapper;
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphListener;
import org.apache.clerezza.rdf.jena.tdb.storage.SingleTdbDatasetTcProvider;

/**
 * {@link LockableMGraph} wrapper that uses a single {@link ReadWriteLock} for
 * the Jena TDB {@link SingleTdbDatasetTcProvider#dataset}
 * @author Rupert Westenthaler
 *
 */
public class DatasetLockedMGraph extends LockableMGraphWrapper {


    /**
     * Constructs a LocalbleMGraph for an MGraph.
     *
     * @param providedMGraph a non-lockable mgraph
     */
    public DatasetLockedMGraph(final ReadWriteLock lock, final MGraph providedMGraph) {
        super(providedMGraph, lock);
    }

    //Maybe overwriting this prevents unnecessary locking
    /*
    @Override
    public Iterator<Triple> filter(NonLiteral subject, UriRef predicate, Resource object) {
        //users will need to aquire a readlock while iterating
        return wrapped.filter(subject, predicate, object);
    }
    */
        
}
