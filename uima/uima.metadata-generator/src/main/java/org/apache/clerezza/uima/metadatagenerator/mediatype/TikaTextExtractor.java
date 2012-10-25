package org.apache.clerezza.uima.metadatagenerator.mediatype;
/*
 *
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
 *
*/


import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;

import javax.ws.rs.core.MediaType;
import java.io.*;

/**
 * An implementation based on <a href="http://tika.apache.org">Apache Tika</a>.
 *
 * @author Davide Palmisano
 */
public class TikaTextExtractor implements MediaTypeTextExtractor {

  private Tika tika;

  private TikaConfig config;

  /**
   * Construct an instance using the default {@link org.apache.tika.Tika} configuration.
   */
  public TikaTextExtractor() {
    try {
      config = TikaConfig.getDefaultConfig();
    } catch (Exception e) {
      throw new RuntimeException("Error while loading Tika configuration.", e);
    }
    tika = new Tika(config);
  }

  /**
   * Construct an instance using a custom <i>tika-config.xml</i> configuration file.
   *
   * @param tikaConfigPath the path to the <i>tika-config.xml</i> configuration file.
   */
  public TikaTextExtractor(String tikaConfigPath) {
    InputStream inputStream = getResourceAsStream(tikaConfigPath);
    try {
      config = new TikaConfig(inputStream);
      inputStream.close();
    } catch (Exception e) {
      throw new RuntimeException("Error while loading Tika configuration.", e);
    }
    tika = new Tika(config);
  }

  /**
   * {@inheritDoc}
   */
  public boolean supports(MediaType mediaType) {
    return tika.detect(mediaType.getType()) != null;
  }

  /**
   * {@inheritDoc}
   */
  public String extract(byte[] bytes) throws UnsupportedMediaTypeException {
    InputStream inputStream = new ByteArrayInputStream(bytes);
    String mimeType = null;
    try {
      mimeType = this.tika.detect(inputStream);
    } catch (IOException e) {
      throw new RuntimeException("Error while detecting mime type", e);
    }
    if (tika.detect(mimeType) == null) {
      throw new UnsupportedMediaTypeException(
              String.format("[%s] mime type is not supported", mimeType)
      );
    }
    Metadata metadata = new Metadata();
    metadata.set(Metadata.CONTENT_TYPE, mimeType);
    Reader reader = null;
    try {
      reader = this.tika.parse(inputStream, metadata);
    } catch (IOException e) {
      throw new RuntimeException("Error while parsing the provided input");
    }
    BufferedReader in
            = new BufferedReader(reader);
    String line;
    String result = null;
    try {
      line = in.readLine();
      while (line != null) {
        result = line;
        line = in.readLine();
      }
    } catch (IOException e) {
      throw new RuntimeException("Error while parsing the provided input");
    }
    return result;
  }

  /**
   * Loads the <code>Tika</code> configuration file.
   *
   * @return the input stream containing the configuration.
   */
  private InputStream getResourceAsStream(String tikaConfigFile) {
    InputStream result;
    result = TikaTextExtractor.class.getResourceAsStream(tikaConfigFile);
    if (result == null) {
      result = TikaTextExtractor.class.getClassLoader().getResourceAsStream(tikaConfigFile);
      if (result == null) {
        result = ClassLoader.getSystemResourceAsStream(tikaConfigFile);
      }
    }
    return result;
  }
}
