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
package org.apache.clerezza.utils.customproperty.ontology;

import org.apache.clerezza.rdf.core.UriRef;

public class CUSTOMPROPERTY {

	/**
	 * The Ontology for properties, defined by customer.
	 */

	// Classes
	public static final UriRef CustomProperty = new UriRef(
			"http://clerezza.org/2009/06/custompropery#CustomProperty");
	public static final UriRef CustomField = new UriRef(
			"http://clerezza.org/2009/06/custompropery#CustomField");
	public static final UriRef CustomFieldCollection = new UriRef(
			"http://clerezza.org/2009/06/custompropery#CustomFieldCollection");
	public static final UriRef SingleValuedField = new UriRef(
			"http://clerezza.org/2009/06/custompropery#SingleValuedField");
	public static final UriRef MultiValuedField = new UriRef(
			"http://clerezza.org/2009/06/custompropery#MultiValuedField");

	// Properties

	public static final UriRef presentationlabel = new UriRef(
			"http://clerezza.org/2009/06/custompropery#presentationlabel");
	public static final UriRef configuration = new UriRef(
			"http://clerezza.org/2009/06/custompropery#configuration");
	public static final UriRef property = new UriRef(
			"http://clerezza.org/2009/06/custompropery#property");
	public static final UriRef cardinality = new UriRef(
			"http://clerezza.org/2009/06/custompropery#cardinality");
	public static final UriRef fieldlength = new UriRef(
			"http://clerezza.org/2009/06/custompropery#fieldlength");
	public static final UriRef value = new UriRef(
			"http://clerezza.org/2009/06/custompropery#value");
	public static final UriRef multiselectable = new UriRef(
			"http://clerezza.org/2009/06/custompropery#multiselectable");
	public static final UriRef dependency = new UriRef(
			"http://clerezza.org/2009/06/custompropery#category");
	public static final UriRef dependencyvalue = new UriRef(
			"http://clerezza.org/2009/06/custompropery#categoryproperty");
	public static final UriRef customfield = new UriRef(
			"http://clerezza.org/2009/06/custompropery#customfield");
	public static final UriRef length = new UriRef(
			"http://clerezza.org/2009/06/custompropery#length");
	public static final UriRef actualvalues = new UriRef(
			"http://clerezza.org/2009/06/custompropery#actualvalues");
}
