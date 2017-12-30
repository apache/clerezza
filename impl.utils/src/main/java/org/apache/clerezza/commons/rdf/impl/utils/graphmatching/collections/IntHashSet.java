/*
 * Copyright 2002-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.clerezza.commons.rdf.impl.utils.graphmatching.collections;

import java.util.HashSet;
import java.util.Iterator;

/**
 * This is currently just a placeholder implementation based onm HashSet<Integer>
 * an efficient implementation is to store the primitives directly.
 *
 * @author reto
 */
public class IntHashSet extends HashSet<Integer> implements IntSet {

    @Override
    public IntIterator intIterator() {
        final Iterator<Integer> base = iterator();
        return new IntIterator() {

            @Override
            public int nextInt() {
                return base.next();
            }

            @Override
            public boolean hasNext() {
                return base.hasNext();
            }

            @Override
            public Integer next() {
                return base.next();
            }

            @Override
            public void remove() {
                base.remove();
            }
        };
    }

    @Override
    public void add(int i) {
        super.add((Integer)i);
    }

}
