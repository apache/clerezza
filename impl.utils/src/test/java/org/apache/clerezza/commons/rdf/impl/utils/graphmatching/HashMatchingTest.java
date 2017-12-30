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

package org.apache.clerezza.commons.rdf.impl.utils.graphmatching;


import java.util.Map;

import org.apache.clerezza.commons.rdf.BlankNode;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class HashMatchingTest {

    @Test
    public void twoLine() throws GraphNotIsomorphicException {
        BlankNodeOrIRI start1 = new BlankNode();
        Graph tc1 = Utils4Testing.generateLine(4,start1);
        tc1.addAll(Utils4Testing.generateLine(5,start1));
        BlankNodeOrIRI start2 = new BlankNode();
        Graph tc2 = Utils4Testing.generateLine(5,start2);
        tc2.addAll(Utils4Testing.generateLine(4,start2));
        Assert.assertEquals(9, tc1.size());
        final Map<BlankNode, BlankNode> mapping = new HashMatching(tc1, tc2).getMatchings();
        Assert.assertNotNull(mapping);
        Assert.assertEquals(10, mapping.size());
    }

}
