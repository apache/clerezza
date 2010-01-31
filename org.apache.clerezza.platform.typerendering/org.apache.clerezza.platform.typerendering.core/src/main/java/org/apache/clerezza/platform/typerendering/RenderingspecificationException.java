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
package org.apache.clerezza.platform.typerendering;

import java.net.URI;
import org.apache.clerezza.platform.typerendering.ontologies.TYPERENDERING;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * This exception is thrown when an error in the rendering specification caused
 * an exception in the renderlet.
 *
 * @author mir
 */
public class RenderingspecificationException extends TypeRenderingException{
	
	private int lineNumber, columnNumber;
	private GraphNode node;
	private URI renderingSpecification;

	/**
	 * Creates a <code>RenderingspecificationException</code> with message,
	 * scriptUri, lineNumber, columnNumber and renderNode to be used in an
	 * <code>GraphNode</code> that can be used to render an exception page.
	 *
	 * @param message A message about the error that caused the exception
	 * @param renderingSpecification The uri of the rendering specification
	 *		which contains error.
	 * @param lineNumber The line number on which the error is located in the
	 *		rendering specificiation.
	 * @param columnNumber The column number on which the error is located in the
	 *		rendering specificiation.
	 * @param renderNode The graph node which were given to the renderlet to
	 *		be rendered.
	 */
	public RenderingspecificationException(String message, URI renderingSpecification,
			int lineNumber,	int columnNumber, GraphNode renderNode, GraphNode context) {
		super(message, renderingSpecification, renderNode, context);
		this.lineNumber = lineNumber;
		this.columnNumber = columnNumber;
		this.node = renderNode;
		this.renderingSpecification = renderingSpecification;
	}

	/**
	 * Creates a <code>RenderingspecificationException</code> with message,
	 * scriptUri, and renderNode to be used in an <code>GraphNode</code> that
	 * can be used to render an exception page.
	 *
	 * @param message A message about the error that caused the exception
	 * @param renderingSpecification The uri of the rendering specification
	 *		which contains error.
	 * @param renderNode The graph node which were given to the renderlet to
	 *		be rendered.
	 */
	public RenderingspecificationException(String message, URI renderingSpecification,
			GraphNode renderNode, GraphNode context) {
		super(message, renderingSpecification, renderNode, context);
		this.lineNumber = -1;
		this.columnNumber = -1;
		this.node = renderNode;
		this.renderingSpecification = renderingSpecification;
	}

	/**
	 * Returns the column number on which the error is located in the rendering
	 * specificiation.
	 *
	 * @return the column number on which the error occurred.
	 */
	public int getColumnNumber() {
		return columnNumber;
	}

	/**
	 * Returns the line number on which the error is located in the rendering
	 * specificiation.
	 *
	 * @return the line number on which the error occurred.
	 */
	public int getLineNumber() {
		return lineNumber;
	}

	/**
	 * Returns a message composed of the information about the occurred error.
	 * @return
	 */
	public String getComposedMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Error occurred in " + renderingSpecification.toString() + " ");
		if (getLineNumber() != -1 || getColumnNumber() != -1) {
			sb.append("at line number " + getLineNumber() + " ");
			sb.append("at column number " + getColumnNumber() + ": ");
		}
		sb.append(getMessage() + "\n");
		return sb.toString();
	}

	@Override
	public GraphNode getExceptionGraphNode() {
		GraphNode result = new GraphNode(new BNode(), new SimpleMGraph());
		result.addProperty(RDF.type, TYPERENDERING.Exception);
		LiteralFactory factory = LiteralFactory.getInstance();
		result.addProperty(TYPERENDERING.errorSource, new UriRef(renderingSpecification.toString()));
		if (lineNumber != -1) {
			result.addProperty(TYPERENDERING.line, factory.createTypedLiteral(new Integer(lineNumber)));
		}
		if (columnNumber != -1) {
			result.addProperty(TYPERENDERING.column, factory.createTypedLiteral(Integer.valueOf(columnNumber)));
		}
		result.addProperty(TYPERENDERING.message, new PlainLiteralImpl(getMessage()));
		return result;
	}

}
