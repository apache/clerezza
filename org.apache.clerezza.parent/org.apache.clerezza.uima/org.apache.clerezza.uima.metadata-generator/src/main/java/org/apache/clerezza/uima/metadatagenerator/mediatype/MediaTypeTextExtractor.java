package org.apache.clerezza.uima.metadatagenerator.mediatype;

import javax.ws.rs.core.MediaType;

/**
 * A MediaTypeTextExtractor should extract text from a (list of) specified {@link javax.ws.rs.core.MediaType}
 */
public interface MediaTypeTextExtractor {

  public boolean supports(MediaType mediaType);

  public String extract(byte[] bytes)  throws UnsupportedMediaTypeException;

}
