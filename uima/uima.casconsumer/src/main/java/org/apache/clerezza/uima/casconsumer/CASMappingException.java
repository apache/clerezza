package org.apache.clerezza.uima.casconsumer;

/**
 * An {@link Exception} thrown when mapping a CAS model to RDF
 */
public class CASMappingException extends Exception {
  public CASMappingException(Throwable cause) {
    super(cause);
  }
}
