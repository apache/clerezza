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

import org.apache.clerezza.commons.rdf.impl.utils.graphmatching.PermutationIterator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author reto
 */
public class PermutationIteratorTest {

    @Test
    public void simple() {
        List<String> list = new ArrayList<String>();
        PermutationIterator<String> pi = new PermutationIterator<String>(list);
        Assert.assertFalse(pi.hasNext());
    }

    @Test
    public void lessSimple() {
        List<String> list = new ArrayList<String>();
        list.add("Hasan");
        PermutationIterator<String> pi = new PermutationIterator<String>(list);
        Assert.assertTrue(pi.hasNext());
    }

    @Test
    public void regular() {
        List<String> list = new ArrayList<String>();
        list.add("Hasan");
        list.add("Tsuy");
        PermutationIterator<String> pi = new PermutationIterator<String>(list);
        Set<List<String>> permutations = new HashSet<List<String>>();
        while (pi.hasNext()) {
            permutations.add(pi.next());
        }
        Assert.assertEquals(2, permutations.size());
    }

    @Test
    public void extended() {
        List<String> list = new ArrayList<String>();
        list.add("Hasan");
        list.add("Tsuy");
        list.add("Llena");
        PermutationIterator<String> pi = new PermutationIterator<String>(list);
        Set<List<String>> permutations = new HashSet<List<String>>();
        while (pi.hasNext()) {
            permutations.add(pi.next());
        }
        Assert.assertEquals(6, permutations.size());
    }

}
