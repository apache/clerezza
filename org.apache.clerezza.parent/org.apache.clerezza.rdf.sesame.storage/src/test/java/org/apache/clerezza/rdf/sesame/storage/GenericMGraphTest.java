package org.apache.clerezza.rdf.sesame.storage;

import org.junit.After;
import org.junit.Before;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.test.MGraphTest;


/**
 *
 * @author reto
 */
public class GenericMGraphTest extends MGraphTest {
    
    private SesameGraphTestSupport support;
    private SesameMGraph graph;
    
    
    @Before
    public void setUp() throws Exception {
        support= new SesameGraphTestSupport();
        graph= support.setUp("GenericTest");
    }
    
    @After
    public void tearDown() throws Exception {
        support.tearDown();
    }
    
    
    @Override
    protected MGraph getEmptyMGraph() {
        return graph;
    }
}
