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

import org.apache.clerezza.uima.casconsumer.ao.AOMappingStrategy;

import java.util.HashMap;
import java.util.Map;

/**
 * A repository for {@link CASMappingStrategy} implementations
 */
public class CASMappingStrategiesRepository {
  private static CASMappingStrategiesRepository instance = new CASMappingStrategiesRepository();

  private Map<String, CASMappingStrategy> strategies;

  public static CASMappingStrategiesRepository getInstance() {
    return instance;
  }

  private CASMappingStrategiesRepository() {
    initialize();
  }

  private void initialize() {
    strategies = new HashMap<String, CASMappingStrategy>();
    strategies.put(null, new DefaultCASMappingStrategy());
    strategies.put("", new DefaultCASMappingStrategy());
    strategies.put("default", new DefaultCASMappingStrategy());
    strategies.put("ao", new AOMappingStrategy());
  }

  public void register(CASMappingStrategy casMappingStrategy, String name) {
    strategies.put(name, casMappingStrategy);
  }

  public CASMappingStrategy getStrategy(String name) throws UnknownStrategyException {
    if (strategies.get(name) != null)
      return strategies.get(name);
    else
      throw new UnknownStrategyException(new StringBuilder("Could not find a strategy with name '").append(name).
              append("'").toString());
  }
}
