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

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.uima.utils.UIMAUtils;
import org.apache.clerezza.uima.utils.exception.FeatureStructureNotFoundException;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.resource.ResourceInitializationException;

/**
 * A UIMA CASConsumer which consumes a {@link CAS} to a Clerezza {@link TripleCollection}
 */
public class ClerezzaCASConsumer extends CasAnnotator_ImplBase {

  private String graphName;
  private String casModelFilePath;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    // get the RDF model
    casModelFilePath = String.valueOf(context.getConfigParameterValue("casModelFile"));

    // get the output graph name
    graphName = String.valueOf(context.getConfigParameterValue("graphName"));
  }

  @Override
  public void process(CAS cas) throws AnalysisEngineProcessException {
    // create the output graph
    MGraph outputGraph = createGraph();

    // create the root node
    GraphNode sample = new GraphNode(new BNode(), outputGraph);

    // if no casModel was specified use the default mapping
    if (casModelFilePath==null || casModelFilePath.length()==0) {
      try {
        UIMAUtils.enhanceNode(sample, UIMAUtils.getAllFSofType(TOP.type, cas.getJCas()));
      } catch (FeatureStructureNotFoundException e) {
        throw new AnalysisEngineProcessException(e);
      } catch (CASException e) {
        throw new AnalysisEngineProcessException(e);
      }
    }
    // TODO otherwise map UIMA FeatureStructures to Triples using the given casModel

  }

  private MGraph createGraph() {
    final TcManager tcManager = TcManager.getInstance();
    final UriRef mGraphName = new UriRef(graphName);
    return tcManager.createMGraph(mGraphName);
  }
}
