package org.apache.clerezza.uima.utils;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testcase for {@link UIMAExecutor}
 *
 */
public class UIMAExecutorTest {

  @Test
  public void testDefaultConstructor() {
    try {
      UIMAExecutor uimaExecutor = new UIMAExecutor("ExtServicesAE.xml");
      assertTrue(uimaExecutor != null);
    } catch (Throwable e) {
      fail(e.getLocalizedMessage());
    }
  }
  
  @Test
  public void testWithResultsConstructor() {
    try {
      UIMAExecutor uimaExecutor = new UIMAExecutor("ExtServicesAE.xml").withResults();
      assertTrue(uimaExecutor != null);
    } catch (Throwable e) {
      fail(e.getLocalizedMessage());
    }
  }

}
