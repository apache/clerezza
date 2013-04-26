package org.apache.clerezza.rdf.jena.tdb.storage;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.test.MGraphTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.osgi.service.cm.ConfigurationException;

public class SingleTdbDatasetMGraphTest extends MGraphTest {

    private static final String MGRAPHNAME_PREFIX = "http://text.example.org/testGraph";
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
        config.put(SingleTdbDatasetTcProvider.DEFAULT_GRAPH_NAME, "http://www.example.org/defaultGraph");
        provider = new SingleTdbDatasetTcProvider(config);
    }
    @AfterClass
    public static void cleanUpDirectory() throws IOException {
        for(int i = 0; i < testGraphCounter;i++){
            provider.deleteTripleCollection(new UriRef(MGRAPHNAME_PREFIX+i));
        }
        provider.deactivate(null);
        TdbTcProvider.delete(tempFile);
    }
    @Override
    protected MGraph getEmptyMGraph() {
        MGraph graph = provider.createMGraph(new UriRef(MGRAPHNAME_PREFIX+testGraphCounter));
        testGraphCounter++;
        return graph;
    }

}
