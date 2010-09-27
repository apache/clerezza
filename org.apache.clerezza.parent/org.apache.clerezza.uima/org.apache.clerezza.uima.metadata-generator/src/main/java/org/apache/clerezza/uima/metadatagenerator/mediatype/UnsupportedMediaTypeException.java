package org.apache.clerezza.uima.metadatagenerator.mediatype;

import javax.ws.rs.core.MediaType;

/**
 * When a {@link javax.ws.rs.core.MediaType} is not supported this exception is thrown
 */
public class UnsupportedMediaTypeException extends Exception {
  private static final String UNSUPPORTED = " is not supported";

  public UnsupportedMediaTypeException(String message) {
      super(message);
  }

  public UnsupportedMediaTypeException(String message, Exception e) {
      super(message, e);
  }

  public UnsupportedMediaTypeException(MediaType mediaType) {
    super(new StringBuilder(mediaType.getType()).append(UNSUPPORTED).toString());
  }
}
