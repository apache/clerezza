package org.apache.clerezza.commons.rdf.impl.utils.graphmatching;

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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An iterator over all possible mapping beetween the elemnets of two sets of
 * the same size, each mapping maps each element from set1 to a disctinct one of
 * set2.
 *
 *
 *
 * @author reto
 */
class MappingIterator<T,U> implements Iterator<Map<T, U>> {

    private List<T> list1;
    private Iterator<List<U>> permutationList2Iterator;


    public MappingIterator(Set<T> set1, Set<U> set2) {
        if (set1.size() != set2.size()) {
            throw new IllegalArgumentException();
        }
        this.list1 = new ArrayList<T>(set1);
        permutationList2Iterator = new PermutationIterator<U>(
                new ArrayList<U>(set2));
    }

    @Override
    public boolean hasNext() {
        return permutationList2Iterator.hasNext();
    }

    @Override
    public Map<T, U> next() {
        List<U> list2 = permutationList2Iterator.next();
        Map<T, U> result = new HashMap<T, U>(list1.size());
        for (int i = 0; i < list1.size(); i++) {
            result.put(list1.get(i), list2.get(i));
        }
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported.");
    }



}
