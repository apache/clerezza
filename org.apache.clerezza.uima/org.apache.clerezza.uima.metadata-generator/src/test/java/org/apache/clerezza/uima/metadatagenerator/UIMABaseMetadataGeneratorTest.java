package org.apache.clerezza.uima.metadatagenerator;

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import static org.junit.Assert.fail;
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
      String textToAnalyze = "Italy, the defending champions and four-time World Cup winners, suffer a shock World Cup defeat to Slovakia, who win a remarkable game 3-2 to book their place in the last 16";
      MGraph mGraph = new SimpleMGraph();
      GraphNode node = new GraphNode(new UriRef("test"), mGraph.getGraph());
      MediaType mediaType = MediaType.valueOf("multipart/form-data; boundary=AaB03x");
      baseMetadataGenerator.generate(node, textToAnalyze.getBytes(), mediaType);
    } catch (Exception e) {
      fail(e.getLocalizedMessage());
    }
    
  }

}
