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
package org.apache.clerezza.rdf.core.access;

/**
 * This interface is implemented by providers to which {@link TcManagerImpl}
 * delegates.
 *
 * @author reto
 */
public interface WeightedTcProvider extends TcProvider {

    /**
     * Get the weight of this provider. {@link TcManager} will prioritize
     * <code>TcProvider</code>s with greater weight.
     * 
     * @return a positive number indicating the weight of the provider
     */
    int getWeight();
}
