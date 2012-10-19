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

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLInputSource;

import java.util.Map;

/**
 * Executes UIMA pipelines collecting results in a {@link JCas}
 */
public class UIMAExecutor {

  private final AEProvider aeProvider;

  public UIMAExecutor(AEProvider aeProvider) {
    this.aeProvider = aeProvider;
  }

  /**
   * analyze a text document using the default Analysis Engine
   *
   * @param doc
   * @throws AnalysisEngineProcessException
   */
  public JCas analyzeDocument(String doc) throws AnalysisEngineProcessException, ResourceInitializationException {
    return executeAE(aeProvider.getDefaultAE(),doc);
  }

  /**
   * analyze a text document specifying a different Analysis Engine descriptor path
   *
   * @param doc
   * @param xmlPath
   * @throws AnalysisEngineProcessException,
   *          ResourceInitializationException
   */
  public JCas analyzeDocument(String doc, String xmlPath) throws AnalysisEngineProcessException, ResourceInitializationException {
    return executeAE(aeProvider.getAE(xmlPath), doc);
  }

  /**
   * analyze a text document specifying a different Analysis Engine descriptor path and specific Analysis Engine parameter settings
   *
   * @param doc
   * @param xmlInputSource
   * @param aeParameterSettings
   * @throws AnalysisEngineProcessException,
   *          ResourceInitializationException
   */
  public JCas analyzeDocument(String doc, XMLInputSource xmlInputSource, Map<String, Object> aeParameterSettings) throws AnalysisEngineProcessException, ResourceInitializationException {
    AnalysisEngine engine = aeProvider.getAEFromSource(xmlInputSource, aeParameterSettings);
    return executeAE(engine, doc);
  }

  private JCas executeAE(AnalysisEngine ae, String docText) throws AnalysisEngineProcessException, ResourceInitializationException {
    // create a JCas, given an Analysis Engine (ae)
    JCas jcas = ae.newJCas();

    // analyze a document text
    jcas.setDocumentText(docText);
    ae.process(jcas);

    return jcas;
  }


}
