package org.apache.clerezza.uima.metadatagenerator;

import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.metadata.MetaDataGenerator;
import org.apache.clerezza.rdf.ontologies.DC;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.uima.utils.ExternalServicesFacade;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.uima.UIMAException;

import javax.ws.rs.core.MediaType;

/**
 * 
 * An implementation of <code>MetaDataGenerator</code> generates meta data about specified data
 * depending on its media type using UIMA.
 * 
 */
@Component()
@Service(MetaDataGenerator.class)
public class UIMABaseMetadataGenerator implements MetaDataGenerator {

  private ExternalServicesFacade facade = new ExternalServicesFacade();

  @Override
  public void generate(GraphNode node, byte[] data, MediaType mediaType) {
    // FIXME only TEXT_PLAIN, also different MediaTypes should be served
    if (MediaType.TEXT_PLAIN.equals(mediaType.getType())) {
      try {
        //add language to the document
        addLanguage(node, data);
        addCategory(node, data);
        
      } catch (Throwable e) {
        // quietly react to errors
      }
    }
  }

  private void addCategory(GraphNode node, byte[] data) throws UIMAException {
    // get category to bind it to the node
    String category = facade.getCategory(data.toString());
    addStringLiteral(category, node, DC.subject);
  }

  private void addLanguage(GraphNode node, byte[] data) throws UIMAException {
    // get language to bind to the node
    String language = facade.getLanguage(data.toString());
    addStringLiteral(language, node, DCTERMS.language);
  }

  private void addStringLiteral(String value, GraphNode node, UriRef uriRef) {
    node.addProperty(uriRef, LiteralFactory.getInstance().createTypedLiteral(value));
  }

}
