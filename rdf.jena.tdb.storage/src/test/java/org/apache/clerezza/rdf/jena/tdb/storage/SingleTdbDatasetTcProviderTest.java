package org.apache.clerezza.rdf.jena.tdb.storage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
import org.apache.clerezza.rdf.core.test.TcProviderTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationException;

public class SingleTdbDatasetTcProviderTest extends TcProviderTest {

    private static File tempFile;
    private static Dictionary<String,Object> config;
    private static SingleTdbDatasetTcProvider provider;
    private static UriRef UNION_GRAPH_NAME = new UriRef("http://www.example.org/unionGraph");
    @BeforeClass
    public static void setup() throws IOException, ConfigurationException {
        tempFile = File.createTempFile("tdbdatasettest", null);
        tempFile.delete();
        tempFile.mkdirs();
        config = new Hashtable<String,Object>();
        config.put(SingleTdbDatasetTcProvider.TDB_DIR, tempFile.getAbsolutePath());
        config.put(SingleTdbDatasetTcProvider.DEFAULT_GRAPH_NAME, UNION_GRAPH_NAME.getUnicodeString());
        //config.put(SingleTdbDatasetTcProvider.USE_GRAPH_NAME_SUFFIXES, true);
    }
    @AfterClass
    public static void cleanUpDirectory() {
        //nothin todo because cleanData does the job already
    }
    @Override
    protected TcProvider getInstance() {
        //tests want us to deactivate/activate the TcProvider on multiple calls
        //to getInstance within the same test
        if(provider !=null){
            provider.deactivate(null);
            provider = null;
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
            provider.deactivate(null);
            TdbTcProvider.delete(tempFile);
            provider = null;
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
        Graph grpah = provider.getGraph(UNION_GRAPH_NAME);
        Assert.assertNotNull(grpah);
    }
    
    @Test
    public void testUnionGraph() throws Exception{
        TcProvider provider = getInstance();
        UriRef type = new UriRef("http://www.w3.org/2000/01/rdf-schema#type");
        UriRef name = new UriRef("http://schema.org/name");
        UriRef personType = new UriRef("http://schema.org/Person");
        UriRef companyType = new UriRef("http://schema.org/Company");
        UriRef worksFor = new UriRef("http://schema.org/works-for");

        //create a graph with persons
        MGraph persons = new SimpleMGraph();
        UriRef tim = new UriRef("http://people.org/tim.johnson");
        persons.add(new TripleImpl(tim, type, personType));
        persons.add(new TripleImpl(tim, name, new PlainLiteralImpl("Tim Johnson")));
        UriRef john = new UriRef("http://people.org/john.swenson");
        persons.add(new TripleImpl(john, type, personType));
        persons.add(new TripleImpl(john, name, new PlainLiteralImpl("John Swenson")));
        provider.createGraph(new UriRef("urn:persons"), persons.getGraph());
        
        //create a MGraph with data about persons
        MGraph orgdata = provider.createMGraph(new UriRef("urn:orgdata"));
        UriRef talinor = new UriRef("http://company.org/talinor");
        orgdata.add(new TripleImpl(talinor, type, companyType));
        orgdata.add(new TripleImpl(talinor, name, new PlainLiteralImpl("Talinor Inc.")));
        orgdata.add(new TripleImpl(john, worksFor, talinor));
        UriRef kondalor = new UriRef("http://company.org/kondalor");
        orgdata.add(new TripleImpl(kondalor, type, companyType));
        orgdata.add(new TripleImpl(kondalor, name, new PlainLiteralImpl("Kondalor Ges.m.b.H.")));
        orgdata.add(new TripleImpl(tim, worksFor, kondalor));
        
        //now get the union graph
        Graph data = provider.getGraph(UNION_GRAPH_NAME);
        Assert.assertNotNull(data);
        //CLEREZZA-714: getTriples need to correctly return the UnionGraph
        data = (Graph)provider.getTriples(UNION_GRAPH_NAME);
        Assert.assertNotNull(data);
        //NOTE: Jena TDB does not support getSize for the union graph
//        int expectedTripleCount = persons.size()+orgdata.size();
//        Assert.assertEquals("Uniongraph has "+data.size()
//            +" triples (expected "+expectedTripleCount+")",
//            expectedTripleCount, data.size());
        Iterator<Triple> it = data.filter(null, type, companyType);
        Set<UriRef> expected = new HashSet<UriRef>(Arrays.asList(talinor,kondalor));
        while(it.hasNext()){
            NonLiteral subject = it.next().getSubject();
            Assert.assertTrue("Unexpected "+subject, expected.remove(subject));
        }
        Assert.assertTrue("Missing "+expected, expected.isEmpty());

        it = data.filter(null, type, personType);
        expected = new HashSet<UriRef>(Arrays.asList(john,tim));
        while(it.hasNext()){
            NonLiteral subject = it.next().getSubject();
            Assert.assertTrue("Unexpected "+subject, expected.remove(subject));
        }
        Assert.assertTrue("Missing "+expected, expected.isEmpty());
        
    }
    
    @Test
    public void testListGraph(){
        TcProvider provider = getInstance();
        //No union graph in listMGraphs
        Set<UriRef> mgl = provider.listMGraphs();
        Assert.assertFalse("Mgraph list don't contain the read-only union-graph", mgl.contains(UNION_GRAPH_NAME));
        //Union graph in listGraphs
        Set<UriRef> gl = provider.listGraphs();
        Assert.assertTrue("Graph list contain the read-only union-graph", gl.contains(UNION_GRAPH_NAME));
    }
}
