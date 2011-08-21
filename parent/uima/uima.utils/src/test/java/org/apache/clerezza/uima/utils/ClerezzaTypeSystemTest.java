package org.apache.clerezza.uima.utils;

import org.apache.clerezza.uima.utils.ts.WikipediaEntityAnnotation;
import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.test.junit_extension.AnnotatorTester;
import org.apache.uima.util.XMLInputSource;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClerezzaTypeSystemTest {

  @Test
  public void parsingTest() {
    try {
      TypeSystemDescription tsd = UIMAFramework.getXMLParser().parseTypeSystemDescription(
              new XMLInputSource(getClass().getResource("/ClerezzaTestTypeSystemDescriptor.xml")));
      assertNotNull(tsd);
      assertNotNull(tsd.getType("org.apache.clerezza.uima.utils.ts.WikipediaEntity"));
      assertNotNull(tsd.getType("org.apache.clerezza.uima.utils.ts.WikipediaEntityAnnotation"));
    } catch (Exception e) {
      fail(e.getLocalizedMessage());
    }
  }

  @Test
  public void dummyTypeSystemWithAnnotatorTest() {
    try {
      CAS cas = AnnotatorTester.performTest("src/test/resources/SampleWikipediaAEDescriptor.xml",
              "this is useless", null);
      assertNotNull(cas);
      /* check annotations */
      AnnotationIndex<Annotation> annotationIndex = cas.getJCas().getAnnotationIndex(
              WikipediaEntityAnnotation.type);
      assertNotNull(annotationIndex);
      assertTrue(annotationIndex.size() == 1);
      /* check entities */
      Type type = cas.getTypeSystem().getType("org.apache.clerezza.uima.utils.ts.WikipediaEntity");
      FSIterator<FeatureStructure> entities = cas.getJCas().getIndexRepository()
              .getAllIndexedFS(type);
      assertTrue(entities.hasNext());
      while (entities.hasNext()) {
        FSList references = (FSList) entities.next().getFeatureValue(
                type.getFeatureByBaseName("references"));
        assertNotNull(references.getNthElement(0));
      }
    } catch (Exception e) {
      fail(e.getLocalizedMessage());
    }
  }
}
