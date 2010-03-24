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

import rdf.utils.GraphNode
import rdf.core.{UriRef, Resource, Literal, TypedLiteral, LiteralFactory}
import java.util.Iterator
import _root_.scala.collection.jcl.Conversions

class RichGraphNode(node: GraphNode) extends GraphNode(node.getNode, node.getGraph) {
    /**
     * Operator syntax shortcut to get all objects as <code>RichGraphNode</code>s
     */
    def /(property: UriRef) = {
    	new CollectedIter(new GraphNodeIter(node.getObjects(property)))
	}

    /**
     * Operator syntax shortcut to get all subjects as <code>RichGraphNode</code>s
     */
    def /-(property: UriRef) = {
    	new CollectedIter(new GraphNodeIter(node.getSubjects(property)))
    }

    /**
    * returns a List with the elements of the rdf:List represented by this node
    */
    def !! = (for (listElem <- Conversions.convertList(node.asList)) yield {
    	new RichGraphNode(new GraphNode(listElem, node.getGraph))
    }).toList

    /**
    * returns the specified index from the rdf:List represenetd by this node
    */
    def %!!(index: Int) = new RichGraphNode(new GraphNode(node.asList.get(index),
                                                          node.getGraph))

	/**
	* returns the lexical form of literals, the unicode-string for UriRef for
	* BNodes the value returned by toString
	*/
	def * = {
		val wrappedNode = node.getNode();
		if (wrappedNode.isInstanceOf[Literal]) {
			wrappedNode.asInstanceOf[Literal].getLexicalForm
		} else {
			if (wrappedNode.isInstanceOf[UriRef]) {
				wrappedNode.asInstanceOf[UriRef].getUnicodeString
			} else {
				wrappedNode.toString
			}
		}
	}

	 def as[T](clazz : Class[T]) : T= {
		 LiteralFactory.getInstance().createObject(clazz,
			 node.getNode().asInstanceOf[TypedLiteral])
	 }

    /**
     * Operator syntax shortcut to get the <code>Resource</code> wrapped by this
     * <code>GraphNode</code>
     */
    def ! = {
    	node.getNode()
    }

    private class GraphNodeIter[T <: Resource](base: Iterator[T]) extends Iterator[RichGraphNode] {
        override def hasNext() = {
            base.hasNext();
        }

        override def next() : RichGraphNode = {
        	new RichGraphNode(new GraphNode(base.next(), node.getGraph));
        }

        override def remove() {
        	base.remove()
        }
    }
}


