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

package org.apache.clerezza.uima.annotator;

import org.apache.clerezza.uima.utils.ts.WikipediaEntity;
import org.apache.clerezza.uima.utils.ts.WikipediaEntityAnnotation;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;

public class DummyWikipediaEntityAnnotator extends JCasAnnotator_ImplBase {

  @Override
  public void process(JCas jcas) throws AnalysisEngineProcessException {
    WikipediaEntity wikipediaEntity = new WikipediaEntity(jcas);
    wikipediaEntity.setUri("http://en.wikipedia.org/wiki/STS-135");
    wikipediaEntity.setLabel("STS-135");
    FSList stsAnnotationsList = findSTSAnnotations(jcas);
    wikipediaEntity.setReferences(stsAnnotationsList);
    wikipediaEntity.addToIndexes();

  }

  private FSList findSTSAnnotations(JCas jcas) {
    NonEmptyFSList annotations = new NonEmptyFSList(jcas);
    WikipediaEntityAnnotation annotation = new WikipediaEntityAnnotation(jcas);
    annotation.setBegin(0);
    annotation.setEnd(1);
    annotation.setUri("http://somesite.org/#STS135cit1");
    annotation.addToIndexes();
    annotations.setHead(annotation);
    return annotations;
  }

}
