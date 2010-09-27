package org.apache.clerezza.uima.metadatagenerator.mediatype;

import javax.ws.rs.core.MediaType;

/**
 * Base implementation of {@link javax.ws.rs.core.MediaType}
 */
public class PlainTextExtractor implements MediaTypeTextExtractor {

  /**
   * {@inheritDoc}
   */
  public boolean supports(MediaType mediaType) {
    return mediaType != null && mediaType.getType().equals(MediaType.TEXT_PLAIN_TYPE.getType()) &&
            mediaType.getSubtype().equals(MediaType.TEXT_PLAIN_TYPE.getSubtype());
  }

  /**
   * {@inheritDoc}
   */
  public String extract(byte[] bytes) throws UnsupportedMediaTypeException {
    return new String(bytes);
  }
}
