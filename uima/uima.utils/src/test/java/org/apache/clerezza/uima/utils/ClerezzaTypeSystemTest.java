package org.apache.clerezza.uima.utils;
/*
 *
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
 *
*/


import org.apache.clerezza.uima.utils.ts.WikipediaEntityAnnotation;
import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.test.junit_extension.AnnotatorTester;
import org.apache.uima.util.XMLInputSource;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClerezzaTypeSystemTest {

  @Test
  public void parsingTest() throws Exception {
    TypeSystemDescription tsd = UIMAFramework.getXMLParser().parseTypeSystemDescription(
            new XMLInputSource(getClass().getResource("/ClerezzaTestTypeSystemDescriptor.xml")));
    assertNotNull(tsd);
    assertNotNull(tsd.getType("org.apache.clerezza.uima.utils.ts.WikipediaEntity"));
    assertNotNull(tsd.getType("org.apache.clerezza.uima.utils.ts.WikipediaEntityAnnotation"));
  }

  @Test
  public void dummyTypeSystemWithAnnotatorTest() throws Exception {
    CAS cas = AnnotatorTester.performTest("src/test/resources/SampleWikipediaAEDescriptor.xml",
            "this is useless", null);
    assertNotNull(cas);
    /* check annotations */
    AnnotationIndex<Annotation> annotationIndex = cas.getJCas().getAnnotationIndex(
            WikipediaEntityAnnotation.type);
    assertNotNull(annotationIndex);
    assertTrue(annotationIndex.size() == 1);
    /* check entities */
    Type type = cas.getTypeSystem().getType("org.apache.clerezza.uima.utils.ts.WikipediaEntity");
    FSIterator<FeatureStructure> entities = cas.getJCas().getIndexRepository()
            .getAllIndexedFS(type);
    assertTrue(entities.hasNext());
    while (entities.hasNext()) {
      FSList references = (FSList) entities.next().getFeatureValue(
              type.getFeatureByBaseName("references"));
      assertNotNull(references.getNthElement(0));
    }
  }
}
