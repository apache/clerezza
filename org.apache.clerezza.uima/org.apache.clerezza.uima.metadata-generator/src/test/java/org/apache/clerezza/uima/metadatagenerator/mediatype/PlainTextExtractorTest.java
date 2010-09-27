package org.apache.clerezza.uima.metadatagenerator.mediatype;

import org.junit.Assert;
import org.junit.Test;

/**
 * Reference test case for {@link org.apache.clerezza.uima.metadatagenerator.mediatype.PlainTextExtractor} class.
 *
 * @author Davide Palmisano
 */
public class PlainTextExtractorTest {

  @Test
  public void testExtract() throws UnsupportedMediaTypeException {
    final String TEXT = "just a simple test string";
    MediaTypeTextExtractor extractor = new PlainTextExtractor();
    String extracted = extractor.extract(TEXT.getBytes());
    Assert.assertNotNull(extracted);
    Assert.assertEquals(extracted, TEXT);
  }

}
