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

import java.util.List;
import java.util.Set;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.wymiwyg.commons.util.Util;


/**
 * A VirtualProperty is a function that returns a string given a Resource. This might be either
 * a direct mapping to an RDF property (PropertyHolder) a sequence of RDF properties describing
 * the Path to the value (PathVirtualProperty) or a list of other virtual properties of which
 * the values are concatenated.
 * 
 * @author rbn, tio
 * 
 * @see JoinVirtualProperty
 * @see PathVirtualProperty
 * @see PropertyHolder
 */
public abstract class VirtualProperty {
    /**
     * As opposed to toString this doesn't need to be human readable but unique (as with
     * a strong hash.
     */
    String stringKey;
    
    /**
     * The RDF properties that are used to compute the value of this virtual property.
     */
    Set<UriRef> baseProperties;

    /**
     * Returns the key of this property.
     * 
     * @return the key.
     */
    public String getStringKey() {
        return stringKey;
    }

    /**
     * Returns the properties this virtual property consists of.
     * 
     * @return    the properties. 
     */
    public Set<UriRef> getBaseProperties() {
        return baseProperties;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VirtualProperty)) {
            return false;
        }
        final VirtualProperty other = (VirtualProperty) obj;
        if (!stringKey.equals(other.stringKey)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return stringKey.hashCode();
    }

    /*
     * Computes a SHA-1 hash of the supplied properties.
     */
    protected static String listDigest(List<VirtualProperty> properties) {

        StringBuilder builder = new StringBuilder();
        
        for (VirtualProperty p : properties) {
            if (builder.length() > 0) { 
                builder.append("|");
            }
            builder.append(p.stringKey);
        }
        return Util.sha1(builder.toString());
    }
    
    /**
     * Returns the value of this property.
     * 
     * @param node The node containing the original properties (where to get the literal value).
     * @return The value.
     */
    protected abstract List<String> value(GraphNode node);
    
    /**
     * The shortest path of inverse RDF properties from property to the indexed resource, this is an
     * empty List for PropertyHolders, for properties in a PathVirtualProperties this is a list with the elements
     * passed to its constructor till the first occurrence of property in reverse order.<br/>
     * This method just returns the shortest path as virtual properties with the same base property in
     * different positions are assumed to be very rare.
     * 
     * @param property the property.
     */
    protected abstract List<UriRef> pathToIndexedResource(UriRef property);
}
