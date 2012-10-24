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

import org.apache.clerezza.uima.utils.cl.ClerezzaUIMAExtensionClassLoader;
import org.apache.clerezza.uima.utils.cl.UIMAResourcesClassLoaderRepository;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.resource.ResourceSpecifier;
import org.apache.uima.util.XMLInputSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * provide the {@link AnalysisEngine} using the default descriptor or using a custom descriptor (absolute)
 * path
 */
@Component
public class AEProvider {
  private final static Logger log = LoggerFactory.getLogger(AEProvider.class);

  private static String defaultXMLPath;
  private static Map<XMLInputSource, AnalysisEngine> registeredAEs;

  @Reference
  private UIMAResourcesClassLoaderRepository classLoaderRepository;

  public AEProvider() {
    defaultXMLPath = "/META-INF/ExtServicesAE.xml"; // if no default is specified use the bundled ext services descriptor
    registeredAEs = new HashMap<XMLInputSource, AnalysisEngine>();
  }

  public AEProvider withDefaultDescriptor(String xmlDescriptorPath) {
    defaultXMLPath = xmlDescriptorPath;
    return this;
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
    AnalysisEngine ae;
    // get Resource Specifier from XML file
    try {
      URL url = createURLFromPath(filePath);
      XMLInputSource in = new XMLInputSource(url);
      ResourceSpecifier specifier = UIMAFramework.getXMLParser().parseResourceSpecifier(in);
      // create AE here
      ae = UIMAFramework.produceAnalysisEngine(specifier);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }

    return ae;
  }

  private URL createURLFromPath(String filePath) throws MalformedURLException {
    // try classpath
    URL url = getClass().getResource(filePath);
    if (url == null) {
      for (ClassLoader c : classLoaderRepository.getComponents()) {
        url = c.getResource(filePath);
        if (url != null)
          break;
      }
    }

    // else try file
    if (url == null) {
      File f = new File(filePath);
      if (f.exists()) {
        url = f.toURI().toURL();
      } else
        throw new MalformedURLException();
    }
    return url;
  }

  public AnalysisEngine getAEFromSource(XMLInputSource xmlInputSource, Map<String, Object> parameterSettings) throws ResourceInitializationException {
    AnalysisEngine ae = null;
    try {
      AnalysisEngine cachedAE = registeredAEs.get(xmlInputSource);
      if (cachedAE != null) {
        cachedAE.reconfigure();
        return cachedAE;
      } else {
        // eventually add/override descriptor's configuration parameters
        AnalysisEngineDescription desc = UIMAFramework.getXMLParser().parseAnalysisEngineDescription(xmlInputSource);
        for (String parameter : parameterSettings.keySet()) {
          if (desc.getAnalysisEngineMetaData().getConfigurationParameterSettings().getParameterValue(parameter) != null)
            desc.getAnalysisEngineMetaData().getConfigurationParameterSettings().setParameterValue(parameter, parameterSettings.get(parameter));
        }

        // create AE here
        try {
          ae = UIMAFramework.produceAnalysisEngine(desc);
        } catch (Exception e) {
          log.warn(new StringBuilder("could not get AE from default RM \n ").append(e.getMessage()).toString());
        }
        if (ae == null) {
          try {
            ResourceManager rm = UIMAFramework.newDefaultResourceManager();
            rm.setExtensionClassPath(new ClerezzaUIMAExtensionClassLoader(getClass().getClassLoader(), classLoaderRepository.getComponents()), "*", true);
            ae = UIMAFramework.produceAnalysisEngine(desc, rm, null);
          } catch (Exception e) {
            log.warn(new StringBuilder("could not get AE from extended classpath RM \n ").append(e.getMessage()).toString());
          }
        }


        if (ae == null) {
          throw new AEInstantiationException();
        } else {
          registeredAEs.put(xmlInputSource, ae);
        }
      }
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }

    return ae;
  }

  private static class AEInstantiationException extends Exception {
    public AEInstantiationException() {
      super();
    }
  }
}