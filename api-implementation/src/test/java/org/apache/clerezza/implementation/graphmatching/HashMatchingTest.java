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

package org.apache.clerezza.implementation.graphmatching;

import org.apache.clerezza.BlankNodeOrIRI;
import org.apache.clerezza.BlankNode;
import org.apache.clerezza.Graph;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.Map;

/**
 * @author reto
 */
@RunWith(JUnitPlatform.class)
public class HashMatchingTest {

    @Test
    public void twoLine() throws GraphNotIsomorphicException {
        BlankNodeOrIRI start1 = new BlankNode();
        Graph tc1 = Utils4Testing.generateLine(4, start1);
        tc1.addAll(Utils4Testing.generateLine(5, start1));
        BlankNodeOrIRI start2 = new BlankNode();
        Graph tc2 = Utils4Testing.generateLine(5, start2);
        tc2.addAll(Utils4Testing.generateLine(4, start2));
        Assertions.assertEquals(9, tc1.size());
        final Map<BlankNode, BlankNode> mapping = new HashMatching(tc1, tc2).getMatchings();
        Assertions.assertNotNull(mapping);
        Assertions.assertEquals(10, mapping.size());
    }

}
