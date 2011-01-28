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
package org.apache.clerezza.rdf.core.serializedform;

import java.io.OutputStream;

import org.apache.clerezza.rdf.core.TripleCollection;


/**
 * An instance of this class serializes <code>TripleCollection</code>s to a
 * specified serialization format. The supported formats are indicated using the
 * {@link SupportedFormat} annotation.
 *
 * @author mir
 */
public interface SerializingProvider {
	
	/** Serializes a <code>TripleCollection</code> to a specified
	 * <code>OutputStream</code> in the format identified by
	 * <code>formatIdentifier</code>. This method will be invoked
	 * for a supported format, a format is considered as supported if the part
	 * before a ';'-character in the <code>formatIdentifier</code> matches
	 * a <code>SupportedFormat</code> annotation of the implementing class.
	 * 
	 * @param outputStream
	 * @param tc
	 * @param formatIdentifier
	 */
	public void serialize(OutputStream outputStream, TripleCollection tc,
			String formatIdentifier);

}
