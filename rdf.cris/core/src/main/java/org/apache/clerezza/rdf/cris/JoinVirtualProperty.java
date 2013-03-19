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
import java.util.List;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * A property that concatenates the literal-content of several properties.
 * 
 * @author rbn, daniel
 */
public class JoinVirtualProperty extends VirtualProperty {

    /**
     * The properties that constitute this join-property.
     */
    List<VirtualProperty> properties;

    /**
     * Creates a JoinVirtualPorperty from the supplied properties.
     * 
     * @param properties the properties.
     */
    public JoinVirtualProperty(List<VirtualProperty> properties) {
        this.stringKey = "J" + VirtualProperty.listDigest(properties);
        this.properties = properties;


        this.baseProperties = new HashSet<UriRef>();

        for (VirtualProperty property : this.properties) {
            for (UriRef p : property.baseProperties) {
                this.baseProperties.add(p);
            }
        }
    }

    @Override
    public List<String> value(GraphNode node) {
        List<String> list = new ArrayList();
        list.add(singleValue(node));
        return list;
    }

    @Override
    protected List<UriRef> pathToIndexedResource(UriRef property) {

        List tempList = null;
        for (VirtualProperty p : this.properties) {
            List currentList = p.pathToIndexedResource(property);
            if (tempList == null) {
                tempList = currentList;
            } else if (tempList.size() < currentList.size()) {
                tempList = currentList;

            }
        }
        return tempList;
    }
    
    private String singleValue(GraphNode node) {
        StringBuilder builder = new StringBuilder();    
        for (VirtualProperty property : this.properties) {
            for(String p : property.value(node)) {
                builder.append(p);
                builder.append(" ");
            }
        }
        if(builder.length() == 0) {
            return "";
        }
        return builder.deleteCharAt(builder.length() -1 ).toString();
    }
}
