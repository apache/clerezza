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
package org.apache.clerezza.platform.documentation.viewer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.apache.clerezza.commons.rdf.IRI;

/**
 *
 * @author mir
 */
public class SortTest {

    @Test
    public void sortTest() {
        DocumentationViewer docViewer = new DocumentationViewer();

        Map<IRI,DocumentationViewer.DocumentationItem> uri2docItemObj =
            new HashMap<IRI,DocumentationViewer.DocumentationItem>();
        IRI ref1 = new IRI("ref1");
        IRI ref2 = new IRI("ref2");
        IRI ref3 = new IRI("ref3");
        IRI ref4 = new IRI("ref4");
        IRI ref5 = new IRI("ref5");

        Set<IRI> set1 = new HashSet<IRI>();
        DocumentationViewer.DocumentationItem a =
            new DocumentationViewer.DocumentationItem(ref1, set1, uri2docItemObj);

        Set<IRI> set2 = new HashSet<IRI>();
        set2.add(ref1);
        DocumentationViewer.DocumentationItem b =
            new DocumentationViewer.DocumentationItem(ref2, set2, uri2docItemObj);

        Set<IRI> set3 = new HashSet<IRI>();
        set3.add(ref2);
        DocumentationViewer.DocumentationItem c =
            new DocumentationViewer.DocumentationItem(ref3, set3, uri2docItemObj);

        Set<IRI> set4 = new HashSet<IRI>();
        set4.add(ref3);
        DocumentationViewer.DocumentationItem d =
            new DocumentationViewer.DocumentationItem(ref4, set4, uri2docItemObj);

        Set<IRI> set5 = new HashSet<IRI>();
        set5.add(ref4);
        DocumentationViewer.DocumentationItem e =
            new DocumentationViewer.DocumentationItem(ref5, set5, uri2docItemObj);

        uri2docItemObj.put(ref2, b);
        uri2docItemObj.put(ref4, d);
        uri2docItemObj.put(ref1, a);
        uri2docItemObj.put(ref3, c);
        uri2docItemObj.put(ref5, e);

        List<DocumentationViewer.DocumentationItem> orderedList = 
            docViewer.sortDocItems(uri2docItemObj.values());

        Iterator<DocumentationViewer.DocumentationItem> iter =
            orderedList.iterator();

        Object[] expecteds =
            new DocumentationViewer.DocumentationItem[5];

        expecteds[0] = a;
        expecteds[1] = b;
        expecteds[2] = c;
        expecteds[3] = d;
        expecteds[4] = e;

        Assert.assertArrayEquals(expecteds, orderedList.toArray());
    }

    @Test(expected=RuntimeException.class)
    public void cycleTest() {
        DocumentationViewer docViewer = new DocumentationViewer();

        Map<IRI,DocumentationViewer.DocumentationItem> uri2docItemObj =
            new HashMap<IRI,DocumentationViewer.DocumentationItem>();
        IRI ref1 = new IRI("ref1");
        IRI ref2 = new IRI("ref2");
        IRI ref3 = new IRI("ref3");
        IRI ref4 = new IRI("ref4");
        IRI ref5 = new IRI("ref5");

        Set<IRI> set1 = new HashSet<IRI>();
        set1.add(ref5);
        DocumentationViewer.DocumentationItem a =
            new DocumentationViewer.DocumentationItem(ref1, set1, uri2docItemObj);

        Set<IRI> set2 = new HashSet<IRI>();
        set2.add(ref1);
        DocumentationViewer.DocumentationItem b =
            new DocumentationViewer.DocumentationItem(ref2, set2, uri2docItemObj);

        Set<IRI> set3 = new HashSet<IRI>();
        set3.add(ref2);
        DocumentationViewer.DocumentationItem c =
            new DocumentationViewer.DocumentationItem(ref3, set3, uri2docItemObj);

        Set<IRI> set4 = new HashSet<IRI>();
        set4.add(ref3);
        DocumentationViewer.DocumentationItem d =
            new DocumentationViewer.DocumentationItem(ref4, set4, uri2docItemObj);

        Set<IRI> set5 = new HashSet<IRI>();
        set5.add(ref4);
        DocumentationViewer.DocumentationItem e =
            new DocumentationViewer.DocumentationItem(ref5, set5, uri2docItemObj);

        uri2docItemObj.put(ref2, b);
        uri2docItemObj.put(ref4, d);
        uri2docItemObj.put(ref1, a);
        uri2docItemObj.put(ref3, c);
        uri2docItemObj.put(ref5, e);
        
        List<DocumentationViewer.DocumentationItem> orderedList =
            docViewer.sortDocItems(uri2docItemObj.values());
    }
}
