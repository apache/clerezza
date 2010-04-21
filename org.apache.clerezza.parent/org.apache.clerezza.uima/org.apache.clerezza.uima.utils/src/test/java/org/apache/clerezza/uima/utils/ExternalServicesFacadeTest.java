package org.apache.clerezza.uima.utils;

import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Testcase for {@link ExternalServicesFacade}
 */
public class ExternalServicesFacadeTest {

    private static final String AN_ENGLISH_TEXT = "this is a document supposed to be recognized as written in the language of Queen Elizabeth";

    @Test
    public void getLanguageTest() {
        try {
            ExternalServicesFacade externalServicesFacade = new ExternalServicesFacade();
            Map<String, Object> parameterSettings = new HashMap<String, Object>();
            parameterSettings.put("apikey", "04490000a72fe7ec5cb3497f14e77f338c86f2fe");
            externalServicesFacade.setParameterSetting(parameterSettings);
            String language = externalServicesFacade.getLanguage(AN_ENGLISH_TEXT);
            assertEquals(language, "english");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }

    }


    @Test
    public void getTagsTest() {
        try {
            ExternalServicesFacade externalServicesFacade = new ExternalServicesFacade();
            Map<String, Object> parameterSettings = new HashMap<String, Object>();
            parameterSettings.put("apikey", "04490000a72fe7ec5cb3497f14e77f338c86f2fe");
            externalServicesFacade.setParameterSetting(parameterSettings);
            List<String> tags = externalServicesFacade.getTags(AN_ENGLISH_TEXT);
            assertTrue(tags != null);
            assertTrue(!tags.isEmpty());
            assertTrue(tags.size() == 1);
            assertTrue(tags.get(0).equals("document"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }

    }

    @Test
    public void getCalaisAnnotationsTest() {
        try {
            ExternalServicesFacade externalServicesFacade = new ExternalServicesFacade();
            String licenseId = "g6h9zamsdtwhb93nc247ecrs";
            Map<String, Object> parameterSettings = new HashMap<String, Object>();
            parameterSettings.put("licenseID", licenseId);
            externalServicesFacade.setParameterSetting(parameterSettings);
            List<Annotation> calaisAnnotations = externalServicesFacade.getCalaisAnnotations(AN_ENGLISH_TEXT);
            assertTrue(calaisAnnotations != null);
            assertTrue(!calaisAnnotations.isEmpty());
            assertTrue(calaisAnnotations.size() == 1);
            assertTrue(calaisAnnotations.get(0).getCoveredText().equals("Queen Elizabeth"));
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }

}
