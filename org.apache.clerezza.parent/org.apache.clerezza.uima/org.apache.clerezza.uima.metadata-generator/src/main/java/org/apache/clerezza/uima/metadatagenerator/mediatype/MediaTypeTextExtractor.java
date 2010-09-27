package org.apache.clerezza.uima.metadatagenerator.mediatype;

import javax.ws.rs.core.MediaType;

/**
 * A MediaTypeTextExtractor extracts text from a (list of) specified {@link javax.ws.rs.core.MediaType}.
 */
public interface MediaTypeTextExtractor {

  /**
   * Check if the provided {@link javax.ws.rs.core.MediaType} is supported by this extractor.
   * 
   * @param mediaType to be checked.
   * @return <code>true</code> if the provided {@link javax.ws.rs.core.MediaType} as input is supported.
   */
  public boolean supports(MediaType mediaType);

  /**
   * Extract the text from the provided input if its <i>Media Type</i> is supported.
   *
   * @param bytes an array of <code>byte</code> representing the input.
   * @return a {@link String} with the extracted text.
   * @throws UnsupportedMediaTypeException if the input implicit <i>Media type</i> is not supported.
   */
  public String extract(byte[] bytes)  throws UnsupportedMediaTypeException;

}
