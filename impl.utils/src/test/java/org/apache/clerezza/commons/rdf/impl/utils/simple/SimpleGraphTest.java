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
package org.apache.clerezza.commons.rdf.impl.utils.simple;

import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.commons.rdf.Triple;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph;

/**
 *
 * @author mir
 */
public class SimpleGraphTest {

    private IRI uriRef1 = new IRI("http://example.org/foo");
    private IRI uriRef2 = new IRI("http://example.org/bar");
    private IRI uriRef3 = new IRI("http://example.org/test");
    private Triple triple1 = new TripleImpl(uriRef1, uriRef2, uriRef3);
    private Triple triple2 = new TripleImpl(uriRef2, uriRef2, uriRef1);
    private Triple triple3 = new TripleImpl(uriRef3, uriRef1, uriRef3);
    private Triple triple4 = new TripleImpl(uriRef1, uriRef3, uriRef2);
    private Triple triple5 = new TripleImpl(uriRef2, uriRef3, uriRef2);
        
    @Test
    public void iteratorRemove() {
        SimpleGraph stc = new SimpleGraph();
        stc.add(triple1);
        stc.add(triple2);
        stc.add(triple3);
        stc.add(triple4);
        stc.add(triple5);
        Iterator<Triple> iter = stc.iterator();
        while (iter.hasNext()) {
            Triple triple = iter.next();
            iter.remove();
        }
        Assert.assertEquals(0, stc.size());
    }

    @Test
    public void removeAll() {
        SimpleGraph stc = new SimpleGraph();
        stc.add(triple1);
        stc.add(triple2);
        stc.add(triple3);
        stc.add(triple4);
        stc.add(triple5);
        SimpleGraph stc2 = new SimpleGraph();
        stc2.add(triple1);
        stc2.add(triple3);
        stc2.add(triple5);
        stc.removeAll(stc2);
        Assert.assertEquals(2, stc.size());
    }
    
    @Test
    public void filterIteratorRemove() {
        SimpleGraph stc = new SimpleGraph();
        stc.add(triple1);
        stc.add(triple2);
        stc.add(triple3);
        stc.add(triple4);
        stc.add(triple5);        
        Iterator<Triple> iter = stc.filter(uriRef1, null, null);
        while (iter.hasNext()) {
            Triple triple = iter.next();
            iter.remove();
        }
        Assert.assertEquals(3, stc.size());
    }

    @Test(expected=ConcurrentModificationException.class)
    public void remove() {
        SimpleGraph stc = new SimpleGraph();
        stc.setCheckConcurrency(true);
        stc.add(triple1);
        stc.add(triple2);
        stc.add(triple3);
        stc.add(triple4);
        stc.add(triple5);
        Iterator<Triple> iter = stc.filter(uriRef1, null, null);
        while (iter.hasNext()) {
            Triple triple = iter.next();
            stc.remove(triple);
        }
        Assert.assertEquals(3, stc.size());
    }
}
