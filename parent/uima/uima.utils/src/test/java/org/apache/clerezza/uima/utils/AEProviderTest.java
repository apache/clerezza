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

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testcase for {@link AEProvider}
 * 
 */
public class AEProviderTest {

  private AEProvider aeProvider;

  @Before
  public void setUp() {
    this.aeProvider = new AEProvider();
  }

  @Test
  public void testGetDefaultAENotNull() {
    try {
      AnalysisEngine ae = this.aeProvider.getDefaultAE();
      assertTrue(ae != null);
    } catch (Throwable e) {
      fail(e.getLocalizedMessage());
    }
  }

  @Test
  public void testGetAEWithPathNotNull() {
    try {
      AnalysisEngine ae = this.aeProvider.getAE("/ExtServicesAE.xml");
      assertTrue(ae != null);
    } catch (Throwable e) {
      fail(e.getLocalizedMessage());
    }
  }

  @Test
  public void testGetAEWithWrongPath() {
    try {
      this.aeProvider.getAE("thisIsSomethingWeird");
      fail();
    } catch (Throwable e) {
    }
  }

}
