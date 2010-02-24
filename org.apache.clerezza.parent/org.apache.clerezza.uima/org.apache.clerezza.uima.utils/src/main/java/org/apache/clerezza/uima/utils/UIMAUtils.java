package org.apache.clerezza.uima.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.clerezza.uima.utils.exception.NotSingletonFeatureStructureException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.jcas.JCas;

/**
 * Utilities for managing UIMA data and features
 * @author tommaso
 *
 */
public class UIMAUtils {
  
  public static List<FeatureStructure> getAllFSofType(int type,JCas cas) {
    List<FeatureStructure> featureStructures = new ArrayList<FeatureStructure>();
    for (FSIterator<FeatureStructure> it = cas.getFSIndexRepository().getAllIndexedFS(cas.getCasType(type)); it.hasNext();) {
      featureStructures.add(it.next());
    }
    return featureStructures;
  }

  public static FeatureStructure getSingletonFeatureStructure(int type, JCas cas) throws NotSingletonFeatureStructureException {
    FeatureStructure featureStructure = null;
    for (FSIterator<FeatureStructure> it = cas.getFSIndexRepository().getAllIndexedFS(cas.getCasType(type)); it.hasNext();) {
      featureStructure = it.next();
      if (it.hasNext())
        throw new NotSingletonFeatureStructureException();
    }
    return featureStructure;
  }

}
