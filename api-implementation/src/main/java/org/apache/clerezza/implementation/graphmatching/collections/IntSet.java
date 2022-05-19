/*
 * Copyright 2002-2004 The Apache Software Foundation.
 *
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

package org.apache.clerezza.implementation.graphmatching.collections;

import java.util.Set;

/**
 * An IntSet allows directly adding primitive ints to a set,
 * {@literal Set<Integer>} is extended, but accessing the respective methods is less efficient.
 *
 * @author reto
 */
public interface IntSet extends Set<Integer> {
    /**
     * @return an iterator over the primitive int
     */
    public IntIterator intIterator();

    public void add(int i);
}
