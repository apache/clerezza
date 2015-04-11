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
package org.apache.clerezza.platform.graphprovider.content;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.clerezza.platform.Constants;

import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.IRI;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.utils.UnionGraph;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 * A service providing a method to retrieve a <code>UnionGraph</code> containing
 * the ContentGraph and additional graphs.
 * 
 * Over the Configuration Admin Service additional graphs can be added
 * permanently. With the methods <code>addTemporaryAdditionGraph</code> and
 * <code>removeTemporaryAdditionGraph</code> graphs can be added temporarily.
 * Temporarily means that after restarting the org.apache.clerezza.platform.graphprovider.content
 * bundle, these graphs will no longer be returned in the <code>UnionGraph</code>.
 * 
 */
@Component
@Service(ContentGraphProvider.class)
public class ContentGraphProvider {


    /**
     * Service property header, which contains the URIs of additional
     * <code>Graph</code>s that are united as read-only to the ImmutableGraph
     * returned by <code>getContentGraph</code>
     * 
     * @scr.property name="additions" values.name="" description="Contains
     *               additional Graphs that are added to the content
     *               graph for reading"
     */
    @Property(name = "additions", description = "Contains "
     +               "additional Graphs that are added to the content "
     +               "graph for reading", value = {"",""})
    public static final String CONTENT_ADDITIONS = "additions";

    @Reference
    private TcManager tcManager;



    /**
     * The URIs of the read-only addition-<code>Graph</code>s
     */
    private IRI[] additions;

    private ReentrantReadWriteLock configLock = new ReentrantReadWriteLock();

    protected void activate(ComponentContext context) {
        try {
            tcManager.getGraph(Constants.CONTENT_GRAPH_URI);
        } catch (NoSuchEntityException nsee) {
            tcManager.createGraph(Constants.CONTENT_GRAPH_URI);
        }
        String[] additionUriStrings = (String[]) context.getProperties().get(
                CONTENT_ADDITIONS);
        additions = new IRI[additionUriStrings.length];
        for (int i = 0; i < additionUriStrings.length; i++) {
            additions[i] = new IRI(additionUriStrings[i]);

        }
    }

    public Graph getContentGraph() {
        configLock.readLock().lock();
        try {
            Graph[] united = new Graph[additions.length + 1];
            int i = 0;
            united[i++] = tcManager.getGraph(Constants.CONTENT_GRAPH_URI);
            for (IRI uriRef : additions) {
                united[i++] = tcManager.getGraph(uriRef);
            }
            return new UnionGraph(united);
        } finally {
            configLock.readLock().unlock();
        }
    }

    public void addTemporaryAdditionGraph(IRI graphName) {
        configLock.writeLock().lock();
        try {
            Set<IRI> additionsSet = new HashSet<IRI>(Arrays
                    .asList(additions));
            additionsSet.add(graphName);
            additions = additionsSet.toArray(new IRI[additionsSet.size()]);
        } finally {
            configLock.writeLock().unlock();
        }
    }

    public void removeTemporaryAdditionGraph(IRI graphName) {
        configLock.writeLock().lock();
        try {
            Set<IRI> additionsSet = new HashSet<IRI>(Arrays
                    .asList(additions));
            additionsSet.remove(graphName);
            additions = additionsSet.toArray(new IRI[additionsSet.size()]);
        } finally {
            configLock.writeLock().unlock();
        }
    }
}
