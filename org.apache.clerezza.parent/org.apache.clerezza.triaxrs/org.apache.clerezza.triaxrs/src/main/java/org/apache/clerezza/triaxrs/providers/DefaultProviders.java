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
package org.apache.clerezza.triaxrs.providers;

import org.apache.clerezza.triaxrs.providers.provided.*;

/**
 * this class defines and creates the default providers
 * 
 * @author szalay
 * @version $Id: $
 */

public class DefaultProviders {

	public static Class[] getDefaultProviders() {

		Class[] result = { StringMessageBodyReader.class,
				StringMessageBodyWriter.class, ByteArrayProvider.class,
				FileProvider.class, FormMultivaluedMapProvider.class,
				InputStreamProvider.class, ReaderProvider.class,
				StreamingOutputProvider.class,
				SourceProvider.StreamSourceProvider.class,
				SourceProvider.SAXSourceProvider.class,
				SourceProvider.DOMSourceProvider.class};

		// commented out because more dependencies needed:
		// XMLJAXBElementProvider.class, XMLRootElementProvider.class };
		// JSONArrayProvider.class, JSONObjectProvider.class,
		// JSONRootElementProvider.class, SourceProvider.class,
		// DataSourceProvider.class, MimeMultipartProvider.class,
		return result;
	}
}

// $Log: $

