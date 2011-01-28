/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.clerezza.uima.utils;

import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
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

    private static final String CLEREZZA_RELATED_TEXT = "Clerezza is fully based on OSGi. OSGi is a very lightweight approach to offer the modularization and dynamism missing in standard Java. By using OSGi services it can also interoperate with Spring-DS or Peaberry applications";

    private static final String ANOTHER_ENGLISH_TEXT ="President Obama vows to \"make BP pay\" for the Gulf oil spill, and says the US must end its fossil fuel \"addiction\".";

    @Test
    public void getLanguageTest() {
        try {
            ExternalServicesFacade externalServicesFacade = new ExternalServicesFacade();
            Map<String, Object> parameterSettings = new HashMap<String, Object>();
            parameterSettings.put("apikey", "04490000a72fe7ec5cb3497f14e77f338c86f2fe");
            externalServicesFacade.setParameterSetting(parameterSettings);
            FeatureStructure languageFS = externalServicesFacade.getLanguage(AN_ENGLISH_TEXT);
            String language = languageFS.getStringValue(languageFS.getType().getFeatureByBaseName("language"));
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
            List<FeatureStructure> tags = externalServicesFacade.getTags(AN_ENGLISH_TEXT);
            assertTrue(tags != null);
            assertTrue(!tags.isEmpty());
            assertTrue(tags.size() == 1);
            FeatureStructure keyword = tags.get(0);
            Type type = keyword.getType();
            String tagText = keyword.getStringValue(type.getFeatureByBaseName("text"));
            assertTrue(tagText!=null && tagText.equals("document"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }

    }

    @Test
    public void getNamedEntitiesTest() {
        try {
            ExternalServicesFacade externalServicesFacade = new ExternalServicesFacade();
            String licenseId = "g6h9zamsdtwhb93nc247ecrs";
            Map<String, Object> parameterSettings = new HashMap<String, Object>();
            parameterSettings.put("licenseID", licenseId);
            externalServicesFacade.setParameterSetting(parameterSettings);
            List<FeatureStructure> entities = externalServicesFacade.getNamedEntities(ANOTHER_ENGLISH_TEXT);
            assertTrue(entities != null);
            assertTrue(!entities.isEmpty());
            for (FeatureStructure fs : entities) {
              Annotation annotation = (Annotation) fs;
              assertTrue(annotation.getType()!=null && annotation.getType().getName()!=null);
              assertTrue(annotation.getBegin()>0);
              assertTrue(annotation.getEnd()>0);
              assertTrue(annotation.getCoveredText()!=null);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }

    @Test
    public void getCategoryTest() {
       try {
            ExternalServicesFacade externalServicesFacade = new ExternalServicesFacade();
            Map<String, Object> parameterSettings = new HashMap<String, Object>();
            parameterSettings.put("apikey", "04490000a72fe7ec5cb3497f14e77f338c86f2fe");
            externalServicesFacade.setParameterSetting(parameterSettings);
            FeatureStructure categoryFS = externalServicesFacade.getCategory(CLEREZZA_RELATED_TEXT);
            String category = categoryFS.getStringValue(categoryFS.getType().getFeatureByBaseName("text"));
            assertEquals(category, "computer_internet");
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }

    @Test
    public void getConceptsTest() {
        try {
            ExternalServicesFacade externalServicesFacade = new ExternalServicesFacade();
            Map<String, Object> parameterSettings = new HashMap<String, Object>();
            parameterSettings.put("apikey", "04490000a72fe7ec5cb3497f14e77f338c86f2fe");
            externalServicesFacade.setParameterSetting(parameterSettings);
            List<FeatureStructure> concepts = externalServicesFacade.getConcepts(ANOTHER_ENGLISH_TEXT);
            assertTrue(concepts != null);
            assertTrue("Concepts list is empty",!concepts.isEmpty());
            assertTrue("Concepts list size is "+concepts.size(),concepts.size() == 8);
            FeatureStructure concept = concepts.get(0);
            Type type = concept.getType();
            String conceptText = concept.getStringValue(type.getFeatureByBaseName("text"));
            assertTrue("First concept was"+conceptText+" instead of Petroleum",conceptText!=null && conceptText.equals("Petroleum"));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }

    }

}
