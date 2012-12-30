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

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLInputSource;

import java.util.Map;

/**
 * Executes UIMA pipelines collecting results in a {@link JCas}
 */
@Service
public class InMemoryUIMAExecutor implements UIMAExecutor {

  @Reference
  private AEProvider aeProvider;

  /**
   * {@inheritDoc}
   */
  public JCas analyzeDocument(String doc) throws AnalysisEngineProcessException, ResourceInitializationException {
    return executeAE(aeProvider.getDefaultAE(),doc);
  }

  /**
   * {@inheritDoc}
   */
  public JCas analyzeDocument(String doc, String xmlPath) throws AnalysisEngineProcessException, ResourceInitializationException {
    return executeAE(aeProvider.getAE(xmlPath), doc);
  }

  /**
   * {@inheritDoc}
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
