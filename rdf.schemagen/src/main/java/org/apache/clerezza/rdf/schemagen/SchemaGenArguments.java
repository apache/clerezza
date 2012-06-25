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
package org.apache.clerezza.rdf.schemagen;

import java.net.URL;

import org.wymiwyg.commons.util.arguments.CommandLine;


/**
 * 
 * @author reto
 */
public interface SchemaGenArguments {
	// comments ommitted as redundant with description

	@CommandLine(longName = "schema", shortName = { "S" }, required = true, 
			description = "The URL from which the vocabulary can be retrieved")
	public URL getSchemaUrl();

	@CommandLine(longName = "namespace", shortName = { "N" }, required = false, 
			description = "Namespace of the vocabulary, by default it uses the URI of a resource of type owl:Ontology found in the vocabulary")
	public String getNamespace();

	@CommandLine(longName = "format", shortName = { "F" }, required = false, 
			description = "The RDF content-type of the schema (Content-Type in an HTTP-Response is ignored)",
			defaultValue = "application/rdf+xml")
	public String getFormatIdentifier();
	
	@CommandLine(longName = "classname", shortName = { "C" }, required = true, 
			description = "The fully qualified class name of the class to be created")
	public String getClassName();
}
