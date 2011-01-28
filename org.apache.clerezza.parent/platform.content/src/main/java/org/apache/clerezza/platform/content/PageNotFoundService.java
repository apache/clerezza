/*
 *  Copyright 2010 reto.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.clerezza.platform.content;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * An instance of this service is called by DiscoBitHandler if a resource
 * is not found in the content graph.
 *
 * @author reto
 */
public interface PageNotFoundService {

	/**
	 * Creates a response when a resource could not be found in the Content 
	 * Graph, this is a 404 response.
	 * 
	 * @param uriInfo
	 * @return
	 */
	public Response createResponse(UriInfo uriInfo);

}
