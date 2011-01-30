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
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * A abstract super-class for all exceptions concerning type rendering.
 *
 * @author mir
 */
public abstract class TypeRenderingException extends RuntimeException{

	private GraphNode node;
	private URI renderingSpecification;
	private GraphNode context;

	public TypeRenderingException(String message, URI renderingSpecification,
			GraphNode renderNode, GraphNode context) {
		super(message+" in "+renderingSpecification);
		this.node = renderNode;
		this.renderingSpecification = renderingSpecification;
		this.context = context;
	}

	public TypeRenderingException(String message, URI renderingSpecification,
			GraphNode renderNode, GraphNode context, Throwable cause) {
		super(message, cause);
		this.node = renderNode;
		this.renderingSpecification = renderingSpecification;
		this.context = context;
	}


	/**
	 * Returns the <code>GraphNode</code> that should have been rendered with
	 * the rendering specification containing the error.
	 *
	 * @return the graph node to be rendered.
	 */
	public GraphNode getRenderNode() {
		return node;
	}

	/**
	 * Returns the context <code>GraphNode</code> that should have been rendered with
	 * the rendering specification containing the error.
	 *
	 * @return the graph node to be rendered.
	 */
	public GraphNode getContextNode() {
		return node;
	}

	/**
	 * Return the uri of the rendering specifiacion that contains the error.
	 * @return the renderering specification containing the error
	 */
	public URI getRenderingSpecification() {
		return renderingSpecification;
	}

	/**
	 * Returns a <code>GraphNode</code> of the type typerendering:Exception
	 * which contains all information used to render an exception page.
	 * The node contains the following information: the line and column number
	 * on which the error occurred in the rendering specification. The uri of
	 * the rendering specification in which the error is. A message with details
	 * about error.
	 *
	 * @return the graph node containing all information about the exception
	 */
	public abstract GraphNode getExceptionGraphNode();
	
}
