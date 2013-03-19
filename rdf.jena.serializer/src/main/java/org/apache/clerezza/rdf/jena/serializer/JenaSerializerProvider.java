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
package org.apache.clerezza.rdf.jena.serializer;

import java.io.OutputStream;

import org.apache.clerezza.rdf.core.serializedform.SerializingProvider;
import org.apache.clerezza.rdf.core.serializedform.SupportedFormat;
import org.apache.clerezza.rdf.jena.facade.JenaGraph;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.serializedform.UnsupportedSerializationFormatException;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
/**
 * A {@link org.apache.clerezza.rdf.core.serializedform.SerializingProvider} based on Jena
 *
 * @author mir
 */
/*
 * see http://jena.sourceforge.net/IO/iohowto.html
 */
@Component
@Service(SerializingProvider.class)
@Property(name="supportedFormat", value={SupportedFormat.RDF_XML,
    SupportedFormat.TURTLE,    SupportedFormat.X_TURTLE,
    SupportedFormat.N_TRIPLE, SupportedFormat.N3})
@SupportedFormat({SupportedFormat.RDF_XML,
    SupportedFormat.TURTLE,    SupportedFormat.X_TURTLE,
    SupportedFormat.N_TRIPLE, SupportedFormat.N3})
public class JenaSerializerProvider implements SerializingProvider {

    @Override
    public void serialize(OutputStream serializedGraph, TripleCollection tc,
            String formatIdentifier) {
        String jenaFormat = getJenaFormat(formatIdentifier);
        com.hp.hpl.jena.graph.Graph graph = new JenaGraph(tc);
        Model model = ModelFactory.createModelForGraph(graph);
        RDFWriter writer = model.getWriter(jenaFormat);
        if ("RDF/XML".equals(jenaFormat)) {
            //jena complains about some URIs that aren't truely bad
            //see: http://tech.groups.yahoo.com/group/jena-dev/message/38313
            writer.setProperty("allowBadURIs", Boolean.TRUE);
        }
        writer.write(model, serializedGraph, "");
    }

    private String getJenaFormat(String formatIdentifier) {
        int semicolonPos = formatIdentifier.indexOf(';');
        if (semicolonPos > -1) {
            formatIdentifier = formatIdentifier.substring(0, semicolonPos);
        }
        if (formatIdentifier.equals(SupportedFormat.RDF_XML)) {
            return "RDF/XML";
        }
        if (formatIdentifier.equals(SupportedFormat.TURTLE) ||
                formatIdentifier.equals(SupportedFormat.X_TURTLE)) {
            return "TURTLE";
        }
        if (formatIdentifier.equals(SupportedFormat.N3)) {
            return "N3";
        }
        if (formatIdentifier.equals(SupportedFormat.N_TRIPLE)) {
            return "N-TRIPLE";
        }
        throw new UnsupportedSerializationFormatException(formatIdentifier);
    }
}
