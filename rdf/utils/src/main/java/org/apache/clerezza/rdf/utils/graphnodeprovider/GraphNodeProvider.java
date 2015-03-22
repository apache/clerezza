package org.apache.clerezza.rdf.utils.graphnodeprovider;

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
import org.apache.commons.rdf.Iri;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * A service that returns a GraphNode for a specified named resource, the
 * returned GraphNode has as BaseGraph the ContentGraph provided by the
 * ContentGraphProvider and the for remote uris the Graphs they dereference to
 * and for local URIs with a path-section starting with /user/{username}/ the
 * local-public-graph of that user.
 */
public interface GraphNodeProvider {

    /**
     * Get a GraphNode for the specified resource, see class comments for
     * details.
     */
    GraphNode get(Iri uriRef);

    /**
     * Get a GraphNode for the specified resource, The resource is assumed to be
     * local, i.e. the method behaves like get(Iri) for a Uri with an
     * authority section contained in the Set retuned by
     * <code>org.apache.clerezza.platform.config.PlatformConfig#getBaseUris()</code>
     */
    GraphNode getLocal(Iri uriRef);

    /**
     * return true iff getLocal(uriRef).getNodeContext.size > 0
     */
    boolean existsLocal(Iri uriRef);
}
