package org.apache.clerezza.uima.utils;

import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.XMLInputSource;

/**
 * Enables execution of UIMA pipelines / AEs
 */
public interface UIMAExecutor {

    /**
     * analyze a text document using the default Analysis Engine
     *
     * @param doc
     * @throws org.apache.uima.analysis_engine.AnalysisEngineProcessException
     */
    public JCas analyzeDocument(String doc) throws AnalysisEngineProcessException, ResourceInitializationException;

    /**
     * analyze a text document specifying a different Analysis Engine descriptor path
     *
     * @param doc
     * @param xmlPath
     * @throws AnalysisEngineProcessException,
     *          ResourceInitializationException
     */
    public JCas analyzeDocument(String doc, String xmlPath) throws AnalysisEngineProcessException, ResourceInitializationException;

    /**
     * analyze a text document specifying a different Analysis Engine descriptor path and specific Analysis Engine parameter settings
     *
     * @param doc
     * @param xmlInputSource
     * @param aeParameterSettings
     * @throws AnalysisEngineProcessException,
     *          ResourceInitializationException
     */
    public JCas analyzeDocument(String doc, XMLInputSource xmlInputSource, Map<String, Object> aeParameterSettings) throws AnalysisEngineProcessException, ResourceInitializationException;
}
