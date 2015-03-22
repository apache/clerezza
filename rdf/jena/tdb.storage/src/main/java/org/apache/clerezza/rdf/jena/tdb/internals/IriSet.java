package org.apache.clerezza.rdf.jena.tdb.internals;

import java.util.AbstractSet;
import java.util.Iterator;

import org.apache.commons.rdf.Triple;
import org.apache.commons.rdf.Iri;
import org.apache.clerezza.rdf.ontologies.RDF;

/**
 * Represents a set of uri ref Set<Iri>
 * @author Minto van der Sluis
 */
public class IriSet extends AbstractSet<Iri> {
    private ModelGraph graphNameIndex;
    private Iri graphType;

    public IriSet(ModelGraph index, Iri object) {
        graphNameIndex = index;
        graphType = object;
    }
  
    @Override
    public int size() {
        // TODO: How to get the proper size based on graphType
        return graphNameIndex.getGraph().size();
    }
  
    @Override
    public Iterator<Iri> iterator() {
        final Iterator<Triple> indexIter = graphNameIndex.getGraph().filter( null, RDF.type, graphType );
        return new Iterator<Iri>() {
            @Override
            public boolean hasNext() {
                return indexIter.hasNext();
            }
  
            @Override
            public Iri next() {
                return Iri.class.cast(indexIter.next().getSubject());
            }
  
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    @Override
    public boolean add(Iri o) {
        throw new UnsupportedOperationException();
    }
  
    @Override
    public boolean contains(Object o) {
        if (o instanceof Iri) {
            return graphNameIndex.getGraph().filter(Iri.class.cast(o), RDF.type, graphType).hasNext();
        }
        return false;
    }
};    
