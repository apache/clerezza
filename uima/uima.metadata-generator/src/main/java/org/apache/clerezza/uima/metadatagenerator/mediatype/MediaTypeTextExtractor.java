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


import javax.ws.rs.core.MediaType;

/**
 * A MediaTypeTextExtractor extracts text from a (list of) specified {@link javax.ws.rs.core.MediaType}.
 */
public interface MediaTypeTextExtractor {

  /**
   * Check if the provided {@link javax.ws.rs.core.MediaType} is supported by this extractor.
   * 
   * @param mediaType to be checked.
   * @return <code>true</code> if the provided {@link javax.ws.rs.core.MediaType} as input is supported.
   */
  public boolean supports(MediaType mediaType);

  /**
   * Extract the text from the provided input if its <i>Media Type</i> is supported.
   *
   * @param bytes an array of <code>byte</code> representing the input.
   * @return a {@link String} with the extracted text.
   * @throws UnsupportedMediaTypeException if the input implicit <i>Media type</i> is not supported.
   */
  public String extract(byte[] bytes)  throws UnsupportedMediaTypeException;

}
