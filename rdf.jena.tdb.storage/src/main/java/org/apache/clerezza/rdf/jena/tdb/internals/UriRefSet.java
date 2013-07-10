package org.apache.clerezza.rdf.jena.tdb.internals;

import java.util.AbstractSet;
import java.util.Iterator;

import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.RDF;

/**
 * Represents a set of uri ref Set<UriRef>
 * @author Minto van der Sluis
 */
public class UriRefSet extends AbstractSet<UriRef> {
    private ModelGraph graphNameIndex;
    private UriRef graphType;

    public UriRefSet(ModelGraph index, UriRef object) {
        graphNameIndex = index;
        graphType = object;
    }
  
    @Override
    public int size() {
        // TODO: How to get the proper size based on graphType
        return graphNameIndex.getMGraph().size();
    }
  
    @Override
    public Iterator<UriRef> iterator() {
        final Iterator<Triple> indexIter = graphNameIndex.getMGraph().filter( null, RDF.type, graphType );
        return new Iterator<UriRef>() {
            @Override
            public boolean hasNext() {
                return indexIter.hasNext();
            }
  
            @Override
            public UriRef next() {
                return UriRef.class.cast(indexIter.next().getSubject());
            }
  
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    @Override
    public boolean add(UriRef o) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public boolean contains(Object o) {
        if (o instanceof UriRef) {
            return graphNameIndex.getMGraph().filter(UriRef.class.cast(o), RDF.type, graphType).hasNext();
        }
        return false;
    }
};    
