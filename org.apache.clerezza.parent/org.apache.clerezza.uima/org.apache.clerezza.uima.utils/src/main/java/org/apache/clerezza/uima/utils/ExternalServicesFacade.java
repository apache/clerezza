package org.apache.clerezza.uima.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.alchemy.ts.keywords.KeywordFS;
import org.apache.uima.alchemy.ts.language.LanguageFS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;

/**
 * Facade for querying UIMA external services
 * 
 * @author tommaso
 * 
 */
public class ExternalServicesFacade {

  private UIMAExecutor uimaExecutor;

  public ExternalServicesFacade() {
    this.uimaExecutor = new UIMAExecutor("ExtServicesAE.xml").withResults();
  }

  public List<String> getTags(String document) throws UIMAException {

    List<String> tags = null;

    try {
      // analyze the document
      uimaExecutor.analyzeDocument(document);

      tags = new ArrayList<String>();

      // get execution results
      JCas jcas = uimaExecutor.getResults();

      // get AlchemyAPI keywords extracted using UIMA
      List<FeatureStructure> keywords = UIMAUtils.getAllFSofType(KeywordFS.type, jcas);

      for (FeatureStructure keywordFS : keywords) {
        tags.add(keywordFS.getStringValue(keywordFS.getType().getFeatureByBaseName("text")));
      }
    } catch (Exception e) {
      throw new UIMAException(e);
    }

    return tags;
  }

  public String getLanguage(String document) throws UIMAException {

    String language = null;

    try {

      // analyze the document
      uimaExecutor.analyzeDocument(document);

      // get execution results
      JCas jcas = uimaExecutor.getResults();

      // extract language Feature Structure using AlchemyAPI Annotator
      FeatureStructure languageFS = UIMAUtils.getSingletonFeatureStructure(LanguageFS.type, jcas);

      language = languageFS.getStringValue(languageFS.getType().getFeatureByBaseName("text"));

    } catch (Exception e) {
      throw new UIMAException(e);
    }

    return language;
  }

}
