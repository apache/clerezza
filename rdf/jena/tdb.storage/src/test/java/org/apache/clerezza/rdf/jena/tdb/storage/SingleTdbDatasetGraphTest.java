package org.apache.clerezza.rdf.jena.tdb.storage;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.test.GraphTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.osgi.service.cm.ConfigurationException;

public class SingleTdbDatasetGraphTest extends GraphTest {

    private static final String MGRAPHNAME_PREFIX = "http://text.example.org/testGraph";
    private static IRI UNION_GRAPH_NAME = new IRI("http://www.example.org/unionGraph");
    private static int testGraphCounter = 0;

    private static File tempFile;
    private static Dictionary<String,Object> config;
    private static SingleTdbDatasetTcProvider provider;

    @BeforeClass
    public static void setup() throws IOException, ConfigurationException {
        tempFile = File.createTempFile("tdbdatasettest", null);
        tempFile.delete();
        tempFile.mkdirs();
        config = new Hashtable<String,Object>();
        config.put(SingleTdbDatasetTcProvider.TDB_DIR, tempFile.getAbsolutePath());
        config.put(SingleTdbDatasetTcProvider.DEFAULT_GRAPH_NAME, UNION_GRAPH_NAME.getUnicodeString());
        provider = new SingleTdbDatasetTcProvider(config);
    }
    
    @AfterClass
    public static void cleanUpDirectory() throws IOException {
        for(int i = 0; i < testGraphCounter;i++){
            provider.deleteGraph(new IRI(MGRAPHNAME_PREFIX+i));
        }
        provider.deactivate(null);
        try {
            TdbTcProvider.delete(tempFile);
        } catch (IOException e) {
            System.err.println("Couldn't remove "+tempFile);
        }
    }

    @Override
    protected Graph getEmptyGraph() {
        Graph graph = provider.createGraph(new IRI(MGRAPHNAME_PREFIX+testGraphCounter));
        testGraphCounter++;
        return graph;
    }

}
