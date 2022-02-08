/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor  license  agreements.  See the NOTICE file distributed
 * with this work  for  additional  information  regarding  copyright
 * ownership.  The ASF  licenses  this file to you under  the  Apache
 * License, Version 2.0 (the "License"); you may not  use  this  file
 * except in compliance with the License.  You may obtain  a copy  of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless  required  by  applicable law  or  agreed  to  in  writing,
 * software  distributed  under  the  License  is  distributed  on an
 * "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR  CONDITIONS  OF ANY KIND,
 * either  express  or implied.  See  the License  for  the  specific
 * language governing permissions and limitations under  the License.
 */
package org.apache.clerezza.scala.utils

import java.util.Iterator

import org.apache.clerezza.{Graph, IRI, Literal, RDFTerm}
import org.apache.clerezza._
import org.apache.clerezza.implementation.literal.LiteralFactory
import org.apache.clerezza.utils.{GraphNode, UnionGraph}
import org.apache.clerezza.implementation.in_memory.SimpleGraph
import org.apache.clerezza.implementation.literal.LiteralFactory
import org.apache.clerezza.ontologies.RDF
import org.apache.clerezza.utils.{GraphNode, UnionGraph}

//import _root_.scala.collection.JavaConversions._
import _root_.scala.reflect.Manifest
import _root_.scala.jdk.CollectionConverters._;

/**
  * A RichGraphNode decorates A GraphNode with additional method to be part on a DSL-style scala library.
  *
  * The default constructor is same a the GraphNode constructor, i.e. it takes the node and its context
  * Triple-collection
  *
  * @param resource the node represented by this RichGraphNode
  * @param graph    the Graph that describes the resource
  */
class RichGraphNode(resource: RDFTerm, graph: Graph) extends GraphNode(resource, graph) {

    /**
      * Construct a RichGraphNode given an existing [[GraphNde]]
      *
      * @param node The GraphNode to be wrapped
      */
    def this(node: GraphNode) = this(node.getNode, node.getGraph)

    /**
      * Operator syntax shortcut to get all objects as <code>RichGraphNode</code>
      *
      * @return all objects of the specified property of the node wrapped by this object
      */
    def /(property: IRI): CollectedIter[RichGraphNode] = {
        new CollectedIter[RichGraphNode](() => new GraphNodeIter(getObjects(property)), readLock)
    }

    /**
      * Operator syntax shortcut to get all subjects as <code>RichGraphNode</code>ref
      *
      * @param property the property for which the subjects pointing to this node by that property are requested
      * @return the matching resources
      */
    def /-(property: IRI): CollectedIter[RichGraphNode] = {
        new CollectedIter[RichGraphNode](() => new GraphNodeIter(getSubjects(property)), readLock)
    }

    /**
      * Get the elements of the rdf:List represented by this node
      *
      * @return a List with the elements of the rdf:List represented by this node
      */
    def !! = (for (listElem <- asList.asScala) yield {
        new RichGraphNode(new GraphNode(listElem, getGraph))
    }).toList

    /**
      * get a specified of the rdf:List represented by this node
      *
      * @return the specified index value
      */
    def %!!(index: Int) = new RichGraphNode(new GraphNode(asList.get(index),
        getGraph))

    /**
      * produces a default String representation for the node, this is the lexical form of literals,
      * the unicode-string for IRI and for BlankNodes the value returned by toString
      *
      * @return the default string representation of the node
      */
    def * : String = {
        getNode() match {
            case lit: Literal => lit.getLexicalForm
            case uri: IRI => uri.getUnicodeString
            case wrappedNode => wrappedNode.toString
        }
    }

    private def asClass[T](clazz: Class[T]): T = {
        val typedLiteral = getNode().asInstanceOf[Literal]
        clazz match {
            case c if (c == classOf[Boolean]) => LiteralFactory.getInstance().createObject(
                classOf[java.lang.Boolean], typedLiteral).booleanValue.asInstanceOf[T]
            case _ => LiteralFactory.getInstance().createObject(clazz, typedLiteral)
        }
    }

    /**
      * Creates an instance of specified Class-Type representing the value of the literal wrapped by this
      * <code>GraphNode</code>
      *
      * @return the literal represented by this node as instance of the specified type
      */
    def as[T](implicit m: Manifest[T]): T = {
        asClass(m.runtimeClass.asInstanceOf[Class[T]])
    }

    /**
      * Operator syntax shortcut to get the <code>RDFTerm</code> wrapped by this
      * <code>GraphNode</code>
      *
      * @return the node represented by this GraphNode as RDFTerm, same as <code>getNode</code>
      */
    def ! = {
        getNode()
    }

    private class GraphNodeIter[T <: RDFTerm](base: Iterator[T]) extends Iterator[RichGraphNode] {
        override def hasNext() = {
            base.hasNext();
        }

        override def next(): RichGraphNode = {
            new RichGraphNode(new GraphNode(base.next(), getGraph));
        }

        override def remove() {
            base.remove()
        }
    }

    /**
      * Sets the RDF:type of the subject */
    def a(rdfclass: IRI): RichGraphNode = {
        addProperty(RDF.`type`, rdfclass)
        return this
    }

    /*
     * create an RichGraphNode from this one where the backing graph is protected from writes by a new
     * SimpleGraph.
     */
    def protect(): RichGraphNode = new RichGraphNode(getNode, new UnionGraph(new SimpleGraph(), graph))


    /**
      * relate the subject via the given relation to....
      */
    def --(rel: RDFTerm): DashTuple = new DashTuple(rel)

    def --(rel: RichGraphNode): DashTuple = new DashTuple(rel.getNode)


    /**
      * relate the subject via the inverse of the given relation to....
      */
    def <--(tuple: RichGraphNode#DashTuple): RichGraphNode = {
        val inversePropertyRes = tuple.first.getNode
        val inverseProperty: IRI = inversePropertyRes match {
            case p: IRI => p
            case _ => throw new RuntimeException("DashTuple must be a IRI")
        }
        RichGraphNode.this.addInverseProperty(inverseProperty, tuple.second)
        RichGraphNode.this
    }


    /** class for Inverse relations with the current RichGraphNode.ref as object */
    //TODO add support for adding many for symmetry reasons
    //  class InverseDashTuple(rel: DashTuple) {
    //
    //    /**
    //     * ...to the following non literal
    //     */
    //    def --(subj: BlankNodeOrIRI): RichGraphNode = {
    //      RichGraphNode.this.addInverseProperty(rel, subj)
    //      RichGraphNode.this
    //    }
    //
    //    /**
    //     * ...to the following resource (given as a string)
    //     */
    //    def --(subj: String): RichGraphNode = --(new IRI(subj))
    //
    //    /**
    //     * ...to the following EzGraphNode
    //     * (useful for opening a new parenthesis and specifying other things in more detail
    //     */
    //    def --(subj: GraphNode): RichGraphNode = {
    //      --(subj.getNode.asInstanceOf[BlankNodeOrIRI])
    //    }
    //    // since we can only have inverses from non literals (howto deal with bndoes?)
    //  }

    /**
      * class for relations with the current RichGraphNode.ref as subject
      */
    class DashTuple(val second: RDFTerm) {

        val first = RichGraphNode.this

        /**
          * ...to the following non resource
          */
        def -->(obj: RDFTerm): RichGraphNode = {
            val property = second match {
                case u: IRI => u;
                case _ => throw new RuntimeException("Property must be a IRI")
            }
            RichGraphNode.this.addProperty(property, obj)
            RichGraphNode.this
        }


        /**
          * ...to the EzGraphNode, which is useful for opening a parenthesis.
          */
        def -->(sub: GraphNode): RichGraphNode = {
            //RichGraphNode.this + sub
            -->(sub.getNode)
        }

        /**
          * Add one relation for each member of the iterable collection
          */
        def -->>[T <: RDFTerm](uris: Iterable[T]): RichGraphNode = {
            for (u <- uris) -->(u)
            RichGraphNode.this
        }
    }

}
