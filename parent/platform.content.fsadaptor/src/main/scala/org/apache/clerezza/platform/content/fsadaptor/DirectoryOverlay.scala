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

package org.apache.clerezza.platform.content.fsadaptor

import org.apache.clerezza.rdf.core.NonLiteral
import org.apache.clerezza.rdf.core.Resource
import org.apache.clerezza.rdf.core.Triple
import org.apache.clerezza.rdf.core.TripleCollection
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.core.impl.AbstractTripleCollection
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.utils.IteratorMerger
import org.wymiwyg.commons.util.dirbrowser.PathNode
import java.util.Iterator

class DirectoryOverlay(pathNode: PathNode, base: TripleCollection)
	extends AbstractTripleCollection {

	private val addedTriples = new SimpleMGraph()

	PathNode2MGraph.describeInGraph(pathNode, addedTriples)

	import collection.JavaConversions._

	val subjects = (for (triple <- addedTriples; subject = triple.getSubject) yield {
		subject
	}).toSet

	class FilteringIterator(baseIter: Iterator[Triple]) extends Iterator[Triple] {
		var nextElem: Triple = null
		def prepareNext {
			nextElem = if (baseIter.hasNext) baseIter.next else null
			if ((nextElem != null) && 
				(subjects.contains(nextElem.getSubject))) {
					//println("skipping "+nextElem)
					prepareNext
			}
		}
		prepareNext

		override def next = {
			val result = nextElem
			prepareNext
			result
		}
		override def hasNext = nextElem != null
		override def remove = throw new UnsupportedOperationException
	}

	override def performFilter(s: NonLiteral, p: UriRef,
			o: Resource): Iterator[Triple] = {
		new IteratorMerger(new FilteringIterator(base.filter(s, p, o)), addedTriples.filter(s,p, o))
	}

	/**
	 * returns an upper bound of the size (removals in abse are not deducted)
	 */
	override def size = base.size+addedTriples.size
}
