package org.apache.clerezza.uima.utils;


import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.ProcessTrace;
import org.apache.uima.util.ProcessTraceEvent;

/**
 * Executes a UIMA pipeline
 *
 * @author tommaso
 *
 */
public class UIMAExecutor {

  private transient final AEProvider aeProvider = new AEProvider();

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
      ProcessTrace pt = ae.process(jcas);

      //TODO debug results
     
      jcas.reset();
    } catch (Exception e) {
      throw new AnalysisEngineProcessException(e);
    }

  }

}
