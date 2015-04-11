package org.apache.clerezza.rdf.jena.tdb.storage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.commons.rdf.impl.utils.PlainLiteralImpl;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import org.apache.clerezza.rdf.core.test.TcProviderTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;

public class SingleTdbDatasetTcProviderTest extends TcProviderTest {

    private static File tempFile;
    private static Dictionary<String,Object> config;
    private static SingleTdbDatasetTcProvider provider;
    private static IRI UNION_GRAPH_NAME = new IRI("http://www.example.org/unionGraph");
    
    @Before
    public void setup() throws IOException, ConfigurationException {
        tempFile = File.createTempFile("tdbdatasettest", null);
        tempFile.delete();
        tempFile.mkdirs();
        config = new Hashtable<String,Object>();
        config.put(SingleTdbDatasetTcProvider.TDB_DIR, tempFile.getAbsolutePath());
        config.put(SingleTdbDatasetTcProvider.DEFAULT_GRAPH_NAME, UNION_GRAPH_NAME.getUnicodeString());
        //config.put(SingleTdbDatasetTcProvider.USE_GRAPH_NAME_SUFFIXES, true);
    }
    
    @Override
    protected TcProvider getInstance() {
        //tests want us to deactivate/activate the TcProvider on multiple calls
        //to getInstance within the same test
        if(provider !=null){
            try {
                provider.deactivate(null);
                provider = null;
            } catch (Exception e) {
                System.err.println("Error cleaning up: "+e.getMessage());
            }
        }
        try {
            provider = new SingleTdbDatasetTcProvider(config);
        } catch (Exception e) {
            throw new RuntimeException("forwarding ...",e);
        }
        return provider;
    }

    @After
    public void cleanData() throws IOException{
        //We need to remove all remaining data after a test
        if(provider != null){
            try {
                provider.deactivate(null);
                TdbTcProvider.delete(tempFile);
                provider = null;
            } catch (Exception e) {
                System.err.println("Error cleaning up: "+e.getMessage());
            }
        }
    }
    
    /**
     * The union graph is read only!
     */
    @Test(expected=NoSuchEntityException.class)
    public void testUnionMgraph(){
        TcProvider provider = getInstance();
        provider.getMGraph(UNION_GRAPH_NAME);
    }
    
    /**
     * Assert union graph on an empty dataset
     */
    @Test
    public void testEmptyUnionGraph(){
        TcProvider provider = getInstance();
        ImmutableGraph graph = provider.getImmutableGraph(UNION_GRAPH_NAME);
        Assert.assertNotNull(graph);
    }
    
    @Test
    public void testUnionGraph() throws Exception{
        TcProvider provider = getInstance();
        IRI type = new IRI("http://www.w3.org/2000/01/rdf-schema#type");
        IRI name = new IRI("http://schema.org/name");
        IRI personType = new IRI("http://schema.org/Person");
        IRI companyType = new IRI("http://schema.org/Company");
        IRI worksFor = new IRI("http://schema.org/works-for");

        //create a graph with persons
        Graph persons = new SimpleGraph();
        IRI tim = new IRI("http://people.org/tim.johnson");
        persons.add(new TripleImpl(tim, type, personType));
        persons.add(new TripleImpl(tim, name, new PlainLiteralImpl("Tim Johnson")));
        IRI john = new IRI("http://people.org/john.swenson");
        persons.add(new TripleImpl(john, type, personType));
        persons.add(new TripleImpl(john, name, new PlainLiteralImpl("John Swenson")));
        provider.createImmutableGraph(new IRI("urn:persons"), persons);
        
        //create a Graph with data about persons
        Graph orgdata = provider.createGraph(new IRI("urn:orgdata"));
        IRI talinor = new IRI("http://company.org/talinor");
        orgdata.add(new TripleImpl(talinor, type, companyType));
        orgdata.add(new TripleImpl(talinor, name, new PlainLiteralImpl("Talinor Inc.")));
        orgdata.add(new TripleImpl(john, worksFor, talinor));
        IRI kondalor = new IRI("http://company.org/kondalor");
        orgdata.add(new TripleImpl(kondalor, type, companyType));
        orgdata.add(new TripleImpl(kondalor, name, new PlainLiteralImpl("Kondalor Ges.m.b.H.")));
        orgdata.add(new TripleImpl(tim, worksFor, kondalor));
        
        //now get the union graph
        ImmutableGraph data = provider.getImmutableGraph(UNION_GRAPH_NAME);
        Assert.assertNotNull(data);
        //CLEREZZA-714: getTriples need to correctly return the UnionGraph
        data = (ImmutableGraph)provider.getGraph(UNION_GRAPH_NAME);
        Assert.assertNotNull(data);
        //NOTE: Jena TDB does not support getSize for the union graph
//        int expectedTripleCount = persons.size()+orgdata.size();
//        Assert.assertEquals("Uniongraph has "+data.size()
//            +" triples (expected "+expectedTripleCount+")",
//            expectedTripleCount, data.size());
        Iterator<Triple> it = data.filter(null, type, companyType);
        Set<IRI> expected = new HashSet<IRI>(Arrays.asList(talinor,kondalor));
        while(it.hasNext()){
            BlankNodeOrIRI subject = it.next().getSubject();
            Assert.assertTrue("Unexpected "+subject, expected.remove(subject));
        }
        Assert.assertTrue("Missing "+expected, expected.isEmpty());

        it = data.filter(null, type, personType);
        expected = new HashSet<IRI>(Arrays.asList(john,tim));
        while(it.hasNext()){
            BlankNodeOrIRI subject = it.next().getSubject();
            Assert.assertTrue("Unexpected "+subject, expected.remove(subject));
        }
        Assert.assertTrue("Missing "+expected, expected.isEmpty());
    }
    
    @Test
    public void testListGraph(){
    	TcProvider provider = getInstance();
    	//No union graph in listGraphs
    	Set<IRI> mgl = provider.listMGraphs();
        Assert.assertFalse("Mgraph contains the read-only union-graph", mgl.contains(UNION_GRAPH_NAME));
        //Union graph in listGraphs
        Set<IRI> gl = provider.listGraphs();
        Assert.assertTrue("ImmutableGraph does not contain the read-only union-graph", gl.contains(UNION_GRAPH_NAME));
    }
}
