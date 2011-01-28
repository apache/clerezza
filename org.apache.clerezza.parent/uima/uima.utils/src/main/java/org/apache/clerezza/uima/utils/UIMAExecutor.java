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

import org.apache.clerezza.uima.utils.exception.ExecutionWithoutResultsException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.util.Map;

/**
 * Executes a UIMA pipeline eventually collecting results in a {@link JCas}
 *
 */
public class UIMAExecutor {

    private transient final AEProvider aeProvider;

    private boolean withResults;

    private JCas results;

    @SuppressWarnings("unused")
    private UIMAExecutor() {
        // cannot instantiate without arguments
        aeProvider = null;
    }

    public UIMAExecutor(String xmlDescriptorPath) {
        withResults = false;
        aeProvider = new AEProvider(xmlDescriptorPath);
    }

    public UIMAExecutor withResults() {
        this.withResults = true;
        return this;
    }

    /**
     * analyze a text document using with this executor
     *
     * @param doc
     * @throws AnalysisEngineProcessException
     */
    public void analyzeDocument(String doc) throws AnalysisEngineProcessException {
        analyzeDocument(doc, aeProvider.getDefaultXMLPath());
    }

    /**
     * analyze a text document specifying a different Analysis Engine descriptor path
     *
     * @param doc
     * @param xmlPath
     * @throws AnalysisEngineProcessException
     */
    public void analyzeDocument(String doc, String xmlPath) throws AnalysisEngineProcessException {
        try {
            this.executeAE(aeProvider.getAE(xmlPath), doc);
        } catch (ResourceInitializationException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    /**
     * analyze a text document specifying a different Analysis Engine descriptor path and specific Analysis Engine parameter settings
     *
     * @param doc
     * @param xmlPath
     * @param aeParameterSettings
     * @throws AnalysisEngineProcessException
     */
    public void analyzeDocument(String doc, String xmlPath, Map<String, Object> aeParameterSettings) throws AnalysisEngineProcessException {
        try {
            AnalysisEngine engine = aeProvider.getAE(xmlPath,aeParameterSettings);
            this.executeAE(engine, doc);
        } catch (ResourceInitializationException e) {
            throw new AnalysisEngineProcessException(e);
        }
    }

    private void executeAE(AnalysisEngine ae, String docText) throws AnalysisEngineProcessException {
        try {
            // create a JCas, given an Analysis Engine (ae)
            JCas jcas = ae.newJCas();

            // analyze a document
            jcas.setDocumentText(docText);

            ae.process(jcas);

            if (withResults) {
                setResults(jcas);
            } else
                jcas.reset();
        } catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }

    }


    public JCas getResults() {
        if (!withResults)
            throw new ExecutionWithoutResultsException();
        return results;
    }

    private void setResults(JCas jcas) {
        this.results = jcas;

  }

}
