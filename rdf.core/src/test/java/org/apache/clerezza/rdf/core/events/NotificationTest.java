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
package org.apache.clerezza.rdf.core.events;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.event.FilterTriple;
import org.apache.clerezza.rdf.core.event.GraphEvent;
import org.apache.clerezza.rdf.core.event.GraphListener;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.impl.TripleImpl;

/**
 *
 * @author reto
 */
public class NotificationTest {
    @Test public void getEventsTogether() throws Exception {
        final TripleCollection tc = new SimpleMGraph();
        final List<List<GraphEvent>> eventChunks = new ArrayList<List<GraphEvent>>();
        GraphListener myGraphListener = new GraphListener() {
            @Override
            public void graphChanged(List<GraphEvent> events) {
                eventChunks.add(events);
                //the following causes an event to be added to events
                //(the List we already got)! This is because it doesn't wait
                //on a synhronized(this) as we are already in an evnet dispatch
                //thread
                //tc.add(generateTriple());
            }
        };
        tc.addGraphListener(myGraphListener, new FilterTriple(null, null, null),
                500);
        for (int i = 0; i < 100; i++) {
            tc.add(generateTriple());
        }
        Thread.sleep(600);
        Assert.assertEquals(1, eventChunks.size());
        Assert.assertEquals(100, eventChunks.get(0).size());
        tc.add(generateTriple());
        Thread.sleep(600);
        Assert.assertEquals(2, eventChunks.size());
        Assert.assertEquals(1, eventChunks.get(1).size());
    }


    @Test public void synchroneousEvents() throws Exception {
        final TripleCollection tc = new SimpleMGraph();
        final List<List<GraphEvent>> eventChunks = new ArrayList<List<GraphEvent>>();
        GraphListener myGraphListener = new GraphListener() {
            @Override
            public void graphChanged(List<GraphEvent> events) {
                eventChunks.add(events);
            }
        };
        tc.addGraphListener(myGraphListener, new FilterTriple(null, null, null),
                0);
        for (int i = 0; i < 100; i++) {
            tc.add(generateTriple());
        }
        Assert.assertEquals(100, eventChunks.size());
        Assert.assertEquals(1, eventChunks.get(97).size());
        tc.add(generateTriple());
        Assert.assertEquals(101, eventChunks.size());
        Assert.assertEquals(1, eventChunks.get(100).size());
    }

    private static Triple generateTriple() {
        return new TripleImpl(generateRandomUriRef(), generateRandomUriRef(),
                generateRandomUriRef());
    }

    private static UriRef generateRandomUriRef() {
        return new UriRef("http://example.org/"+Math.random());
    }
}
