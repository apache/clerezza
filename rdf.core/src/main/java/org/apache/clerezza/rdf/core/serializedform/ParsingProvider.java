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
package org.apache.clerezza.rdf.core.serializedform;

import java.io.InputStream;

import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;

/**
 * An instance of this class parses RDF-Graph from one or more serialization
 * formats. The supported formats are indicated using the {@link SupportedFormat}
 * annotation.
 *
 * @author reto
 */
public interface ParsingProvider {

    /**
     * Parses a stream as the specified RDF-format. This method will be invoked
     * for a supported format, a format is considered as supported if the part
     * before a ';'-character in the <code>formatIdentifier</code> matches
     * a <code>SupportedFormat</code> annotation of the implementing class.
     *
     * @param target the mutable graph to which the read triples shall be added
     * @param serializedGraph the stream from which the serialized graph is read
     * @param formatIdentifier a String identifying the format
     * @param baseUri the baseUri for interpreting relative uris, may be null
     */
    void parse(MGraph target, InputStream serializedGraph,
            String formatIdentifier, UriRef baseUri);

}
