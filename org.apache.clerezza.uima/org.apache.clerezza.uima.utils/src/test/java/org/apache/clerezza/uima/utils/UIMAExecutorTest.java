package org.apache.clerezza.uima.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Testcase for {@link UIMAExecutor}
 * @author tommaso
 *
 */
public class UIMAExecutorTest {

  @Test
  public void testDefaultConstructor() {
    try {
      UIMAExecutor uimaExecutor = new UIMAExecutor();
      assertTrue(uimaExecutor != null);
    } catch (Throwable e) {
      fail(e.getLocalizedMessage());
    }
  }
  
  @Test
  public void testWithResultsConstructor() {
    try {
      UIMAExecutor uimaExecutor = new UIMAExecutor().withResults();
      assertTrue(uimaExecutor != null);
    } catch (Throwable e) {
      fail(e.getLocalizedMessage());
    }
  }

}
