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
package org.apache.clerezza.uima.casconsumer.ao;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.uima.casconsumer.CASMappingException;
import org.apache.clerezza.uima.casconsumer.CASMappingStrategy;
import org.apache.clerezza.uima.ontologies.annotationontology.AO;
import org.apache.clerezza.uima.ontologies.annotationontology.AOSELECTORS;
import org.apache.clerezza.uima.utils.UIMAUtils;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * {@link CASMappingStrategy} which maps a CAS object to the Annotation Ontology format
 */
public class AOMappingStrategy implements CASMappingStrategy {

  private final static Logger log = LoggerFactory.getLogger(AOMappingStrategy.class);

  @Override
  public void map(CAS cas, String graphName) throws CASMappingException {

    // This is creating the Graph for the annotation in Annotation Ontology format
    final TcManager tcManager = TcManager.getInstance();
    final UriRef mGraphName = new UriRef(graphName);

    GraphNode node = new GraphNode(new BNode(), tcManager.createMGraph(mGraphName));
    Lock lock = node.writeLock();
    try {
      lock.lock();

      SelectorRegistry selectorRegistry = new SelectorRegistry();
      // Iterate the annotations to create an index of them up front, this
      // is incase we have references between
      // annotations and need to output the appropriate RDF identifier out
      // of sequence
      Map<Annotation, Integer> annotIndex = new HashMap<Annotation, Integer>();
      int annotCnt = 0;
      for (FeatureStructure uimaObject : UIMAUtils.getAllFSofType(TOP.type, cas.getJCas())) {

        // set Annotation specific properties for the node
        if (uimaObject instanceof Annotation) {
          // If type is DocumentAnnotation I skip it
          if (uimaObject.getType().toString().equals("uima.tcas.DocumentAnnotation")) {
            continue;
          }

          // Get persistent URI for region in document
          Annotation annot = (Annotation) uimaObject;
          log.info("annotation index " + annotCnt);
          annotIndex.put(annot, annotCnt);
          annotCnt++;
        }
      }

      UriRef annotationSetUri = new UriRef(
        new StringBuilder(AO.AnnotationSet.getUnicodeString()).toString());
      if (log.isDebugEnabled())
        log.debug(new StringBuilder("AO: Annotation set uri ").append(annotationSetUri).toString());

      GraphNode annotationSet = new GraphNode(annotationSetUri, node.getGraph());
      if (log.isDebugEnabled())
        log.debug(new StringBuilder("AO: Set created ").toString());
      annotationSet.addProperty(new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
        AO.AnnotationSet);

      for (FeatureStructure uimaObject : UIMAUtils.getAllFSofType(TOP.type, cas.getJCas())) {

        // set Annotation specific properties for the node
        if (uimaObject instanceof Annotation) {

          // If type is DocumentAnnotation I skip it
          if (uimaObject.getType().toString().equals("uima.tcas.DocumentAnnotation")) {
            continue;
          }

          // Get persistent URI for region in document
          Annotation annot = (Annotation) uimaObject;
          UriRef selectorUri = selectorRegistry.get(annot.getBegin(), annot.getEnd());

          // Annotation URI
          int annotId = annotIndex.get((Annotation) uimaObject);
          UriRef annotationUri = new UriRef(new StringBuilder(AO.Annotation.getUnicodeString())
            .append("/").append(annotId).toString());
          if (log.isDebugEnabled())
            log.debug(new StringBuilder("annotation uri ").append(annotationUri).toString());

          // Annotation Graph
          GraphNode annotationNode = new GraphNode(annotationUri, annotationSet.getGraph());
          if (log.isDebugEnabled())
            log.debug(new StringBuilder("AO: Node created for Type ").append(
              uimaObject.getType().toString()).toString());
          annotationNode.addProperty(new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
            new UriRef("http://purl.org/ao/Annotation"));

          annotationNode.addProperty(AO.context, selectorUri);

          // finally add the triple to the existing node
          annotationSet.addProperty(AO.item, annotationNode.getNode());
        }
      }

      Iterator<SelectorRegistry.Selector> iterator = selectorRegistry.iterator();
      while (iterator.hasNext()) {

        SelectorRegistry.Selector sel = iterator.next();

        // Get persistent URI for region in document
        UriRef selectorUri = sel.uri;

        // create a new feature node
        GraphNode selectorNode = new GraphNode(selectorUri, node.getGraph());
        if (log.isDebugEnabled())
          log.debug(new StringBuilder("Node created for Selector ").append(selectorUri).toString());

        String documentText = cas.getDocumentText();
        selectorNode.addProperty(new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
          AO.Selector);
        selectorNode.addProperty(new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
          AOSELECTORS.OffsetRangeTextSelector);
        selectorNode.addProperty(new UriRef("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
          AOSELECTORS.PrefixPostfixTextSelector);
        selectorNode.addPropertyValue(AOSELECTORS.exact, getSpan(documentText, sel.start, sel.end));
        selectorNode.addPropertyValue(AOSELECTORS.prefix,
          getSpan(documentText, sel.start - 50, sel.start));
        selectorNode.addPropertyValue(AOSELECTORS.postfix,
          getSpan(documentText, sel.end, sel.end + 50));
        selectorNode.addPropertyValue(AOSELECTORS.offset, sel.start);
        selectorNode.addPropertyValue(AOSELECTORS.range, sel.end);
      }

      if (log.isDebugEnabled()) {
        TripleCollection tc = node.getGraph();
        for (Triple t : tc) {
          log.debug(t.toString());
        }
      }


    } catch (Exception e) {
      throw new CASMappingException(e);
    } finally {
      lock.unlock();
    }


  }

  /**
   * Given a documents text and a start and end offsets it returns an RDF value for the equivalent string literal. Annotations
   * have a convenience method for doing this but this method takes advantage of getting the text for the given document view once.
   *
   * @param documentText from which to extract the span of text
   * @param start        offset into the document text where the span starts if less than 0 will use zero instead
   * @param end          offset into the document text where the span ends. if the end goes beyond the length of the document text it is limited to the length
   * @return
   */

  private static String getSpan(String documentText, int start, int end) {
    if (start < 0) {
      start = 0;
    }
    if (end > documentText.length()) {
      end = documentText.length();
    }
    return documentText.substring(start, end);
  }
}
