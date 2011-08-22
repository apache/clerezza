package org.apache.clerezza.uima.casconsumer;

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
