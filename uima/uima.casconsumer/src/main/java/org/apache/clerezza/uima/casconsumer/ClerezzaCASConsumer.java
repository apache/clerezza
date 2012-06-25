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
package org.apache.clerezza.uima.casconsumer;

import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * A UIMA CASConsumer which consumes a {@link CAS} to a Clerezza {@link TripleCollection}
 */
public class ClerezzaCASConsumer extends CasAnnotator_ImplBase {

  private static final String MAPPING_STRATEGY = "mappingStrategy";
  private static final String GRAPH_NAME = "graphName";

  private String graphName;
  private CASMappingStrategy mappingStrategy;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    // get the CAS mapping strategy
    try {
      mappingStrategy = CASMappingStrategiesRepository.getInstance().getStrategy(String.valueOf(context.
        getConfigParameterValue(MAPPING_STRATEGY)));
    } catch (UnknownStrategyException e) {
      throw new ResourceInitializationException(e);
    }

    // get the output graph name
    graphName = String.valueOf(context.getConfigParameterValue(GRAPH_NAME));
  }

  @Override
  public void process(CAS cas) throws AnalysisEngineProcessException {
    try {
      mappingStrategy.map(cas, graphName);
    } catch (CASMappingException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

}
