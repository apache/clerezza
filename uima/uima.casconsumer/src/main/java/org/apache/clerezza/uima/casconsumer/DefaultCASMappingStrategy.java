package org.apache.clerezza.uima.casconsumer;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.uima.utils.UIMAUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.cas.TOP;

/**
 * the default implementation for {@link CASMappingStrategy}
 */
public class DefaultCASMappingStrategy implements CASMappingStrategy {

  @Override
  public void map(CAS cas, String graphName) throws CASMappingException {
    try {
      GraphNode node = createNode(graphName);
      UIMAUtils.enhanceNode(node, UIMAUtils.getAllFSofType(TOP.type, cas.getJCas()));
    } catch (Exception e) {
      throw new CASMappingException(e);
    }
  }

  private GraphNode createNode(String graphName) {
    final TcManager tcManager = TcManager.getInstance();
    final UriRef mGraphName = new UriRef(graphName);
    return new GraphNode(new BNode(), tcManager.createMGraph(mGraphName));
  }
}
