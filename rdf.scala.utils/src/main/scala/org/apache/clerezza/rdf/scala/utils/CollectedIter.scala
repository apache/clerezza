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

import java.util.ConcurrentModificationException
import java.util.Iterator
import _root_.scala.collection
import collection.mutable._
import collection.immutable
import _root_.scala.collection.JavaConversions._
import java.util.concurrent.locks.Lock

/**
 * 
 * A Collection that groups the elements of an iterator, giving a view over it as over a
 * sequence. 
 * 
 * The collection takes a function returning an iterator, in order to allow for cases where the
 * iterator needs to be called from the beginning again. i.e. when a ConcurrentModificationException
 * occurs and the iteration is repeated in a section used using the provided readLock.
 */
class CollectedIter[T](iterCreator: () => Iterator[T], readLock: Lock) extends immutable.Seq[T] {

	def this(jList : java.util.List[T], readLock: Lock) = this(() => jList.iterator(), readLock)
	def this() = this( ()=> java.util.Collections.emptyList[T].iterator(),null)

	var iter = iterCreator()
	var firstIter = true

	private val collectedElems = new ArrayBuffer[T]()

	/**
    * This method allows the position to be expressed between parenthesis
    */
    def apply(pos : Int) = {
    	ensureReadTill(pos)
    	collectedElems(pos)
    }


	/**
	* returns a new fully expanded and sorted CollectediterCreator
	*/
	def sort(lt : (T,T) => Boolean) = {
		val sortedElems = iterator.toList.sortWith(lt)
		//TODO this re-expands everything, return sorted-list directly
		new CollectedIter[T](sortedElems, readLock)

	}

    /**
    * Operator style syntax to access a position.
    */
    def %(pos: Int) = apply(pos)

    private def ensureReadTill(pos: Int) {
		try {
			
			while (iter.hasNext && (collectedElems.length-1 <= pos)) {
				val next = iter.next()
				if (firstIter || !collectedElems.contains(next)) {
					collectedElems += next
				}
			}
		} catch {
			case e: ConcurrentModificationException => {
					readLock.lock()
					try {
						iter = iterCreator()
						firstIter = false
						//going beyond pos, do reduce chance we have to aquire another lock
						val biggerPos = if (pos < (Integer.MAX_VALUE - 100)) {
							pos + 100
						} else {
							Integer.MAX_VALUE
						}
						while (iter.hasNext && (collectedElems.length-1 <= biggerPos)) {
							val next = iter.next()
							if (!collectedElems.contains(next)) {
								collectedElems += next
							}
						}
					} finally {
						readLock.unlock()
					}
			}
			case e => throw e
		}
    }

    override def length : Int = {
    	length(Integer.MAX_VALUE)
    }

    /**
    * The value returned is same or less than the length of the collection,
    * the underlying Iterator isn't expanded till more than <code>max</code>. If
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

    override def iterator = {
    	ensureReadTill(Integer.MAX_VALUE)
        collectedElems.iterator
    }
}