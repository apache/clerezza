/*
 * Copyright  2002-2006 WYMIWYG (http://wymiwyg.org)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.clerezza.jaxrs.utils.form;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An ordered collection of labeled parameter-values
 * 
 * This class is not thread-safe.
 * 
 * @author reto
 * 
 */
public abstract class AbstractParameterCollection extends
        AbstractCollection<KeyValuePair<ParameterValue>> {

    /**
     * the rawColletion contains the KeyValuePairS, this may be set a value by
     * the subclass or it is given a value on invocation of the size() and
     * getParameter* methods of this class.
     */
    protected List<KeyValuePair<ParameterValue>> rawCollection = null;

    public String[] getParameterNames() {
        if (rawCollection == null) {
            createRawCollection();
        }
        List<String> resultList = new ArrayList<String>();
        for (KeyValuePair<ParameterValue> keyValuePair : rawCollection) {
            String key = keyValuePair.getKey();
            if (!resultList.contains(key)) {
                resultList.add(key);
            }
        }
        return resultList.toArray(new String[resultList.size()]);
    }

    public ParameterValue[] getParameteValues(String parameterName) {
        if (rawCollection == null) {
            createRawCollection();
        }
        List<ParameterValue> resultList = new ArrayList<ParameterValue>();
        for (KeyValuePair<ParameterValue> keyValuePair : rawCollection) {
            if (keyValuePair.getKey().equals(parameterName)) {
                resultList.add(keyValuePair.getValue());
            }
        }
        return resultList.toArray(new ParameterValue[resultList.size()]);
    }

    @Override
    public int size() {
        if (rawCollection == null) {
            createRawCollection();
        }
        return rawCollection.size();
    }

    /**
     * 
     */
    private void createRawCollection() {
        Iterator<KeyValuePair<ParameterValue>> iterator = iterator();
        if (rawCollection == null) {
            // the call to the implementations iterator() didn't create
            // rawCollection
            rawCollection = new ArrayList<KeyValuePair<ParameterValue>>();
            while (iterator.hasNext()) {
                rawCollection.add(iterator.next());
            }
        }
    }

}
