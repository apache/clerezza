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
package org.apache.clerezza.rdf.core.sparql.query;

import org.apache.clerezza.rdf.core.sparql.update.Update;

/**
 * <p>This interface represents a SPARQL Query or Update.</p>
 *
 * @author hasan
 */
public interface SparqlUnit {

    /**
     * 
	 * @return
	 *		true if it is a {@link Query}, false if it is an {@link Update}
     */
    public boolean isQuery();

    /**
     * 
	 * @return
	 *		the wrapped Query if it is a {@link Query}, null otherwise
     */
	public Query getQuery();

    /**
     * 
	 * @return
	 *		the wrapped Update if it is an {@link Update}, null otherwise
     */
	public Update getUpdate();

}
