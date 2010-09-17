package org.apache.clerezza.uima.utils;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.XMLInputSource;

import java.net.URL;
import java.util.Map;

/**
 * provide the AnalysisEngine using the default descriptor or using a custom descriptor (absolute)
 * path
 */
public class AEProvider {

  private static String defaultXMLPath;

  public AEProvider() {
    defaultXMLPath = "ExtServicesAE.xml";
  }

  public AEProvider(String xmlDescriptorPath) {
    defaultXMLPath = xmlDescriptorPath;
  }

  public String getDefaultXMLPath() {
    return defaultXMLPath;
  }

  /**
   * get an Analysis Engine using the default path (specified in constructor)
   *
   * @return
   * @throws ResourceInitializationException
   *
   */
  public AnalysisEngine getDefaultAE() throws ResourceInitializationException {
    return getAE(defaultXMLPath);
  }

  /**
   * get an Analysis Engine from a different descriptor path
   *
   * @param filePath
   * @return
   * @throws ResourceInitializationException
   *
   */
  public AnalysisEngine getAE(String filePath) throws ResourceInitializationException {
    AnalysisEngine ae = null;
    // get Resource Specifier from XML file
    try {
      URL url = this.getClass().getClassLoader().getResource(filePath);
      XMLInputSource in = new XMLInputSource(url);
      ResourceSpecifier specifier = UIMAFramework.getXMLParser().parseResourceSpecifier(in);
      // create AE here
      ae = UIMAFramework.produceAnalysisEngine(specifier);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }

    return ae;
  }

  public AnalysisEngine getAE(String filePath, Map<String, Object> parameterSettings) throws ResourceInitializationException {
    AnalysisEngine ae = null;
    // get Resource Specifier from XML file
    try {
      URL url = this.getClass().getClassLoader().getResource(filePath);
      XMLInputSource in = new XMLInputSource(url);

      // eventually add/override descriptor's configuration parameters
      AnalysisEngineDescription desc = UIMAFramework.getXMLParser().parseAnalysisEngineDescription(in);
      for (String parameter : parameterSettings.keySet()) {
        if (desc.getAnalysisEngineMetaData().getConfigurationParameterSettings().getParameterValue(parameter)!=null)
          desc.getAnalysisEngineMetaData().getConfigurationParameterSettings().setParameterValue(parameter,parameterSettings.get(parameter));
      }

      // create AE here
      ae = UIMAFramework.produceAnalysisEngine(desc);

    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }

    return ae;
  }
}