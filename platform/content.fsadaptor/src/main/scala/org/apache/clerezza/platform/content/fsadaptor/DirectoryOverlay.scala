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

import org.apache.clerezza.commons.rdf.BlankNodeOrIRI
import org.apache.clerezza.commons.rdf.RDFTerm
import org.apache.clerezza.commons.rdf.Triple
import org.apache.clerezza.commons.rdf.Graph
import org.apache.clerezza.commons.rdf.IRI
import org.apache.clerezza.commons.rdf.impl.utils.AbstractGraph
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph
import org.apache.clerezza.utils.IteratorMerger
import org.wymiwyg.commons.util.dirbrowser.PathNode
import java.util.Iterator

class DirectoryOverlay(pathNode: PathNode, base: Graph)
  extends AbstractGraph {

  

  import collection.JavaConversions._

  

  override def performFilter(s: BlankNodeOrIRI, p: IRI,
    o: RDFTerm): Iterator[Triple] = {
    val addedTriples = new SimpleGraph()

    PathNode2Graph.describeInGraph(pathNode, addedTriples)
    
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
      
    new IteratorMerger(new FilteringIterator(base.filter(s, p, o)), addedTriples.filter(s,p, o))
  }

  /**
   * returns an upper bound of the size (removals in abse are not deducted)
   */
  override def performSize = {
    val addedTriples = new SimpleGraph()

      PathNode2Graph.describeInGraph(pathNode, addedTriples)

   base.size+addedTriples.size 
  }
}
