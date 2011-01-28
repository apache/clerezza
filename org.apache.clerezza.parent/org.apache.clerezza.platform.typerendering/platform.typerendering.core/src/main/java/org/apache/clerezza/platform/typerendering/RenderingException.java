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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import org.apache.clerezza.platform.typerendering.ontologies.TYPERENDERING;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.ontologies.RDF;

/**
 * This exception is thrown when an exception occured while rendering a
 * <code>GraphNode</code> and a rendering specification with a renderlet.
 * 
 * @author mir
 */
public class RenderingException extends TypeRenderingException {

	private URI renderingSpecification;
	private Exception cause;

	public RenderingException(Exception cause, URI renderingSpecification,
			GraphNode renderNode, GraphNode context) {
		super(cause.getClass().getName() + ": " + cause.getMessage(), renderingSpecification,
				renderNode, context, cause);
		this.cause = cause;
		this.renderingSpecification = renderingSpecification;
	}

	@Override
	public GraphNode getExceptionGraphNode() {
		GraphNode result = new GraphNode(new BNode(), new SimpleMGraph());
		result.addProperty(RDF.type, TYPERENDERING.Exception);
		result.addProperty(TYPERENDERING.errorSource, new UriRef(renderingSpecification.toString()));
		result.addProperty(TYPERENDERING.message, new PlainLiteralImpl(getMessage()));
		result.addProperty(TYPERENDERING.stackTrace, getStackTraceLiteral());
		return result;
	}

	private PlainLiteral getStackTraceLiteral() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		cause.printStackTrace(pw);
		pw.flush();
		return new PlainLiteralImpl(sw.toString());
	}
}
