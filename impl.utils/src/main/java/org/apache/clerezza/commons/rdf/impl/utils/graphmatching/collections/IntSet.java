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

import java.util.Set;

/**
 * A IntSet allows directly adding primitive ints to a set, Set<Integer> is 
 * extended, but accessingt he respective methods is less efficient.
 *
 * @author reto
 */
public interface IntSet extends Set<Integer> {
    /**
     *
     * @return an iterator over the primitive int
     */
    public IntIterator intIterator();
    
    public void add(int i);
}
