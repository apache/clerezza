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
package org.apache.clerezza.platform.typerendering.seedsnipe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.clerezza.platform.typerendering.CallbackRenderer;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.templating.RenderingFunction;
import org.apache.clerezza.templating.RenderingFunctions;

/**
 * 
 * @author rbn, mkr, dsr
 */
class WebRenderingFunctions implements RenderingFunctions {

	private static final UriRef XML_DATE_LITERAL = new UriRef(
			"http://www.w3.org/2001/XMLSchema#dateTime");
	private final static UriRef RDF_XML_LITERAL = new UriRef(
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral");
	private TripleCollection graph;
	private GraphNode context;
	private CallbackRenderer callbackRenderer;
	private String mode;

	WebRenderingFunctions(TripleCollection graph,
			GraphNode context,
			CallbackRenderer callbackRenderer, String mode) {
		this.graph = graph;
		this.context = context;
		this.callbackRenderer = callbackRenderer;
		this.mode = mode;
	}



	@Override
	public RenderingFunction<Object, String> getDefaultFunction() {
		return new RenderingFunction<Object, String>() {

			@Override
			public String process(Object... values) {
				Object value = values[0];
				if (value instanceof String) {
					return (String) value;
				}
				String stringValue;
				if (value instanceof Literal) {
					if (value instanceof TypedLiteral) {
						TypedLiteral typedLiteral = (TypedLiteral) value;
						if (typedLiteral.getDataType().equals(RDF_XML_LITERAL)) {
							return typedLiteral.getLexicalForm();
						}
					}
					stringValue = ((Literal) value).getLexicalForm();
				} else {
					if (value instanceof UriRef) {
						stringValue = ((UriRef) value).getUnicodeString();
					} else {
						stringValue = value.toString();
					}
				}
				return escape(stringValue);

			}
		};
	}

	private String escape(String s) {
		StringWriter resultWriter = new StringWriter();
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch (ch) {
				case '<':
					resultWriter.write("&lt;");
					break;
				case '&':
					resultWriter.write("&amp;");
					break;
				default:
					resultWriter.write(ch);
			}
		}
		return resultWriter.toString();
	}

	@Override
	public Map<String, RenderingFunction> getNamedFunctions() {
		Map<String, RenderingFunction> result = new HashMap<String, RenderingFunction>();
		result.put("render", new RenderFunction());
		result.put("mode", new ModeFunction());
		result.put("language", languageFunction);
		result.put("datatype", datatypeFunction);
		result.put("type", typeFunction);
		result.put("toString", toStringFunction);
		result.put("date", dateFunction);
		result.put("substring", substringFunction);
		result.put("lexicalForm", lexicalFormFunction);
		result.put("contains", containsFunction);
		return result;
	}

	/**
	 * A functions that takes a node and optionally a templating mode as
	 * arguments
	 */
	private class RenderFunction implements RenderingFunction<Object, String> {

		@Override
		public String process(Object... values) throws IOException {
			NonLiteral resource = (NonLiteral) values[0];
			GraphNode graphNode = new GraphNode(resource, graph);
			String mode = null;
			if (values.length > 1) {
				mode = (String) values[1];
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			callbackRenderer.render(graphNode, context, mode, out);
			try {
				return new String(out.toByteArray(), "utf-8");
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/**
	 * A function that returns the current rendering mode
	 */
	private class ModeFunction implements RenderingFunction<Object, String> {

		@Override
		public String process(Object... values) {
			return mode;
		}
	}

	/**
	 * A function that returns the Language of a PlainLiteral or null if the
	 * Literal has no language or if the object is not a PlainLiteral
	 */
	private static RenderingFunction languageFunction = new RenderingFunction<Object, Language>() {

		@Override
		public Language process(Object... values) {
			Object value = values[0];
			if (value instanceof PlainLiteral) {
				return ((PlainLiteral) value).getLanguage();
			}
			return null;
		}
	};

	/**
	 * A function that returns the Datatype of a TypedLiteral or null if the
	 * Literal has no language or if the object is not a TypedLiteral
	 */
	private static RenderingFunction datatypeFunction = new RenderingFunction<Object, UriRef>() {

		@Override
		public UriRef process(Object... values) {
			Object value = values[0];
			if (value instanceof PlainLiteral) {
				return ((TypedLiteral) value).getDataType();
			}
			return null;
		}
	};

	/**
	 * A function that returns the value returned by the toString() method of an
	 * object
	 */
	private static RenderingFunction toStringFunction = new RenderingFunction<Object, String>() {

		@Override
		public String process(Object... values) {
			Object value = values[0];
			return value.toString();
		}
	};

	/**
	 * A function that returns a String representing the type of the Object.
	 * Either oneof - plainLiteral - typedLiteral - uriRef - bNode
	 * 
	 * or, for other types the SimpleName of the class.
	 * 
	 */
	private static RenderingFunction typeFunction = new RenderingFunction<Object, String>() {

		@Override
		public String process(Object... values) {
			Object value = values[0];
			if (value instanceof PlainLiteral) {
				return "plainLiteral";
			}
			if (value instanceof TypedLiteral) {
				return "typedLiteral";
			}
			if (value instanceof UriRef) {
				return "uriRef";
			}
			if (value instanceof BNode) {
				return "bNode";
			}
			return value.getClass().getSimpleName();
		}
	};

	/**
	 * A function that returns String representation of an XMLLiteral of type
	 * date or an empty String if value[0] is not an XML Literal of type date.
	 * Optionally it allows the date to be formated according to a format-String
	 * parameter which uses the Java SimpleFormat syntax.
	 */
	private static RenderingFunction dateFunction = new RenderingFunction<Object, String>() {

		@Override
		public String process(Object... values) {
			TypedLiteral typedLiteral;
			if (values[0] instanceof TypedLiteral && (typedLiteral = (TypedLiteral) values[0]).getDataType().equals(XML_DATE_LITERAL)) {

				String dateString = typedLiteral.getLexicalForm();

				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
						values.length >= 2 ? (String) values[1] : "yyyy-MM-ddHH:mm:ss.SSSZ");

				Date date = LiteralFactory.getInstance().createObject(Date.class, typedLiteral);
				return simpleDateFormat.format(date);
				
			} else {
				return "";
			}
		}
	};


	

	/**
	 * A function that takes an object, a beginindex and an endindex as
	 * arguments. It returns the substring from beginindex to endindex
	 * of the string representation of the first argument.
	 */
	private static RenderingFunction substringFunction = new RenderingFunction<Object, String>() {

		@Override
		public String process(Object... values) {
			String str = values[0].toString();
			int begin = 0;
			int end = str.length();
					if (values.length >= 2) {
				begin = Integer.parseInt(values[1].toString());
				if (!(begin >= 0)) {
					begin = 0;
					}
				if (!(begin <= end)) {
					begin = str.length();
				}
			}
			if (values.length >= 3) {
				end = Integer.parseInt(values[2].toString());
				if (!(end >= begin)) {
					end = begin;
				}
				if (!(end <= str.length())) {
					end = str.length();
				}
			}

			return str.substring(begin, end);
			}
	};

	/**
	 * A function that takes an object and a string. It returns a boolean
	 * value that indicates if the string representation of the object
	 * (first argument) contains the string (second argument).
	 */
	private static RenderingFunction containsFunction = new RenderingFunction<Object, Boolean>() {

		@Override
		public Boolean process(Object... values) {
			String str = values[0].toString();
			String str2 = (String)values[1];
			return str.contains(str2);
			}
	};
	
	/**
	 * A function that takes a Literal as argument and returns the lexical form.
	 */
	private static RenderingFunction lexicalFormFunction = new RenderingFunction<Object, String>() {
		@Override
		public String process(Object... values) {
			return ((Literal) values[0]).getLexicalForm();
		}
	};
	
}
