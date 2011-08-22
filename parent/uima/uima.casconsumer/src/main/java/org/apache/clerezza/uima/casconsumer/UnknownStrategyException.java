package org.apache.clerezza.uima.casconsumer;

/**
 * Exception raised when an unrecognized (unknown or registered with wrong name) {@link CASMappingStrategy} is requested
 * to the {@link CASMappingStrategiesRepository}
 */
public class UnknownStrategyException extends Throwable {
  public UnknownStrategyException(String message) {
    super(message);
  }
}
