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
package org.apache.clerezza.uima.samples.services;

import org.apache.clerezza.rdf.core.Graph;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Testcase for {@link OpenNLPNERAOService}
 */
public class OpenNLPNERAOServiceTest {

  @Test
  public void serviceExecutionTest() {
    try {
      OpenNLPNERAOService service = new OpenNLPNERAOService();
      Graph graph = service.extractPersons(getClass().getResource("/ner_test_page.html").toURI().toString());
      assertNotNull(graph);
    } catch (Exception e) {
      fail(e.getLocalizedMessage());
    }
  }
}
