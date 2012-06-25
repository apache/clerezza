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

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.admin.CASFactory;
import org.apache.uima.cas.admin.CASMgr;
import org.apache.uima.cas.admin.FSIndexRepositoryMgr;
import org.apache.uima.cas.admin.TypeSystemMgr;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.jcas.tcas.DocumentAnnotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testcase for {@link UIMAUtils}
 */
public class UIMAUtilsTest {
  private static final String DOCUMENT_TEXT = "the server will return a \"A concept with the same label and language already exists!\", so there are actually 2 issues:";

  @Test
  public void testGetAllFSOfAnnotationType() {
    try {
      JCas cas = getCAS().getJCas();
      cas.setDocumentText(DOCUMENT_TEXT);

      FeatureStructure firstFeatureStructure = new TOP(cas);
      cas.addFsToIndexes(firstFeatureStructure);
      FeatureStructure secondFeatureStructure = new TOP(cas);
      cas.addFsToIndexes(secondFeatureStructure);

      List<FeatureStructure> featureStructures = UIMAUtils.getAllFSofType(TOP.type, cas);

      assertTrue(featureStructures != null);
      assertTrue(!featureStructures.isEmpty());
      assertTrue(featureStructures.size() == 3); // two simple FSs and the DocumentAnnotation, both extend TOP
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.toString());
    }

  }

  @Test
  public void testGetAllAnnotationsOfCustomType() {
    try {
      JCas cas = getCAS().getJCas();
      cas.setDocumentText(DOCUMENT_TEXT);

      Annotation simpleAnnotation = new Annotation(cas);
      simpleAnnotation.setBegin(10);
      simpleAnnotation.setEnd(24);
      simpleAnnotation.addToIndexes();

      Annotation secondSimpleAnnotation = new Annotation(cas);
      secondSimpleAnnotation.setBegin(32);
      secondSimpleAnnotation.setEnd(44);
      secondSimpleAnnotation.addToIndexes();

      List<Annotation> foundAnnotations = UIMAUtils.getAllAnnotationsOfType(DocumentAnnotation.type, cas);

      assertTrue(foundAnnotations != null);
      assertTrue(!foundAnnotations.isEmpty());
      assertTrue(foundAnnotations.size() == 1);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.toString());
    }

  }

  @Test
  public void testGetSingletonFS() {
    try {
      JCas cas = getCAS().getJCas();
      cas.setDocumentText(DOCUMENT_TEXT);
      FeatureStructure documentAnnotation = UIMAUtils.getSingletonFeatureStructure(DocumentAnnotation.type, cas);
      assertTrue(documentAnnotation != null);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.toString());
    }
  }

  @Test
  public void testFailingGetSingletonFS() {
    try {
      JCas cas = getCAS().getJCas();
      cas.setDocumentText(DOCUMENT_TEXT);
      Annotation simpleAnnotation = new Annotation(cas);
      simpleAnnotation.setBegin(10);
      simpleAnnotation.setEnd(24);
      simpleAnnotation.addToIndexes();

      Annotation secondSimpleAnnotation = new Annotation(cas);
      secondSimpleAnnotation.setBegin(32);
      secondSimpleAnnotation.setEnd(44);
      secondSimpleAnnotation.addToIndexes();

      UIMAUtils.getSingletonFeatureStructure(Annotation.type, cas);
      fail("should raise exception since there are 3 annotations of type Annotation");
    } catch (Exception e) {
      // if here, test passed
    }
  }

  @Test
  public void testEnhanceNode() {

    try {
      JCas cas = getCAS().getJCas();
      cas.setDocumentText(DOCUMENT_TEXT);

      Annotation simpleAnnotation = new Annotation(cas);
      simpleAnnotation.setBegin(10);
      simpleAnnotation.setEnd(24);
      simpleAnnotation.addToIndexes();

      Annotation secondSimpleAnnotation = new Annotation(cas);
      secondSimpleAnnotation.setBegin(32);
      secondSimpleAnnotation.setEnd(44);
      secondSimpleAnnotation.addToIndexes();

      MGraph mGraph = new SimpleMGraph();
      GraphNode node = new GraphNode(new UriRef(cas.toString()), mGraph);

      UIMAUtils.enhanceNode(node, UIMAUtils.getAllAnnotationsOfType(Annotation.type, cas));


    } catch (Exception e) {
      fail(e.getLocalizedMessage());
    }

  }


  private static CAS getCAS() {
    // Create an initial CASMgr from the factory.
    CASMgr casMgr0 = CASFactory.createCAS();
    CASMgr casMgr = null;
    try {
      // this call does nothing: because 2nd arg is null
      CasCreationUtils.setupTypeSystem(casMgr0, null);
      // Create a writable type system.
      TypeSystemMgr tsa = casMgr0.getTypeSystemMgr();

      // Commit the type system.
      ((CASImpl) casMgr0).commitTypeSystem();

      casMgr = CASFactory.createCAS(tsa);

      // Create the Base indexes.
      casMgr.initCASIndexes();
      // Commit the index repository.
      FSIndexRepositoryMgr irm = casMgr.getIndexRepositoryMgr();

      irm.commit();
    } catch (ResourceInitializationException e) {
      e.printStackTrace();
    } catch (CASException e) {
      e.printStackTrace();
    }

    // Create the default text Sofa and return CAS view
    return casMgr.getCAS().getCurrentView();
  }

}
