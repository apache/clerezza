package org.apache.clerezza.uima.utils;

import org.apache.clerezza.uima.utils.exception.ExecutionWithoutResultsException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * Executes a UIMA pipeline
 * 
 * @author tommaso
 * 
 */
public class UIMAExecutor {

  private transient final AEProvider aeProvider = new AEProvider();

  private boolean withResults;

  private JCas results;

  public UIMAExecutor() {
    withResults = false;
  }

  public UIMAExecutor withResults() {
    this.withResults = true;
    return this;
  }

  public void analyzeDocument(String doc) throws AnalysisEngineProcessException {
    analyzeDocument(doc, aeProvider.getDefaultXMLPath());
  }

  public void analyzeDocument(String doc, String xmlPath) throws AnalysisEngineProcessException {
    try {
      this.executeAE(aeProvider.getAE(xmlPath), doc);
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
