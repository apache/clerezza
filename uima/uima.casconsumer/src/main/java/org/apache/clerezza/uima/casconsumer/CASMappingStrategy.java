package org.apache.clerezza.uima.casconsumer;

import org.apache.uima.cas.CAS;

/**
 * a {@link CASMappingStrategy} defines a strategy for mapping a CAS object to an RDF model/file/object/store
 */
public interface CASMappingStrategy {

  /**
   * Converts a {@link CAS} object to an RDF object
   * @param cas the {@link CAS} object to convert
   * @param graphName the name of the graph to be created
   * @throws CASMappingException
   */
  void map(CAS cas, String graphName) throws CASMappingException;
}
