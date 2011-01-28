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
package org.apache.clerezza.platform.content.genericmetadata;

import java.util.Date;
import javax.ws.rs.core.MediaType;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.metadata.MetaDataGenerator;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.ontologies.DCTERMS;

/**
 * This class generates metadata about assets.
 *
 * @author tio
 */
@Component()
@Service(MetaDataGenerator.class)
public class GenericMetaDataGenerator implements MetaDataGenerator {

	@Override
	public void generate(GraphNode node, byte[] data, MediaType mediaType) {
		TypedLiteral dateLiteral = LiteralFactory.getInstance()
					.createTypedLiteral(new Date());
		if(node.getObjects(DCTERMS.dateSubmitted).hasNext()) {
			if(node.getObjects(DCTERMS.modified).hasNext()) {
				node.deleteProperties(DCTERMS.modified);
			}
			node.addProperty(DCTERMS.modified, dateLiteral);
		} else {
			node.addProperty(DCTERMS.dateSubmitted, dateLiteral);
		}
	}
}
