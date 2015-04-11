package org.apache.clerezza.rdf.jena.tdb.internals;

import java.util.concurrent.locks.ReadWriteLock;

import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Graph;

import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.rdf.core.impl.util.PrivilegedGraphWrapper;
import org.apache.clerezza.rdf.core.impl.util.PrivilegedGraphWrapper;
import org.apache.clerezza.rdf.jena.storage.JenaGraphAdaptor;
import org.apache.clerezza.rdf.jena.tdb.storage.SingleTdbDatasetTcProvider;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.tdb.TDB;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleImmutableGraph;

/**
 * Represents the Jena {@link Model} and the Clerezza {@link ImmutableGraph} or
 * {@link Graph}. It also provide access to the {@link JenaGraphAdaptor}
 * so that this component can add parsed data to {@link ImmutableGraph}s created
 * by calls to {@link SingleTdbDatasetTcProvider#createGraph(IRI, TripleCollection)}.
 * @author Rupert Westenthaler
 *
 */
public class ModelGraph {
    /**
     * The Jena Model
     */
    private final Model model;
    /**
     * The JenaGraphAdapter. Note that in case of read-only in anonymous
     * subclass is used that prevents the creation of an in-memory copy
     * of the data when calling {@link JenaGraphAdaptor#getGraph()}.
     */
    private JenaGraphAdaptor jenaAdapter;
    /**
     * The {@link ImmutableGraph}(in case of read-only) or {@link Graph} (if read/write)
     * that can be shared with other components. The instance stored by this
     * variable will use all the required Wrappers such as such as 
     * {@link LockableGraphWrapper lockable} and {@link PrivilegedGraphWrapper
     * privileged}.
     */
    private Graph graph;
    /**
     * keeps the state if this represents an {@link ImmutableGraph} (read-only) or
     * {@link Graph}(read/write) ModelGraph.
     */
    private final boolean readWrite;
    
    /**
     * Constructs and initializes the ModelGraph
     * @param model the Jena Model
     * @param readWrite if the Clerezza counterpart should be read- and 
     * write-able or read-only.
     */
    public ModelGraph(final ReadWriteLock lock, Model model, boolean readWrite){
        if(model == null){
            throw new IllegalArgumentException("The parsed Model MUST NOT be NULL");
        }
        this.model = model;
        this.readWrite = readWrite;
        if(!readWrite){ //construct an graph
            jenaAdapter = new JenaGraphAdaptor(model.getGraph()){
                /**
                 * Ensure that no in-memory copies are created for read only
                 * Jena Graphs
                 * @return
                 */
                @Override
                public ImmutableGraph getImmutableGraph() {
                    return new SimpleImmutableGraph(this,true);
                }
            };
            graph = new PrivilegedGraphWrapper(jenaAdapter);
        } else { //construct an Graph
            jenaAdapter = new JenaGraphAdaptor(model.getGraph(), lock);
            this.graph =  new PrivilegedGraphWrapper(jenaAdapter);
        }
    }
    /**
     * The {@link JenaGraphAdaptor}. For internal use only! Do not pass
     * this instance to other components. Use {@link #getGraph()} and
     * {@link #getGraph()} instead!
     * @return the plain {@link JenaGraphAdaptor}
     */
    public JenaGraphAdaptor getJenaAdapter(){
        return jenaAdapter;
    }

    public boolean isReadWrite(){
        return readWrite;
    }
    /**
     * Getter for the {@link Graph}
     * @return the {@link Graph}
     * @throws IllegalStateException if this {@link ModelGraph} is NOT
     * {@link #readWrite}
     */
    public Graph getGraph(){
        if(!readWrite){
            throw new IllegalStateException("Unable to return Graph for read-only models");
        }
        return graph;
    }
    /**
     * Getter for the {@link ImmutableGraph}
     * @return the {@link ImmutableGraph}
     * @throws IllegalStateException if this {@link ModelGraph} is 
     * {@link #readWrite}
     */
    public ImmutableGraph getImmutableGraph() {
        if(readWrite){
            throw new IllegalStateException("Unable to return ImmutableGraph for read/write models.");
        }
        return graph.getImmutableGraph();
    }
    /**
     * closes this ModelGraph and frees up all Jena TDB related resources.
     */
    public void close(){
        this.graph = null;
        this.jenaAdapter = null;
        sync();
        this.model.close();
    }
    /**
     * Synchronize the Jena Model with the field system by calling
     * {@link TDB#sync(Model)}
     */
    public void sync(){
        TDB.sync(model);
    }
    /**
     * Removes all triples from the Jena Model and than calls {@link #close()}
     * to free remaining resources. Note that in Jena TDB a named model is 
     * deleted if no more triples with the given context are present within
     * the {@link Quad} store of the Jena TDB {@link DatasetGraph}.
     */
    public void delete(){
        this.model.removeAll();
        close();
    }
    
}
