package org.apache.clerezza.rdf.jena.tdb.internals;

import java.util.concurrent.locks.ReadWriteLock;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraphWrapper;
import org.apache.clerezza.rdf.core.impl.SimpleGraph;
import org.apache.clerezza.rdf.core.impl.util.PrivilegedGraphWrapper;
import org.apache.clerezza.rdf.core.impl.util.PrivilegedMGraphWrapper;
import org.apache.clerezza.rdf.jena.storage.JenaGraphAdaptor;
import org.apache.clerezza.rdf.jena.tdb.storage.SingleTdbDatasetTcProvider;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.tdb.TDB;

/**
 * Represents the Jena {@link Model} and the Clerezza {@link Graph} or
 * {@link MGraph}. It also provide access to the {@link JenaGraphAdaptor}
 * so that this component can add parsed data to {@link Graph}s created
 * by calls to {@link SingleTdbDatasetTcProvider#createGraph(UriRef, TripleCollection)}.
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
     * The {@link Graph}(in case of read-only) or {@link MGraph} (if read/write)
     * that can be shared with other components. The instance stored by this
     * variable will use all the required Wrappers such as such as 
     * {@link LockableMGraphWrapper lockable} and {@link PrivilegedMGraphWrapper
     * privileged}.
     */
    private TripleCollection graph;
    /**
     * keeps the state if this represents an {@link Graph} (read-only) or
     * {@link MGraph}(read/write) ModelGraph.
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
                public Graph getGraph() {
                    return new SimpleGraph(this,true);
                }
            };
            graph = new PrivilegedGraphWrapper(jenaAdapter.getGraph());
        } else { //construct an MGraph
            jenaAdapter = new JenaGraphAdaptor(model.getGraph());
            this.graph =  new DatasetLockedMGraph(lock,
                new PrivilegedMGraphWrapper(jenaAdapter));
        }
    }
    /**
     * The {@link JenaGraphAdaptor}. For internal use only! Do not pass
     * this instance to other components. Use {@link #getGraph()} and
     * {@link #getMGraph()} instead!
     * @return the plain {@link JenaGraphAdaptor}
     */
    public JenaGraphAdaptor getJenaAdapter(){
        return jenaAdapter;
    }

    public boolean isReadWrite(){
        return readWrite;
    }
    /**
     * Getter for the {@link MGraph}
     * @return the {@link MGraph}
     * @throws IllegalStateException if this {@link ModelGraph} is NOT
     * {@link #readWrite}
     */
    public MGraph getMGraph(){
        if(!readWrite){
            throw new IllegalStateException("Unable to return MGraph for read-only models");
        }
        return (MGraph)graph;
    }
    /**
     * Getter for the {@link Graph}
     * @return the {@link Graph}
     * @throws IllegalStateException if this {@link ModelGraph} is 
     * {@link #readWrite}
     */
    public Graph getGraph() {
        if(readWrite){
            throw new IllegalStateException("Unable to return Graph for read/write models.");
        }
        return (Graph)graph;
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
