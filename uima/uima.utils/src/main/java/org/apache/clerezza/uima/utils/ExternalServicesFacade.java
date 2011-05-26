/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.clerezza.uima.utils;

import org.apache.uima.UIMAException;
import org.apache.uima.alchemy.ts.categorization.Category;
import org.apache.uima.alchemy.ts.concept.ConceptFS;
import org.apache.uima.alchemy.ts.keywords.KeywordFS;
import org.apache.uima.alchemy.ts.language.LanguageFS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Facade for querying UIMA external services
 */
public class ExternalServicesFacade implements UIMAServicesFacade {

  private UIMAExecutor uimaExecutor;

  private Map<String, Object> parameterSetting = new HashMap<String, Object>();

  public ExternalServicesFacade() {
    this.uimaExecutor = new UIMAExecutor("/ExtServicesAE.xml").withResults();
  }

  public List<FeatureStructure> getTags(String document) throws UIMAException {

    List<FeatureStructure> keywords = new ArrayList<FeatureStructure>();

    try {
      // analyze the document
      uimaExecutor.analyzeDocument(document, "/TextKeywordExtractionAEDescriptor.xml", getParameterSetting());

      // get execution results
      JCas jcas = uimaExecutor.getResults();

      // get AlchemyAPI keywords extracted using UIMA
      keywords.addAll(UIMAUtils.getAllFSofType(KeywordFS.type, jcas));

    } catch (Exception e) {
      throw new UIMAException(e);
    }

    return keywords;
  }

  public FeatureStructure getLanguage(String document) throws UIMAException {

    FeatureStructure languageFS = null;

    try {

      // analyze the document
      uimaExecutor.analyzeDocument(document, "/TextLanguageDetectionAEDescriptor.xml", getParameterSetting());

      // get execution results
      JCas jcas = uimaExecutor.getResults();

      // extract language Feature Structure using AlchemyAPI Annotator
      languageFS = UIMAUtils.getSingletonFeatureStructure(LanguageFS.type, jcas);

    } catch (Exception e) {
      throw new UIMAException(e);
    }

    return languageFS;
  }

  public List<FeatureStructure> getNamedEntities(String document) throws UIMAException {

    List<FeatureStructure> calaisAnnotations = new ArrayList<FeatureStructure>();

    try {

      // analyze the document
      uimaExecutor.analyzeDocument(document, "/OpenCalaisAnnotator.xml", getParameterSetting());

      // get execution results
      JCas jcas = uimaExecutor.getResults();

      // extract entities using OpenCalaisAnnotator
      calaisAnnotations.addAll(UIMAUtils.getAllAnnotationsOfType(org.apache.uima.calais.BaseType.type, jcas));

    } catch (Exception e) {
      throw new UIMAException(e);
    }
    return calaisAnnotations;
  }

  public FeatureStructure getCategory(String document) throws UIMAException {
    FeatureStructure categoryFS = null;
    try {

      // analyze the document
      uimaExecutor.analyzeDocument(document, "/TextCategorizationAEDescriptor.xml", getParameterSetting());

      // get execution results
      JCas jcas = uimaExecutor.getResults();

      // extract category Feature Structure using AlchemyAPI Annotator
      categoryFS = UIMAUtils.getSingletonFeatureStructure(Category.type, jcas);

    } catch (Exception e) {
      throw new UIMAException(e);
    }

    return categoryFS;
  }

  @Override
  public List<FeatureStructure> getConcepts(String document) throws UIMAException {
    List<FeatureStructure> concepts = new ArrayList<FeatureStructure>();

    try {
      // analyze the document
      uimaExecutor.analyzeDocument(document, "/TextConceptTaggingAEDescriptor.xml", getParameterSetting());

      // get execution results
      JCas jcas = uimaExecutor.getResults();

      // get AlchemyAPI concepts extracted using UIMA
      concepts.addAll(UIMAUtils.getAllFSofType(ConceptFS.type, jcas));

    } catch (Exception e) {
      throw new UIMAException(e);
    }

    return concepts;
  }

  public Map<String, Object> getParameterSetting() {
    return parameterSetting;
  }

  public void setParameterSetting(Map<String, Object> parameterSetting) {
    this.parameterSetting = parameterSetting;
  }


}
