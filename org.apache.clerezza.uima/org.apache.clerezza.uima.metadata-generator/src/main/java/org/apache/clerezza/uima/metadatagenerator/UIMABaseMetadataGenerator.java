package org.apache.clerezza.uima.metadatagenerator;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.clerezza.rdf.metadata.MetaDataGenerator;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.uima.utils.ExternalServicesFacade;
import org.apache.uima.UIMAException;

/**
 * 
 * An implementation of <code>MetaDataGenerator</code> generates meta data about specified data
 * depending on its media type using UIMA.
 * 
 */
public class UIMABaseMetadataGenerator implements MetaDataGenerator {

  private ExternalServicesFacade facade = new ExternalServicesFacade();

  @Override
  public void generate(GraphNode node, byte[] data, MediaType mediaType) {
    // FIXME only TEXT_PLAIN, also different MediaTypes should be served
    if (MediaType.TEXT_PLAIN.equals(mediaType.getType())) {
      try {
        //add language to the document
        addLanguage(node, data);
      } catch (Throwable e) {
        // quietly react to errors
      }
    }
  }

  private void addLanguage(GraphNode node, byte[] data) throws UIMAException {
    // get language to bind to the node
    String language = facade.getLanguage(data.toString());
    addStringLiteral(language, node, DCTERMS.language);
  }

  private void addTags(GraphNode node, byte[] data) throws UIMAException {
    // get keywords (tags) to bind to the node
    List<String> tags = facade.getTags(data.toString());
    for (String keyword : tags) {
      // add each tag inside the node
      // FIXME find the proper UriRef to store tags
      addStringLiteral(keyword, node, null);
    }
  }

  private void addStringLiteral(String value, GraphNode node, UriRef uriRef) {
    node.addProperty(uriRef, LiteralFactory.getInstance().createTypedLiteral(value));
  }

}
