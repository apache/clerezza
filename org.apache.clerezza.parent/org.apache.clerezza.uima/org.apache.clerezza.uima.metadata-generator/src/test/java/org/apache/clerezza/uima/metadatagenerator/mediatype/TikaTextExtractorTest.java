package org.apache.clerezza.uima.metadatagenerator.mediatype;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

/**
 * Reference test for {@link org.apache.clerezza.uima.metadatagenerator.mediatype.TikaTextExtractor} class.
 *
 * @author Davide Palmisano
 */
public class TikaTextExtractorTest {

  private TikaTextExtractor tikaTextExtractor;

  @Before
  public void setUp() {
    this.tikaTextExtractor = new TikaTextExtractor();
  }

  @After
  public void tearDown() {
    this.tikaTextExtractor = null;
  }

  @Test
  public void testExtract() throws UnsupportedMediaTypeException, IOException {
    InputStream inputStream = TikaTextExtractorTest.class.getResourceAsStream("clerezza-homepage.html");
    byte[] bytes = new byte[inputStream.available()];
    inputStream.read(bytes);
    String extracted = this.tikaTextExtractor.extract(bytes);
    Assert.assertNotNull(extracted);
    Assert.assertTrue(extracted.length() > 0);
  }

  /**
   * Tests the if all the {@link javax.ws.rs.core.MediaType}s already defined could be recognized.
   *
   * @throws UnsupportedMediaTypeException
   * @throws IllegalAccessException
   */
  @Test
  public void testSupports() throws UnsupportedMediaTypeException, IllegalAccessException {
    Class mediaTypeClass = MediaType.class;
    for (Field field : mediaTypeClass.getFields()) {
      if (field.getType().equals(MediaType.class)) {
        MediaType objectInstance = new MediaType();
        MediaType value = (MediaType) field.get(objectInstance);
        Assert.assertTrue(tikaTextExtractor.supports(value));
      }
    }
  }

}
