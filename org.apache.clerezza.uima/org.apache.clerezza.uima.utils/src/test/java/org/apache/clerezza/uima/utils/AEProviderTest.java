package org.apache.clerezza.uima.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.junit.Before;
import org.junit.Test;

/**
 * Testcase for {@link AEProvider}
 * 
 * @author tommaso
 * 
 */
public class AEProviderTest {

  private AEProvider aeProvider;

  @Before
  public void setUp() {
    this.aeProvider = new AEProvider();
  }

  @Test
  public void testDefaulXMLPath() {
    try {
      String xmlPath = this.aeProvider.getDefaultXMLPath();
      assertTrue(xmlPath != null);
      assertTrue(xmlPath.equals("ExtServicesAE.xml"));
    } catch (Throwable e) {
      fail(e.getLocalizedMessage());
    }
  }

  @Test
  public void testGetDefaultAENotNull() {
    try {
      AnalysisEngine ae = this.aeProvider.getDefaultAE();
      assertTrue(ae != null);
    } catch (Throwable e) {
      fail(e.getLocalizedMessage());
    }
  }

  @Test
  public void testGetAEWithPathNotNull() {
    try {
      AnalysisEngine ae = this.aeProvider.getAE("ExtServicesAE.xml");
      assertTrue(ae != null);
    } catch (Throwable e) {
      fail(e.getLocalizedMessage());
    }
  }

  @Test
  public void testGetAEWithWrongPath() {
    try {
      this.aeProvider.getAE("thisIsSomethingWeird");
      fail();
    } catch (Throwable e) {
    }
  }

}
