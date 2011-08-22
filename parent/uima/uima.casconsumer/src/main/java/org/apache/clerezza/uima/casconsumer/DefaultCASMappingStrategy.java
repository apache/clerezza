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
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.uima.utils.UIMAUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.jcas.cas.TOP;

/**
 * the default implementation for {@link CASMappingStrategy}
 */
public class DefaultCASMappingStrategy implements CASMappingStrategy {

  @Override
  public void map(CAS cas, String graphName) throws CASMappingException {
    try {
      GraphNode node = createNode(graphName);
      UIMAUtils.enhanceNode(node, UIMAUtils.getAllFSofType(TOP.type, cas.getJCas()));
    } catch (Exception e) {
      throw new CASMappingException(e);
    }
  }

  private GraphNode createNode(String graphName) {
    final TcManager tcManager = TcManager.getInstance();
    final UriRef mGraphName = new UriRef(graphName);
    return new GraphNode(new BNode(), tcManager.createMGraph(mGraphName));
  }
}
