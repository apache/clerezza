package org.apache.clerezza.uima.metadatagenerator;

import org.apache.clerezza.rdf.metadata.MetaDataGenerator;
import org.apache.clerezza.rdf.ontologies.DC;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.uima.metadatagenerator.mediatype.MediaTypeTextExtractor;
import org.apache.clerezza.uima.metadatagenerator.mediatype.PlainTextExtractor;
import org.apache.clerezza.uima.metadatagenerator.mediatype.UnsupportedMediaTypeException;
import org.apache.clerezza.uima.utils.ExternalServicesFacade;
import org.apache.clerezza.uima.utils.UIMAUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * An implementation of {@link MetaDataGenerator} generates meta data about specified data
 * depending on its media type using Apache UIMA.
 */
@Component(metatype = true)
@Services({
        @Service(MetaDataGenerator.class),
        @Service(UIMABaseMetadataGenerator.class)
})
public class UIMABaseMetadataGenerator implements MetaDataGenerator {

  private final static Logger log = LoggerFactory.getLogger(UIMABaseMetadataGenerator.class);

  private ExternalServicesFacade facade;

  private Set<MediaTypeTextExtractor> textExtractors;

  public UIMABaseMetadataGenerator() {
    this.facade = new ExternalServicesFacade();
    this.textExtractors = new TreeSet<MediaTypeTextExtractor>();
  }

  public UIMABaseMetadataGenerator(ExternalServicesFacade facade) {
    this.facade = facade;
    this.textExtractors = new TreeSet<MediaTypeTextExtractor>();
  }

  @Override
  public void generate(GraphNode node, byte[] data, MediaType mediaType) {
    if (textExtractors.isEmpty()) {
      initializeExtractors();
    }
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

      log.info(new StringBuilder(node.toString()).append(" graph node enriched").toString());
    } catch (Throwable e) {
      log.error(new StringBuilder("Unable to extract metadata due to ").append(e.toString()).toString());
    }
  }

  /* initialize text extractors sorted set */
  private void initializeExtractors() {
    this.textExtractors.add(new PlainTextExtractor());
  }

  private String getTextToAnalyze(byte[] data, MediaType mediaType) throws UnsupportedMediaTypeException {
    // since extractors are sorted, the first I found supporting this mediaType is good
    String text = null;
    for (MediaTypeTextExtractor textExtractor : this.textExtractors) {
      if (textExtractor.supports(mediaType)) {
        text = textExtractor.extract(data);
        break;
      }
    }
    if (text == null) {
      throw new UnsupportedMediaTypeException(mediaType);
    }
    return text;
  }


  private void addCategory(GraphNode node, String data) throws UIMAException {
    // get category to bind it to the node
    String category = this.facade.getCategory(data);
    node.addPropertyValue(DC.subject, category);
  }

  private void addLanguage(GraphNode node, String data) throws UIMAException {
    // get language to bind it to the node
    String language = this.facade.getLanguage(data);
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
    List<FeatureStructure> alchemyAPIEntities = this.facade.getAlchemyAPITags(data);
    // convert entities to nodes inside the current graph
    UIMAUtils.enhanceNode(existingNode, alchemyAPIEntities);
  }

}
