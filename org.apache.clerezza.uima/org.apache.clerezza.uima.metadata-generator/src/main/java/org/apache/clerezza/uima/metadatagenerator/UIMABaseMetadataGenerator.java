package org.apache.clerezza.uima.metadatagenerator;

import org.apache.clerezza.rdf.metadata.MetaDataGenerator;
import org.apache.clerezza.rdf.ontologies.DC;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.uima.utils.ExternalServicesFacade;
import org.apache.clerezza.uima.utils.UIMAUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.tcas.Annotation;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * An implementation of <code>MetaDataGenerator</code> generates meta data about specified data
 * depending on its media type using Apache UIMA.
 */
@Component(metatype=true)
@Services({
  @Service(MetaDataGenerator.class),
  @Service(UIMABaseMetadataGenerator.class)
})
public class UIMABaseMetadataGenerator implements MetaDataGenerator {

  private ExternalServicesFacade facade = new ExternalServicesFacade();

  @Override
  public void generate(GraphNode node, byte[] data, MediaType mediaType) {
    // FIXME only TEXT_PLAIN, also different MediaTypes should be served
    if (MediaType.TEXT_PLAIN.equals(mediaType.getType())) {
      try {
        // add language to the document
        addLanguage(node, data);

        // add wide purpose subject to the document
        addCategory(node, data);

        // add calais annotations' nodes
        addCalaisAnnotations(node, data);

        // add alchemyAPI's annotations' nodes
        addAlchemyAPIEntities(node,data);

      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }

  private void addCategory(GraphNode node, byte[] data) throws UIMAException {
    // get category to bind it to the node
    String category = facade.getCategory(data.toString());
    node.addPropertyValue(DC.subject, category);
  }

  private void addLanguage(GraphNode node, byte[] data) throws UIMAException {
    // get language to bind it to the node
    String language = facade.getLanguage(data.toString());
    node.addPropertyValue(DCTERMS.language, language);
  }

  private void addCalaisAnnotations(GraphNode existingNode, byte[] data) throws UIMAException {
    // analyze document text and get the corresponding OpenCalais annotations
    List<Annotation> calaisAnnotations = facade.getCalaisAnnotations(data.toString());
    // convert annotations to nodes inside the current graph
    UIMAUtils.enhanceNode(existingNode, calaisAnnotations);

  }

  private void addAlchemyAPIEntities(GraphNode existingNode, byte[] data) throws UIMAException {
    // analyze document text and get the corresponding AlchemyAPI Tags
    List<FeatureStructure> alchemyAPIEntities = facade.getAlchemyAPITags(data.toString());
    // convert entities to nodes inside the current graph
    UIMAUtils.enhanceNode(existingNode, alchemyAPIEntities);
  }

}
