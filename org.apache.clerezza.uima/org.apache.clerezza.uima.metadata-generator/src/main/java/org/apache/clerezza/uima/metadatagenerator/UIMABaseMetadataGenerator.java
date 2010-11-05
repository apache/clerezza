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
package org.apache.clerezza.uima.metadatagenerator;

import org.apache.clerezza.rdf.metadata.MetaDataGenerator;
import org.apache.clerezza.rdf.ontologies.DC;
import org.apache.clerezza.rdf.ontologies.DCTERMS;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.uima.metadatagenerator.mediatype.MediaTypeTextExtractor;
import org.apache.clerezza.uima.metadatagenerator.mediatype.PlainTextExtractor;
import org.apache.clerezza.uima.metadatagenerator.mediatype.UnsupportedMediaTypeException;
import org.apache.clerezza.uima.utils.ExternalServicesFacade;
import org.apache.clerezza.uima.utils.UIMAServicesFacade;
import org.apache.clerezza.uima.utils.UIMAUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.FeatureStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * An implementation of {@link MetaDataGenerator} generates meta data about specified data
 * depending on its media type using Apache UIMA.
 */
@Component(metatype = true)
@Services({
        @Service(MetaDataGenerator.class),
        @Service(UIMABaseMetadataGenerator.class)
})
public class UIMABaseMetadataGenerator implements MetaDataGenerator {

  private final static Logger log = LoggerFactory.getLogger(UIMABaseMetadataGenerator.class);

  private UIMAServicesFacade facade;

  private Set<MediaTypeTextExtractor> textExtractors;

  public UIMABaseMetadataGenerator() {
    this.facade = new ExternalServicesFacade();
    this.textExtractors = new TreeSet<MediaTypeTextExtractor>();
  }

  public UIMABaseMetadataGenerator(ExternalServicesFacade facade) {
    this.facade = facade;
    this.textExtractors = new TreeSet<MediaTypeTextExtractor>();
  }

  public void generate(GraphNode node, byte[] data, MediaType mediaType) {
    if (textExtractors.isEmpty()) {
      initializeExtractors();
    }
    try {
      String text = getTextToAnalyze(data, mediaType);

      // add language to the document
      addLanguage(node, text);

      // add wide purpose subject to the document
      addCategory(node, text);

      // add named entities' nodes
      addNamedEntities(node, text);

      // add tags' nodes
      addTags(node, text);

      log.info(new StringBuilder(node.toString()).append(" graph node enriched").toString());
    } catch (Throwable e) {
      log.error(new StringBuilder("Unable to extract metadata due to ").append(e.toString()).toString());
    }
  }

  /* initialize text extractors sorted set */

  private void initializeExtractors() {
    this.textExtractors.add(new PlainTextExtractor());
  }

  private String getTextToAnalyze(byte[] data, MediaType mediaType) throws UnsupportedMediaTypeException {
    // since extractors are sorted, the first I found supporting this mediaType is good
    String text = null;
    for (MediaTypeTextExtractor textExtractor : this.textExtractors) {
      if (textExtractor.supports(mediaType)) {
        text = textExtractor.extract(data);
        break;
      }
    }
    if (text == null) {
      throw new UnsupportedMediaTypeException(mediaType);
    }
    return text;
  }


  private void addCategory(GraphNode node, String data) throws UIMAException {
    // get category to bind it to the node
    FeatureStructure categoryFS = this.facade.getCategory(data);
    String category = categoryFS.getStringValue(categoryFS.getType().getFeatureByBaseName("text"));
    node.addPropertyValue(DC.subject, category);
  }

  private void addLanguage(GraphNode node, String data) throws UIMAException {
    // get language to bind it to the node
    FeatureStructure languageFS = this.facade.getLanguage(data);
    String language = languageFS.getStringValue(languageFS.getType().getFeatureByBaseName("language"));
    node.addPropertyValue(DCTERMS.language, language);
  }

  private void addNamedEntities(GraphNode existingNode, String data) throws UIMAException {
    // analyze document text and get the corresponding OpenCalais annotations
    List<FeatureStructure> namedEntities = facade.getNamedEntities(data);
    // convert annotations to nodes inside the current graph
    UIMAUtils.enhanceNode(existingNode, namedEntities);

  }

  private void addTags(GraphNode existingNode, String data) throws UIMAException {
    // analyze document text and get the corresponding AlchemyAPI Tags
    List<FeatureStructure> alchemyAPITags = this.facade.getTags(data);
    // convert tags to nodes inside the current graph
    UIMAUtils.enhanceNode(existingNode, alchemyAPITags);
  }

}
