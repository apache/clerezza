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
package org.apache.clerezza.jaxrs.sparql.providers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.sparql.ResultSet;
import org.apache.clerezza.rdf.core.sparql.SolutionMapping;
import org.apache.clerezza.rdf.core.sparql.query.Variable;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MessageBodyWirter for <code>ResultSet</code>.
 * Resulting output conforms to:
 * http://www.w3.org/TR/2007/NOTE-rdf-sparql-json-res-20070618/
 * 
 * @author misl
 */
@Component
@Service(Object.class)
@Property(name="javax.ws.rs", boolValue=true)
@Produces({"application/json", "application/sparql-results+json"})
@Provider
@SuppressWarnings("unchecked")
public class ResultSetJsonMessageBodyWriter implements MessageBodyWriter<ResultSet> {

	final Logger logger = LoggerFactory.getLogger(ResultSetJsonMessageBodyWriter.class);

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return ResultSet.class.isAssignableFrom(type);
	}

	@Override
	public long getSize(ResultSet t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(ResultSet resultSet, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType, MultivaluedMap<String,
			Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {

		JSONObject json = toJsonSource(resultSet);
		entityStream.write(json.toJSONString().getBytes("UTF-8"));
	}

	/**
	 * Helper: transforms a {@link ResultSet} or a {@link Boolean} to a
	 * json object.
	 *
	 * @param queryResult
	 */
	private JSONObject toJsonSource(ResultSet queryResult) {
		JSONObject root = new JSONObject();
		JSONObject head = new JSONObject();
		root.put("head", head);
		createVariables(queryResult.getResultVars(), head);
		
		JSONObject results = new JSONObject();
		root.put("results", results);
		
		JSONArray bindings = null;
		while (queryResult.hasNext()) {
			if (bindings == null) {
				bindings = new JSONArray();
				results.put("bindings", bindings);
			}
			bindings.add(createResult(queryResult.next()));				
		}

		return root;
	}

	/**
	 * Helper: creates value element from {@link Resource} depending on its
	 * class
	 *
	 */
	private JSONObject createResultElement(Resource resource) {
		JSONObject element = new JSONObject();
		if (resource instanceof UriRef) {
			element.put("type", "uri");
			element.put("value", UriRef.class.cast(resource).getUnicodeString());
		} else if (resource instanceof PlainLiteral) {
			element.put("type", "literal");
			element.put("value", PlainLiteral.class.cast(resource).getLexicalForm());
			Language lang = PlainLiteral.class.cast(resource).getLanguage();
			if (lang != null) {
				element.put("xml:lang", lang.toString());
			}
		} else if (resource instanceof TypedLiteral) {
			element.put("type", "typed-literal");
			element.put("datatype", TypedLiteral.class.cast(resource).getDataType().getUnicodeString());
			element.put("value", TypedLiteral.class.cast(resource).getLexicalForm());
		} else if (resource instanceof BNode) {
			element.put("type", "bnode");
			element.put("value", "/");
		} else {
			element = null;
		}
		return element;
	}

	/**
	 * Helper: creates results element from ResultSet
	 *
	 */
	private JSONObject createResult(SolutionMapping solutionMap) {
		JSONObject result = new JSONObject();
		Set<Variable> keys = solutionMap.keySet();
		for (Variable key : keys) {
			result.put(key.getName(), createResultElement((Resource) solutionMap.get(key)));
		}
		return result;
	}

	private void createVariables(List<String> variables, JSONObject head) {
		JSONArray vars = null;
		for (String variable : variables) {
			if (vars == null) {
				vars = new JSONArray();
				head.put("vars", vars);
			}
			vars.add(variable);
		}
	}
}
