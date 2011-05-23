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

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.admin.CASFactory;
import org.apache.uima.cas.admin.CASMgr;
import org.apache.uima.cas.admin.FSIndexRepositoryMgr;
import org.apache.uima.cas.admin.TypeSystemMgr;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.test.junit_extension.AnnotatorTester;
import org.apache.uima.util.CasCreationUtils;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * TestCase for {@link ClerezzaCASConsumer}
 */
public class ClerezzaCASConsumerTest {

  @Test
  public void configurationTest() {
    try {
      AnnotatorTester.doConfigurationTest("src/main/resources/ClerezzaCASConsumerDescriptor.xml");
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getLocalizedMessage());
    }
  }

  @Test
  public void simpleRunningTest() {
    try {
      AnnotatorTester annotatorTester = new AnnotatorTester("src/test/resources/TestClerezzaCASConsumerDescriptor.xml");

      /* create a mock CAS */
      CAS cas = createCAS();

      cas.setDocumentText("Clerezza is an Apache project");
      cas.setDocumentLanguage("en");

      AnnotationFS annotation = cas.createAnnotation(cas.getAnnotationType(), 0, 9);
      cas.addFsToIndexes(annotation);

      /* execute ClerezzaCASConsumer on the created CAS */
      annotatorTester.performTest(cas);

      MGraph createdGraph = TcManager.getInstance().getMGraph(new UriRef("mytest-clerezza-uima-graph"));
      assertNotNull(createdGraph);
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getLocalizedMessage());
    }
  }

  private CAS createCAS() throws ResourceInitializationException, CASException {
    CASMgr casMgr0 = CASFactory.createCAS();
    CASMgr casMgr = null;
    CasCreationUtils.setupTypeSystem(casMgr0, null);

    TypeSystemMgr tsa = casMgr0.getTypeSystemMgr();

    ((CASImpl) casMgr0).commitTypeSystem();

    casMgr = CASFactory.createCAS(tsa);

    casMgr.initCASIndexes();
    FSIndexRepositoryMgr irm = casMgr.getIndexRepositoryMgr();
    irm.commit();

    return casMgr.getCAS().getCurrentView();
  }
}
