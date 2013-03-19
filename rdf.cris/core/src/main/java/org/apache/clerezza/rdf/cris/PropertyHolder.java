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

package org.apache.clerezza.rdf.cris;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * Adapts RDF properties to be VirtualProperties.
 * 
 * @author rbn, daniel
 */
public class PropertyHolder extends VirtualProperty {
    
    /**
     * The wrapped property.
     */
    UriRef property;
    
    /**
     * Creates a new PropertyHolder that wraps an RDF property.
     * 
     * @param property    the property to wrap. 
     */
    public PropertyHolder(UriRef property) {
        this.property = property;
        stringKey = property.getUnicodeString();
        baseProperties = new HashSet();
        baseProperties.add(property);
    }
    
    @Override
    protected List<String> value(GraphNode node) {
        List<String> list = new ArrayList<String>();
        Lock lock = node.readLock();
        lock.lock();
        try {
            Iterator<Resource> iter = node.getObjects(this.property);
            while(iter.hasNext()) {
                Resource resource = iter.next();
                if(resource instanceof Literal) {
                    list.add(((Literal)resource).getLexicalForm());
                } else if(resource instanceof UriRef) {
                    list.add(((UriRef) resource).getUnicodeString());
                }
            }
        } finally {
            lock.unlock();
        }
        return list;
    }
    
    @Override
    protected List<UriRef> pathToIndexedResource(UriRef property) {
        return new ArrayList<UriRef>();
    }
}
