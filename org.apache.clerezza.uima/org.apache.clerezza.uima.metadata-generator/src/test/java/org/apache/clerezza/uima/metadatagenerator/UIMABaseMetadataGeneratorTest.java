package org.apache.clerezza.uima.metadatagenerator;

import static org.junit.Assert.fail;

import javax.ws.rs.core.MediaType;

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.junit.Test;
/**
 * Testcase for {@link UIMABaseMetadataGenerator}
 *
 */
public class UIMABaseMetadataGeneratorTest {
  
  @Test
  public void testConstructor() {
    try {
      new UIMABaseMetadataGenerator();
    }
    catch (Exception e) {
      fail(e.getLocalizedMessage());
    }
  }
  
  @Test
  public void testGenerateMethodWithUnsupportedMediaType() {
    try {
      UIMABaseMetadataGenerator baseMetadataGenerator = new UIMABaseMetadataGenerator();
      byte[] data = new byte[]{};
      MGraph mGraph = new SimpleMGraph();
      GraphNode node = new GraphNode(new UriRef("test"), mGraph.getGraph());
      MediaType mediaType = MediaType.valueOf("multipart/form-data; boundary=AaB03x");
      baseMetadataGenerator.generate(node, data, mediaType);
    } catch (Exception e) {
      fail(e.getLocalizedMessage());
    }
    
  }

}
