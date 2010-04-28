package org.apache.clerezza.uima.utils;

import org.apache.uima.UIMAException;
import org.apache.uima.alchemy.ts.categorization.Category;
import org.apache.uima.alchemy.ts.keywords.KeywordFS;
import org.apache.uima.alchemy.ts.language.LanguageFS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Facade for querying UIMA external services
 */
public class ExternalServicesFacade {

  private UIMAExecutor uimaExecutor;

  private Map<String, Object> parameterSetting = new HashMap<String, Object>();

  public ExternalServicesFacade() {
    this.uimaExecutor = new UIMAExecutor("ExtServicesAE.xml").withResults();
  }

  public List<FeatureStructure> getAlchemyAPITags(String document) throws UIMAException {

    List<FeatureStructure> keywords = new ArrayList<FeatureStructure>();

    try {
      // analyze the document
      uimaExecutor.analyzeDocument(document, "TextKeywordExtractionAEDescriptor.xml", getParameterSetting());

      // get execution results
      JCas jcas = uimaExecutor.getResults();

      // get AlchemyAPI keywords extracted using UIMA
      keywords.addAll(UIMAUtils.getAllFSofType(KeywordFS.type, jcas));

    } catch (Exception e) {
      throw new UIMAException(e);
    }

    return keywords;
  }

  public String getLanguage(String document) throws UIMAException {

    String language = null;

    try {

      // analyze the document
      uimaExecutor.analyzeDocument(document, "TextLanguageDetectionAEDescriptor.xml", getParameterSetting());

      // get execution results
      JCas jcas = uimaExecutor.getResults();

      // extract language Feature Structure using AlchemyAPI Annotator
      FeatureStructure languageFS = UIMAUtils.getSingletonFeatureStructure(LanguageFS.type, jcas);

      language = languageFS.getStringValue(languageFS.getType().getFeatureByBaseName("language"));

    } catch (Exception e) {
      throw new UIMAException(e);
    }

    return language;
  }

  public List<Annotation> getCalaisAnnotations(String document) throws UIMAException {

    List<Annotation> calaisAnnotations = new ArrayList<Annotation>();

    try {

      // analyze the document
      uimaExecutor.analyzeDocument(document, "OpenCalaisAnnotator.xml", getParameterSetting());

      // get execution results
      JCas jcas = uimaExecutor.getResults();

      // extract entities using OpenCalaisAnnotator
      calaisAnnotations.addAll(UIMAUtils.getAllAnnotationsOfType(org.apache.uima.calais.BaseType.type, jcas));

    } catch (Exception e) {
      throw new UIMAException(e);
    }
    return calaisAnnotations;
  }

  public String getCategory(String document) throws UIMAException {
    String category = null;
    try {

      // analyze the document
      uimaExecutor.analyzeDocument(document, "TextCategorizationAEDescriptor.xml", getParameterSetting());

      // get execution results
      JCas jcas = uimaExecutor.getResults();

      // extract category Feature Structure using AlchemyAPI Annotator
      FeatureStructure categoryFS = UIMAUtils.getSingletonFeatureStructure(Category.type, jcas);

      category = categoryFS.getStringValue(categoryFS.getType().getFeatureByBaseName("text"));

    } catch (Exception e) {
      throw new UIMAException(e);
    }
    
    return category;
  }

  public Map<String, Object> getParameterSetting() {
    return parameterSetting;
  }

  public void setParameterSetting(Map<String, Object> parameterSetting) {
    this.parameterSetting = parameterSetting;
  }


}
