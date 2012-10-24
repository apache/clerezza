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
package org.apache.clerezza.uima.utils.cl;

import org.apache.felix.scr.annotations.Service;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Repository for UIMA {@link AnalysisComponent}s' {@link ClassLoader}s
 */
@Service
public class UIMAResourcesClassLoaderRepository {
  private final static Logger log = LoggerFactory.getLogger(UIMAResourcesClassLoaderRepository.class);

  private final static Set<ClassLoader> registeredComponents = new HashSet<ClassLoader>();

  public <C extends AnalysisComponent> void registerComponent(Class<C> component) {
    log.info(new StringBuilder("Component ").append(component.getName()).append(" registered").toString());
    registeredComponents.add(component.getClassLoader());
  }

  public <R extends Resource> void registerResource(Class<R> component) {
    log.info(new StringBuilder("Component ").append(component.getName()).append(" registered").toString());
    registeredComponents.add(component.getClassLoader());
  }

  public Collection<ClassLoader> getComponents() {
    return registeredComponents;
  }
}
