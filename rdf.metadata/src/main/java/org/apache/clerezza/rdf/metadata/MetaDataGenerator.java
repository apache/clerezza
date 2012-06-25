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

package org.apache.clerezza.rdf.metadata;

import javax.ws.rs.core.MediaType;
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * An implementation of <code>MetaDataGenerator</code> generates meta data
 * about specified data depending on its media type.
 *
 * @author mir
 */
public interface MetaDataGenerator {

	/**
	 * Generates meta data about the specified bytes depending on its mediaType.
	 * The meta data will be added to the specified graph node.
	 * @param node The graph node to which the meta data will be added
	 * @param data The data from which the meta data is generated
	 * @param mediaType The media type of the data
	 */
	public void generate(GraphNode node, byte[] data, MediaType mediaType);
	
}
