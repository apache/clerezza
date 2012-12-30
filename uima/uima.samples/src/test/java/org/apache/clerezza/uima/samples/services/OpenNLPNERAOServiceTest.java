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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.felix;
import static org.ops4j.pax.exam.CoreOptions.frameworks;
import static org.ops4j.pax.exam.CoreOptions.mavenConfiguration;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.configProfile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.dsProfile;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.webProfile;
import static org.ops4j.pax.exam.junit.JUnitOptions.junitBundles;

/**
 * Testcase for {@link OpenNLPNERAOService}
 */
@Ignore
@RunWith(JUnit4TestRunner.class)
public class OpenNLPNERAOServiceTest {

  @Configuration
  public static Option[] configuration() {
    return options(
            mavenConfiguration(),
            dsProfile(),
            configProfile(),
            webProfile(),
            junitBundles(),
            frameworks(felix()));
  }




  @Inject
  private BundleContext bundleContext;

  @Test
  public void serviceExecutionTest() throws Exception {
    ServiceReference serviceReference = bundleContext.getServiceReference(OpenNLPService.class.getName());
    assertNotNull(serviceReference);
//    OpenNLPNERAOService service = new OpenNLPNERAOService();
//    Graph graph = service.extractPersons(getClass().getResource("/ner_test_page.html").toURI().toString());
//    assertNotNull(graph);
//    assertFalse(graph.isEmpty());
  }
}
