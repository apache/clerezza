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
package org.apache.clerezza.templating.seedsnipe.graphnodeadapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.ontologies.OWL;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.utils.RdfList;
import org.apache.clerezza.templating.RenderingFunction;
import org.apache.clerezza.templating.RenderingFunctions;
import org.apache.clerezza.templating.seedsnipe.datastructure.DataFieldResolver;
import org.apache.clerezza.templating.seedsnipe.datastructure.FieldDoesNotHaveDimensionException;
import org.apache.clerezza.templating.seedsnipe.datastructure.FieldIndexOutOfBoundsException;
import org.apache.clerezza.templating.seedsnipe.datastructure.InvalidElementException;
import org.apache.clerezza.templating.seedsnipe.simpleparser.DefaultParser;

/**
 * Data Model for {@link GraphNode} to be used with {@link DefaultParser}.
 * 
 * <p>
 * It is used by {@link DefaultParser} to resolve RDF data fields in templates.
 * </p>
 * 
 * @author reto
 */
public class GraphNodeDataFieldResolver extends DataFieldResolver {

	@Override
	public Object resolveAsObject(String fieldName, int[] arrayPos)
			throws FieldDoesNotHaveDimensionException,
			FieldIndexOutOfBoundsException, InvalidElementException, IOException {
		return resolveNonNamespace(fieldName, arrayPos);
	}

	public static class PropertyKey {
		private UriRef property;
		private boolean reverse;
		
		public PropertyKey(UriRef property, boolean reverse) {
			super();
			this.property = property;
			this.reverse = reverse;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((property == null) ? 0 : property.hashCode());
			result = prime * result + (reverse ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PropertyKey other = (PropertyKey) obj;
			if (property == null) {
				if (other.property != null)
					return false;
			} else if (!property.equals(other.property))
				return false;
			if (reverse != other.reverse)
				return false;
			return true;
		}

		
	}
	private static final UriRef RDF_NIL = new UriRef(
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");
	private RenderingFunctions renderingFunctions;

	private Object[] getArgumentValues(String innerFieldName, int[] arrayPos) 
			throws FieldDoesNotHaveDimensionException, InvalidElementException,
			FieldIndexOutOfBoundsException, IOException {
		String[] arguments = innerFieldName.split(",");
		Object[] result = new Object[arguments.length];
		for (int i = 0; i < result.length; i++) {
			String argument = arguments[i].trim();
			if (argument.charAt(0) == '\"') {
				result[i] = argument.substring(1, argument.length()-1);
			} else {
				try {
					result[i] = resolveNonNamespace(argument, arrayPos);
				} catch (FieldDoesNotHaveDimensionException e) {
					result[i] = e.getSolutionObtainedReducingDimensions();
				}
			}

		}
		return result;
	}

	/**
	 * {@link GraphNode} wrapper used by {@link GraphNodeDataFieldResolver}.
	 * 
	 * @author reto
	 */
	private class ExpandedNode {

		private GraphNode node;
		private Resource value;
		private Map<PropertyKey, List<ExpandedNode>> children;
		private List<Resource> list = null;

		private ExpandedNode(Resource value) {
			this.value = value;
			final TripleCollection tc = expandedNode.node.getGraph();
			doListInitialization(tc);
		}

		private ExpandedNode(GraphNode node, Resource value) {
			this.value = value;
			this.node = node;
			doListInitialization(node.getGraph());
		}

		/** if value id an rdf:list in tc, initialize list
		 * @param tc
		 */
		private void doListInitialization(TripleCollection tc) {
			if (value instanceof NonLiteral) {		
				if ((tc.filter((NonLiteral) value, RDF.rest, null).hasNext())
						|| (tc.filter(
						(NonLiteral) value, OWL.sameAs, RDF_NIL).hasNext())) {
					list = new RdfList((NonLiteral) value, tc);
				}
			}
		}

		private ExpandedNode getChild(String[] pathSections, int[] arrayPos, int currentDimension)
				throws FieldIndexOutOfBoundsException, FieldDoesNotHaveDimensionException {
			
			
			
			if (pathSections.length == 0) {
				if (arrayPos.length > 0) {
					throw new FieldDoesNotHaveDimensionException(".", arrayPos, this.value);
				}
				return this;
			}

			if (pathSections[0].equals(".")) {
				return getChild(
						Arrays.copyOfRange(pathSections, 1, pathSections.length),
						arrayPos, currentDimension);
			}

			if (pathSections[0].equals("contains")) {
				if (list == null) {
					throw new RuntimeException("Attempt to access virtual " +
							"property 'contains' on non-list resource");
				}
				int pos = arrayPos[0];
				if (pos >= list.size()) {
					throw new FieldIndexOutOfBoundsException(pathSections[0],arrayPos, currentDimension);
				}
				ExpandedNode listNode = new ExpandedNode(list.get(pos));
				if (pathSections.length == 1) {
					return listNode;
				}
				return listNode.getChild(Arrays.copyOfRange(pathSections, 1, pathSections.length),
						Arrays.copyOfRange(arrayPos, 1, arrayPos.length), currentDimension);
			}
			
            String fieldName = pathSections[0];
            boolean inverseResolve;
            if (fieldName.charAt(0) == '-') {
                fieldName = fieldName.substring(1);
                inverseResolve = true;
            } else {
                inverseResolve = false;
            }
			String uriString = getUriFromCuri(fieldName);
			UriRef property = new UriRef(uriString);
			List<ExpandedNode> childList = getChildList(property, inverseResolve);
			if (arrayPos.length == 0) {
				arrayPos = new int[1];
				currentDimension = 0;
				arrayPos[0] = 0;
			}
			int pos = arrayPos[0];
			if (pos >= childList.size()) {
				throw new FieldIndexOutOfBoundsException(uriString, arrayPos, currentDimension);
			}
			
			currentDimension++;
			return childList.get(pos).getChild(
					Arrays.copyOfRange(pathSections, 1, pathSections.length),
					Arrays.copyOfRange(arrayPos, 1, arrayPos.length), currentDimension);

		}

		private List<ExpandedNode> getChildList(UriRef property, boolean inverseResolve) {
			PropertyKey propertyKey = new PropertyKey(property, inverseResolve); 
			List<ExpandedNode> result;
			if (children == null) {
				children = new HashMap<PropertyKey, List<ExpandedNode>>();
				result = null;
			} else {
				result = children.get(propertyKey);
			}
			if (result == null) {
				result = new ArrayList<ExpandedNode>();
				if (node == null) {
					node = new GraphNode((NonLiteral) value, expandedNode.node.getGraph());
				}
                if (inverseResolve) {
                    Iterator<NonLiteral> subjects = node.getSubjects(property);
                    while (subjects.hasNext()) {
                        ExpandedNode childNode = new ExpandedNode(subjects.next());
                        result.add(childNode);
                    }
                } else {
                    Iterator<Resource> objects = node.getObjects(property);
                    while (objects.hasNext()) {
                        ExpandedNode childNode = new ExpandedNode(objects.next());
                        result.add(childNode);
                    }
                }
				children.put(propertyKey, result);
			}
			return result;
		}
	}
	private ExpandedNode expandedNode;
	private Map<String, String> nsMap = new HashMap<String, String>();

	/**
	 * Constructor.
	 * 
	 * @param node  An RDF resource.
	 */
	public GraphNodeDataFieldResolver(GraphNode node, RenderingFunctions renderingFunctions) {
		expandedNode = new ExpandedNode(node, node.getNode());
		this.renderingFunctions = renderingFunctions;
	}

	
	@Override
	public String resolve(String fieldName, int[] arrayPos)
			throws FieldDoesNotHaveDimensionException,
			FieldIndexOutOfBoundsException, InvalidElementException, IOException {
		if (fieldName.startsWith("ns:")) {
			defineNamespace(fieldName.substring(3));
			return "";
		}
		return renderingFunctions.getDefaultFunction().process(
				resolveAsObject(fieldName, arrayPos));
	}

	private Object resolveNonNamespace(String fieldName, int[] arrayPos)
			throws FieldDoesNotHaveDimensionException,
			FieldIndexOutOfBoundsException, InvalidElementException, IOException {
		final int openingBracket = fieldName.indexOf('(');
		if (openingBracket > 0) {
			final int closingBracket = fieldName.lastIndexOf(')');
			if (closingBracket == -1) {
				throw new InvalidElementException("Unmatched brackets");
			}
			final String functionName = fieldName.substring(0, openingBracket);
			RenderingFunction<Object, ?> function = renderingFunctions.getNamedFunctions().get(functionName);
			if (function == null) {
				throw new InvalidElementException("No such function: " + functionName);
			}
			String innerFieldName = fieldName.substring(openingBracket + 1, closingBracket);
			Object[] argumentValues = getArgumentValues(innerFieldName,arrayPos);
			return function.process(argumentValues);
		}
		final String[] pathSections = fieldName.split("/");
		return expandedNode.getChild(pathSections, arrayPos, 0).value;

	}

	/**
	 * Returns the absolute URI for a field name by
	 * expanding its namespace.
	 * 
	 * <p>
	 * 	<code>fieldName</code> is something like <code>foaf:name</code> and 
	 * 	the return value is for example <code>http://xmlns.com/foaf/0.1/name</code>.
	 * </p>
	 * 
	 * @param fieldName  The name of the RDF data field (e.g. foaf:name).
	 * @return  the absolute URI of a RDF resource.
	 */
	private String getUriFromCuri(String fieldName) {
		String[] parts = fieldName.split(":");
		return nsMap.get(parts[0]) + parts[1];
	}

	/**
	 * Saves the namespace and its URI in <code>nsMap</code>.
	 * 
	 * @param string  The namespace and its URI in the format <code>ns=URI</code>
	 * 				  (e.g. <code>foaf=http://xmlns.com/foaf/0.1/</code>).
	 */
	private void defineNamespace(String string) {
		String[] parts = string.split("=");
		nsMap.put(parts[0], parts[1]);
	}
}
