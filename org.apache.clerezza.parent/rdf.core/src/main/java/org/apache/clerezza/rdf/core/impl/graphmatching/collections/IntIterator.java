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

package org.apache.clerezza.rdf.core.impl.graphmatching.collections;

import java.util.Iterator;


/**
 * An iterator allowing to iterate over ints, Iterator<Integer> is extended for
 * compatibility, however accessing nextInt allows faster implementations.
 *
 * @author reto
 */
public interface IntIterator extends Iterator<Integer> {
	public int nextInt();
}
