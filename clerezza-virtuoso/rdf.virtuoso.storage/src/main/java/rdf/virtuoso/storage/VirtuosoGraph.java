package rdf.virtuoso.storage;

import java.util.Collection;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jdbc4.VirtuosoConnection;
/**
 * This is a read-only version of {@link VirtuosoMGraph}
 * @author enridaga
 *
 */
public class VirtuosoGraph extends VirtuosoMGraph implements Graph{

	/**
	 * Logger
	 */
	protected Logger logger = LoggerFactory
			.getLogger(VirtuosoGraph.class);
	
	public VirtuosoGraph(String name, VirtuosoConnection connection) {
		super(name, connection);
	}

	@Override
	public synchronized boolean add(Triple e) {
		logger.warn("Attempting modifying an immutable graph");
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized boolean addAll(Collection<? extends Triple> c) {
		logger.warn("Attempting modifying an immutable graph");
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void clear() {
		logger.warn("Attempting modifying an immutable graph");
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized boolean remove(Object o) {
		logger.warn("Attempting modifying an immutable graph");
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized boolean removeAll(Collection<?> col) {
		logger.warn("Attempting modifying an immutable graph");
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Must be a VirtuosoGraph with the same name.
	 */
	@Override
	public boolean equals(Object o) {
		logger.debug("equals({})",o.getClass());
		if (o instanceof VirtuosoGraph) {
			logger.debug("{} is a VirtuosoGraph)",o);
			if (((VirtuosoGraph) o).getName().equals(this.getName())) {
				logger.debug("Names are equal! They are equal!");
				return true;
			}
		}else{
			logger.debug("Not a VirtuosoGraph instance: {}",o.getClass());
		}
		return false;
	}
}
