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

import java.util.Iterator
import _root_.scala.collection
import collection.mutable._
import collection.jcl.Conversions._
import collection.jcl.Buffer

class CollectedIter[T](iter: Iterator[T]) extends Seq[T] {

	def this(jList : java.util.List[T]) = this(jList.iterator())
	

	private val collectedElems = new ArrayBuffer[T]()

	/**
    * This method allows the position to be expressed between parenthesis
    */
    def apply(pos : Int) = {
    	ensureReadTill(pos)
    	collectedElems(pos)
    }


	/**
	* returns a new fully expanded and sorted CollectedIter
	*/
	def sort(lt : (T,T) => Boolean) = {
		new CollectedIter(java.util.Arrays.asList(
				elements.toList.sort(lt).toArray: _*))
	}

    /**
    * Operator style synatx to access a position.
    */
    def %(pos: Int) = apply(pos)

    private def ensureReadTill(pos: Int) {
        while (iter.hasNext && (collectedElems.length-1 <= pos)) {
        	collectedElems += iter.next()
        }
    }

    override def length : Int = {
    	length(Integer.MAX_VALUE)
    }

    /**
    * The value returned is same or less than the length of the collection,
    * the underlying iterator isn't expanded till more than <code>max</code>. If
    * the result is smaller than max it is the length of the collection.
    */
    def length(max: Int) : Int = {
    	ensureReadTill(max)
    	collectedElems.length
	}

    override def toString() = {
    	if (length(1) > 0) {
        	apply(0).toString
    	} else {
        	"empty"
        }
    }

    override def elements = {
    	ensureReadTill(Integer.MAX_VALUE)
        collectedElems.elements
    }
}