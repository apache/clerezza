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



import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 *
 * An Iterator over all permuations of a list.
 *
 * @author reto
 */
class PermutationIterator<T> implements Iterator<List<T>> {

    private Iterator<List<T>> restIterator;
    private List<T> list;
    private List<T> next;
    int posInList = 0; //the position of the last element of next returned list
    //with list, this is the one excluded from restIterator

    PermutationIterator(List<T> list) {
        this.list = Collections.unmodifiableList(list);
        if (list.size() > 1) {
            createRestList();
        }
        prepareNext();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public List<T> next() {
        List<T> result = next;
        if (result == null) {
            throw new NoSuchElementException();
        }
        prepareNext();
        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported");
    }

    private void createRestList() {
        List<T> restList = new ArrayList<T>(list);
        restList.remove(posInList);
        restIterator = new PermutationIterator<T>(restList);
    }

    private void prepareNext() {
        next = getNext();
        
    }
    private List<T> getNext() {
        if (list.size() == 0) {
            return null;
        }
        if (list.size() == 1) {
            if (posInList++ == 0) {
                return new ArrayList<T>(list);
            } else {
                return null;
            }
        } else {
            if (!restIterator.hasNext()) {
                if (posInList < (list.size()-1)) {
                    posInList++;
                    createRestList();
                } else {
                    return null;
                }
            }
            List<T> result = restIterator.next();
            result.add(list.get(posInList));
            return result;
        }
    }

}
