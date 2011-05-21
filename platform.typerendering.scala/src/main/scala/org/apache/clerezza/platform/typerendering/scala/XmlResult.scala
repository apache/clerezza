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
package org.apache.clerezza.platform.typerendering.scala

import java.io.OutputStream
import java.io.PrintWriter
import java.net.URI
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
import scala.xml._
import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.platform.typerendering.TypeRenderlet.RequestProperties
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.ontologies._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import java.security.{PrivilegedAction, AccessController}
import org.osgi.framework.{BundleContext, ServiceReference}
import org.apache.clerezza.rdf.scala.utils.RichGraphNode

/**
 * PageRenderlet.renderedPage returns an instance of this class, implementing
 * the content method to produce an XML Element suitable as response to the
 * request yielding to the arguments passed to the constructor.
 */
abstract class XmlResult(arguments: XmlResult.Arguments) {

	val XmlResult.Arguments(
					res: GraphNode,
					context: GraphNode,
					sharedRenderingValues: java.util.Map[String, Object],
					renderer: CallbackRenderer,
					modeOption: Option[String],
					mediaType: MediaType,
					requestProperties: RequestProperties,
					os: OutputStream) = arguments;
	val mode = modeOption match {
		case Some(x) => x
		case None => null
	}

	val uriInfo = requestProperties.getUriInfo
	val requestHeaders = requestProperties.getRequestHeaders
	val responseHeaders = requestProperties.getResponseHeaders

	def render(resource: GraphNode): Seq[Node] = {
		modeOption match {
			case Some(m) => render(resource, m)
			case None => render(resource, "naked")
		}
	}

	def render(resource: GraphNode, mode: String) = {
		def parseNodeSeq(string: String) = {
			_root_.scala.xml.XML.loadString("<elem>" + string + "</elem>").child
		}
		val baos = new java.io.ByteArrayOutputStream
		renderer.render(resource, context, mode, baos)
		parseNodeSeq(new String(baos.toByteArray))
	}

	/**
	 * renders the specified resource without using the base-graph from resource
	 * rendered by the caller but getting a new context using the GraphNodeProvider
	 */
	def render(resource: UriRef): Seq[Node] = {
		modeOption match {
			case Some(m) => render(resource, m)
			case None => render(resource, "naked")
		}
	}

	/**
	 * renders the specified resource without using the base-graph from resource
	 * rendered by the caller but getting a new context using the GraphNodeProvider
	 */
	def render(resource: UriRef, mode: String) = {
		def parseNodeSeq(string: String) = {
			_root_.scala.xml.XML.loadString("<elem>" + string + "</elem>").child
		}
		val baos = new java.io.ByteArrayOutputStream
		renderer.render(resource, context, mode, baos)
		parseNodeSeq(new String(baos.toByteArray))
	}

	/**
	 * This is an object that allows one to use some nice shortcuts in scala based subclasses
	 * - $variable will get the value of the sharedRenderingValues hash
	 * - $variable = value allows one to update the sharedRenderingValues hash
	 * - $[ClassName] allows to access an osgi service annotated to be a WebRenderingService
	 */
	object $ {
		def apply(key: String) = sharedRenderingValues.get(key)

		def update(key: String, value: Object) = sharedRenderingValues.put(key, value)

		def apply[T](implicit m: Manifest[T]): T = {
			val clazz = m.erasure.asInstanceOf[Class[T]]
			requestProperties.getRenderingService(clazz)
		}
	}

	def ifx[T](con: => Boolean)(f: => T): T = {
		if (con) f else null.asInstanceOf[T]
	}

	val resultDocModifier = org.apache.clerezza.platform.typerendering.ResultDocModifier.getInstance();

	val out = new PrintWriter(os)

	out.print(
		content match {
			case s: Seq[_] => s.mkString
			case o => o.toString
		}
	)
	out.flush()

	/**
	 * This is the main method/variable that needs to be implemented by subclasses
	 */
	def content: AnyRef;


}

object XmlResult {

	/**
	 * Class to encapsulate information sent to the rendering engine.
	 *
	 * @param res  RDF resource to be rendered with the template.
	 * @param context  RDF resource providing a rendering context.
	 * @param sharedRenderingValues	a map that can be used for sharing values
	 * across the different Renderlets involved in a rendering process
	 * @param callbackRenderer  renderer for call backs.
	 * @param renderingSpecification  the rendering specification
	 * @param modeOption the mode this Renderlet was invoked with, this is mainly used
	 * so that the callbackRenderer can be claeed inheriting the mode.
	 * @param mediaType  the media type this media produces (a part of)
	 * @param requestProperties properties of the http request, may be null
	 * @param os  where the output will be written to.
	 */
	case class Arguments(res: GraphNode,
					context: GraphNode,
					sharedRenderingValues: java.util.Map[String, Object],
					renderer: CallbackRenderer,
					modeOption: Option[String],
					mediaType: MediaType,
					requestProperties: RequestProperties,
					os: OutputStream);
}