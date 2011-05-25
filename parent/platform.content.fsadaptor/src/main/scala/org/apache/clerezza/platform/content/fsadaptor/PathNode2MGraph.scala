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

package org.apache.clerezza.platform.content.fsadaptor;

import java.io.ByteArrayOutputStream
import javax.ws.rs.core.MediaType
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.rdf.core.LiteralFactory
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl
import org.apache.clerezza.rdf.core.impl.TripleImpl
import org.apache.clerezza.rdf.ontologies.DISCOBITS
import org.apache.clerezza.rdf.ontologies.HIERARCHY
import org.apache.clerezza.rdf.ontologies.RDF
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.web.fileserver.util.MediaTypeGuesser
import org.wymiwyg.commons.util.dirbrowser.PathNode


/**
 *
 * @author reto
 */
object PathNode2MGraph {

	private val URI_PREFIX = Constants.URN_LOCAL_INSTANCE

	private val literalFactory = LiteralFactory.getInstance

	private def getMediaType(file: PathNode) = {
		val guessedMediaType: MediaType = MediaTypeGuesser.getInstance().guessTypeForName(file.getPath());
		if (guessedMediaType != null) {
			guessedMediaType.toString
		} else {
			"application/octet-stream"
		}

	}

	private def getData(file: PathNode) = {
		val baos = new ByteArrayOutputStream
		val in = file.getInputStream()
		val buffer = new Array[Byte](4096)
		var read = in.read(buffer)
		while (read != -1) {
			baos.write(buffer, 0, read)
			read = in.read(buffer)
		}
		in.close()
		baos.toByteArray
	}

	def describeInGraph(directory: PathNode, mGraph: MGraph) {
		val basePathLength = directory.getPath.length
		def createUriRef(file: PathNode, isDirectory: Boolean) = {
			def addSlashIfNeeded(s: String) = {
				 if (s.endsWith("/")) {
					 s
				 } else {
					 s+'/'
				 }
			}
			val path =	if (isDirectory) {
				addSlashIfNeeded(file.getPath.substring(basePathLength))
			} else {
				file.getPath.substring(basePathLength)
			}
			new UriRef(URI_PREFIX+path)
		}
		def processDirectory(directory: PathNode) {
			val directoryResource = createUriRef(directory, true)
			mGraph.add(new TripleImpl(directoryResource, RDF.`type`, HIERARCHY.Collection))
			for (subPath <- directory.list) {
				val file = directory.getSubPath(subPath)
				val isDirectory = file.isDirectory
				val resource = createUriRef(file, isDirectory)
				mGraph.add(new TripleImpl(resource, HIERARCHY.parent, directoryResource))
				if (isDirectory) {
					processDirectory(file)
				} else {
					mGraph.add(new TripleImpl(resource, RDF.`type`, DISCOBITS.InfoDiscoBit))
					val data = getData(file)
					mGraph.add(new TripleImpl(resource, DISCOBITS.infoBit, literalFactory.createTypedLiteral(data)))
					mGraph.add(new TripleImpl(resource, DISCOBITS.mediaType,
											  literalFactory.createTypedLiteral(getMediaType(file))));
				}
			}
		}
		processDirectory(directory)
	}

	def removeNodesFromGraph(directory: PathNode, mGraph: MGraph) {
		val basePathLength = directory.getPath.length
		def createUriRef(file: PathNode, isDirectory: Boolean) = {
			def addSlashIfNeeded(s: String) = {
				 if (s.endsWith("/")) {
					 s
				 } else {
					 s+'/'
				 }
			}
			val path =	if (isDirectory) {
				addSlashIfNeeded(file.getPath.substring(basePathLength))
			} else {
				file.getPath.substring(basePathLength)
			}
			new UriRef(URI_PREFIX+path)
		}
		def processDirectory(directory: PathNode) {
			val directoryResource = createUriRef(directory, true)
			mGraph.remove(new TripleImpl(directoryResource, RDF.`type`, HIERARCHY.Collection))
			for (subPath <- directory.list) {
				val file = directory.getSubPath(subPath)
				val isDirectory = file.isDirectory
				val resource = createUriRef(file, isDirectory)
				val node = new GraphNode(resource, mGraph)
				if (isDirectory) {
					processDirectory(file)
				}
				node.deleteProperties(HIERARCHY.parent)
				node.deleteProperties(RDF.`type`)
				node.deleteProperties(DISCOBITS.infoBit)
				node.deleteProperties(DISCOBITS.mediaType)
			}
		}
		processDirectory(directory)
	}
}
