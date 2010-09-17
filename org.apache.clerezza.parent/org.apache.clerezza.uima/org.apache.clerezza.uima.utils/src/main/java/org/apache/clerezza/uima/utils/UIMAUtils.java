package org.apache.clerezza.uima.utils;

import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.uima.ontologies.ENTITY;
import org.apache.clerezza.uima.utils.exception.FeatureStructureNotFoundException;
import org.apache.clerezza.uima.utils.exception.NotSingletonFeatureStructureException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Utilities for managing UIMA data and features
 */
public class UIMAUtils {

  private final static Logger log = LoggerFactory.getLogger(UIMAUtils.class);

  public static List<FeatureStructure> getAllFSofType(int type, JCas cas)
          throws FeatureStructureNotFoundException {
    List<FeatureStructure> featureStructures = new ArrayList<FeatureStructure>();
    for (FSIterator<FeatureStructure> it = cas.getFSIndexRepository().getAllIndexedFS(
            cas.getCasType(type)); it.hasNext();) {
      featureStructures.add(it.next());
    }

    if (featureStructures.isEmpty())
      throw new FeatureStructureNotFoundException();
    return featureStructures;
  }

  public static FeatureStructure getSingletonFeatureStructure(int type, JCas cas)
          throws NotSingletonFeatureStructureException, FeatureStructureNotFoundException {
    FeatureStructure featureStructure = null;
    for (FSIterator<FeatureStructure> it = cas.getFSIndexRepository().getAllIndexedFS(
            cas.getCasType(type)); it.hasNext();) {
      featureStructure = it.next();
      if (it.hasNext())
        throw new NotSingletonFeatureStructureException();
    }

    if (featureStructure == null)
      throw new FeatureStructureNotFoundException();

    return featureStructure;
  }

  public static List<Annotation> getAllAnnotationsOfType(int type, JCas cas) {
    List<Annotation> foundAnnotations = new ArrayList<Annotation>();
    AnnotationIndex<Annotation> annotationIndex = cas.getAnnotationIndex(type);
    for (Annotation annotation : annotationIndex) {
      foundAnnotations.add(annotation);
    }
    return foundAnnotations;
  }

  public static void enhanceNode(GraphNode existingNode, List<? extends FeatureStructure> uimaObjects) {
    Lock lock = existingNode.writeLock();
    try {
      lock.lock();
      for (FeatureStructure uimaObject : uimaObjects) {
        // create a new node for the current Annotation
        GraphNode annotationNode = new GraphNode(ENTITY.Annotation, existingNode.getGraph());
        log.info(new StringBuilder("Node created for Type ").append(uimaObject.getType().toString()).toString());

        // set Annotation specific properties for the node
        if (uimaObject instanceof Annotation) {
          Annotation annotation = (Annotation) uimaObject;
          annotationNode.addPropertyValue(ENTITY.begin, annotation.getBegin());
          annotationNode.addPropertyValue(ENTITY.end, annotation.getEnd());
        }

        //XXX : in OpenCalais the type is an URI so it maybe reasonable to put another node here
        annotationNode.addPropertyValue(ENTITY.uimaType, uimaObject.getType().getName());

        /* inspect features of the annotation */
        Type type = uimaObject.getType();
        for (Feature feature : type.getFeatures()) {

          // create a new feature node
          GraphNode featureNode = new GraphNode(ENTITY.Feature, existingNode.getGraph());
          log.info(new StringBuilder("Node created for Feature ").append(feature.getName()).toString());

          // set feature name and value if not null
          featureNode.addPropertyValue(ENTITY.featureName, feature.getName());

          String featureValue = null;
          try {
            featureValue = uimaObject.getFeatureValueAsString(feature);
          }
          catch (Exception e) {
            // do nothing at the moment
            log.warn(new StringBuilder("Unable to create feature value - ").append(e.toString()).toString());
          }

          if (featureValue != null) {
            featureNode.addPropertyValue(ENTITY.featureValue, featureValue);
            log.info(new StringBuilder("Added feature ").append(feature.getName()).append(" with value ")
                    .append(featureValue.toString()).toString());
          }

          // add feature to the annotation node
          annotationNode.addProperty(ENTITY.hasFeature, featureNode.getNode());
        }

        // finally add the triple existingNode,ENTITY.contains,calaisNode
        existingNode.addProperty(ENTITY.contains, annotationNode.getNode());

      }
    } finally {
      lock.unlock();
    }
  }

}
