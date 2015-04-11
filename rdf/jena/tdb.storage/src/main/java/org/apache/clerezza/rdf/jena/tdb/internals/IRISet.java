package org.apache.clerezza.rdf.jena.tdb.internals;

import java.util.AbstractSet;
import java.util.Iterator;

import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.ontologies.RDF;

/**
 * Represents a set of uri ref Set<IRI>
 * @author Minto van der Sluis
 */
public class IRISet extends AbstractSet<IRI> {
    private ModelGraph graphNameIndex;
    private IRI graphType;

    public IRISet(ModelGraph index, IRI object) {
        graphNameIndex = index;
        graphType = object;
    }
  
    @Override
    public int size() {
        // TODO: How to get the proper size based on graphType
        return graphNameIndex.getGraph().size();
    }
  
    @Override
    public Iterator<IRI> iterator() {
        final Iterator<Triple> indexIter = graphNameIndex.getGraph().filter( null, RDF.type, graphType );
        return new Iterator<IRI>() {
            @Override
            public boolean hasNext() {
                return indexIter.hasNext();
            }
  
            @Override
            public IRI next() {
                return IRI.class.cast(indexIter.next().getSubject());
            }
  
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    @Override
    public boolean add(IRI o) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public boolean contains(Object o) {
        if (o instanceof IRI) {
            return graphNameIndex.getGraph().filter(IRI.class.cast(o), RDF.type, graphType).hasNext();
        }
        return false;
    }
};    
