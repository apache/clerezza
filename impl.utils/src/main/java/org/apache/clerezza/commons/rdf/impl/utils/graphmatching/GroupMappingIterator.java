/*
 *  Copyright 2010 reto.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.clerezza.commons.rdf.impl.utils.graphmatching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Iterates over all mappings from each element of every Set<T> to each
 * elemenent of their corresponding Set<U>.
 *
 * @author reto
 */
class GroupMappingIterator<T,U> implements Iterator<Map<T, U>> {

    private Iterator<Map<T, U>> firstPartIter;
    private Map<T, U> currentFirstPart;
    final private Map<Set<T>, Set<U>> restMap;
    private Iterator<Map<T, U>> currentRestPartIter;

    static <T,U> Iterator<Map<T, U>> create(Map<Set<T>, Set<U>> matchingGroups) {
        if (matchingGroups.size() > 1) {
            return new GroupMappingIterator<T, U>(matchingGroups);
        } else {
            if (matchingGroups.size() == 0) {
                return new ArrayList<Map<T, U>>(0).iterator();
            }
            Map.Entry<Set<T>, Set<U>> entry = matchingGroups.entrySet().iterator().next();
            return new MappingIterator<T,U>(entry.getKey(),
                        entry.getValue());
        }
    }

    private GroupMappingIterator(Map<Set<T>, Set<U>> matchingGroups) {
        if (matchingGroups.size() == 0) {
            throw new IllegalArgumentException("matchingGroups must not be empty");
        }
        restMap = new HashMap<Set<T>, Set<U>>();
        boolean first = true;
        for (Map.Entry<Set<T>, Set<U>> entry : matchingGroups.entrySet()) {
            if (first) {
                firstPartIter = new MappingIterator<T,U>(entry.getKey(),
                        entry.getValue());
                first = false;
            } else {
                restMap.put(entry.getKey(), entry.getValue());
            }
        }
        currentRestPartIter = create(restMap);
        currentFirstPart = firstPartIter.next();
    }

    @Override
    public boolean hasNext() {
        return firstPartIter.hasNext() || currentRestPartIter.hasNext();
    }

    @Override
    public Map<T, U> next() {
        Map<T, U> restPart;
        if (currentRestPartIter.hasNext()) {
            restPart = currentRestPartIter.next();
        } else {
            if (firstPartIter.hasNext()) {
                currentFirstPart = firstPartIter.next();
                currentRestPartIter = create(restMap);
                restPart = currentRestPartIter.next();
            } else {
                throw new NoSuchElementException();
            }
        }
        Map<T, U> result = new HashMap<T, U>(restPart);
        result.putAll(currentFirstPart);
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported.");
    }

}
