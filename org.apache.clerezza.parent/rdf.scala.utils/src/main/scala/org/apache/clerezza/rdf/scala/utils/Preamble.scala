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
package org.apache.clerezza.rdf.scala.utils

import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.core._

/**
* This object provides the implicit conversions. Typically this is used by
* adding
* <code>import org.apache.clerezza.rdf.scala.utils.Preamble._</code> near the top of the
* file using SCB Utilities for Scala
*/
class Preamble(baseTc: TripleCollection) extends TcIndependentConversions {
	implicit def toRichGraphNode(resource: Resource) = {
		new RichGraphNode(new GraphNode(resource, baseTc))
	}
}
object Preamble extends TcIndependentConversions {

}
trait TcIndependentConversions {
	implicit def toRichGraphNode(node: GraphNode) = {
		new RichGraphNode(node)
	}

	implicit def toFirstElement(c: CollectedIter[RichGraphNode])  = {
		if (c.length(1) > 0) {
			c(0)
		} else {
			TcIndependentConversions.emptyLiteral
		}
	}
}
object TcIndependentConversions {
	val emptyGraph = new impl.SimpleGraph(new impl.SimpleMGraph)
	val emptyLiteral = new RichGraphNode(new GraphNode(new impl.PlainLiteralImpl(""), emptyGraph))
}
