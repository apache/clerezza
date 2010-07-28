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
@Component(metatype = true)
@Services({
        @Service(MetaDataGenerator.class),
        @Service(UIMABaseMetadataGenerator.class)
})
public class UIMABaseMetadataGenerator implements MetaDataGenerator {

  private ExternalServicesFacade facade = new ExternalServicesFacade();

  @Override
  public void generate(GraphNode node, byte[] data, MediaType mediaType) {
    try {
      String text = getTextToAnalyze(data, mediaType);

      // add language to the document
      addLanguage(node, text);

      // add wide purpose subject to the document
      addCategory(node, text);

      // add calais annotations' nodes
      addCalaisAnnotations(node, text);

      // add alchemyAPI's annotations' nodes
      addAlchemyAPIEntities(node, text);

    } catch (Throwable e) {
      // do nothing
    }
  }

  private String getTextToAnalyze(byte[] data, MediaType mediaType) throws UnsupportedMediaTypeException {
    String text = null;
    if (MediaType.TEXT_PLAIN.equals(mediaType)) {
      text = new String(data);
    }
    if (text == null) {
      throw new UnsupportedMediaTypeException(mediaType.getType());
    }
    return text;
  }


  private void addCategory(GraphNode node, String data) throws UIMAException {
    // get category to bind it to the node
    String category = facade.getCategory(data);
    node.addPropertyValue(DC.subject, category);
  }

  private void addLanguage(GraphNode node, String data) throws UIMAException {
    // get language to bind it to the node
    String language = facade.getLanguage(data);
    node.addPropertyValue(DCTERMS.language, language);
  }

  private void addCalaisAnnotations(GraphNode existingNode, String data) throws UIMAException {
    // analyze document text and get the corresponding OpenCalais annotations
    List<Annotation> calaisAnnotations = facade.getCalaisAnnotations(data);
    // convert annotations to nodes inside the current graph
    UIMAUtils.enhanceNode(existingNode, calaisAnnotations);

  }

  private void addAlchemyAPIEntities(GraphNode existingNode, String data) throws UIMAException {
    // analyze document text and get the corresponding AlchemyAPI Tags
    List<FeatureStructure> alchemyAPIEntities = facade.getAlchemyAPITags(data);
    // convert entities to nodes inside the current graph
    UIMAUtils.enhanceNode(existingNode, alchemyAPIEntities);
  }

  private class UnsupportedMediaTypeException extends Throwable {
    private UnsupportedMediaTypeException(String s) {
      super(s);
    }
  }
}
