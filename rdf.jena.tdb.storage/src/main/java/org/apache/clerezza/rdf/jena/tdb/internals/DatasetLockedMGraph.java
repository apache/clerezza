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
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphListener;
import org.apache.clerezza.rdf.jena.tdb.storage.SingleTdbDatasetTcProvider;

/**
 * {@link LockableMGraph} wrapper that uses a single {@link ReadWriteLock} for
 * the Jena TDB {@link SingleTdbDatasetTcProvider#dataset}
 * @author Rupert Westenthaler
 *
 */
public class DatasetLockedMGraph implements LockableMGraph {

    private final MGraph wrapped;
    private final ReadWriteLock datasetLock;

    /**
     * Constructs a LocalbleMGraph for an MGraph.
     *
     * @param providedMGraph a non-lockable mgraph
     */
    public DatasetLockedMGraph(final ReadWriteLock lock, final MGraph providedMGraph) {
        this.wrapped = providedMGraph;
        this.datasetLock = lock;
    }

    @Override
    public ReadWriteLock getLock() {
        return datasetLock;
    }

    @Override
    public Graph getGraph() {
        datasetLock.readLock().lock();
        try {
            return wrapped.getGraph();
        } finally {
            datasetLock.readLock().unlock();
        }
    }

    @Override
    public Iterator<Triple> filter(NonLiteral subject, UriRef predicate, Resource object) {
        //users will need to aquire a readlock while iterating
        return wrapped.filter(subject, predicate, object);
    }

    @Override
    public int size() {
        datasetLock.readLock().lock();
        try {
            return wrapped.size();
        } finally {
            datasetLock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        datasetLock.readLock().lock();
        try {
            return wrapped.isEmpty();
        } finally {
            datasetLock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(Object o) {
        datasetLock.readLock().lock();
        try {
            return wrapped.contains(o);
        } finally {
            datasetLock.readLock().unlock();
        }
    }

    @Override
    public Iterator<Triple> iterator() {
        //users will need it acquire a read lock while iterating!
        return wrapped.iterator();
    }

    @Override
    public Object[] toArray() {
        datasetLock.readLock().lock();
        try {
            return wrapped.toArray();
        } finally {
            datasetLock.readLock().unlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        datasetLock.readLock().lock();
        try {
            return wrapped.toArray(a);
        } finally {
            datasetLock.readLock().unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        datasetLock.readLock().lock();
        try {
            return wrapped.containsAll(c);
        } finally {
            datasetLock.readLock().unlock();
        }
    }

    @Override
    public boolean add(Triple e) {
        datasetLock.writeLock().lock();
        try {
            return wrapped.add(e);
        } finally {
            datasetLock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        datasetLock.writeLock().lock();
        try {
            return wrapped.remove(o);
        } finally {
            datasetLock.writeLock().unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends Triple> c) {
        datasetLock.writeLock().lock();
        try {
            return wrapped.addAll(c);
        } finally {
            datasetLock.writeLock().unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        datasetLock.writeLock().lock();
        try {
            return wrapped.removeAll(c);
        } finally {
            datasetLock.writeLock().unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        datasetLock.writeLock().lock();
        try {
            return wrapped.retainAll(c);
        } finally {
            datasetLock.writeLock().unlock();
        }
    }

    @Override
    public void clear() {
        datasetLock.writeLock().lock();
        try {
            wrapped.clear();
        } finally {
            datasetLock.writeLock().unlock();
        }
    }

    @Override
    public void addGraphListener(GraphListener listener, FilterTriple filter, long delay) {
        wrapped.addGraphListener(listener, filter, delay);
    }

    @Override
    public void addGraphListener(GraphListener listener, FilterTriple filter) {
        wrapped.addGraphListener(listener, filter);
    }

    @Override
    public void removeGraphListener(GraphListener listener) {
        wrapped.removeGraphListener(listener);
    }

    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof DatasetLockedMGraph){
            DatasetLockedMGraph other = (DatasetLockedMGraph) obj;
            return wrapped.equals(other.wrapped);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return wrapped.toString();
    }        
}
