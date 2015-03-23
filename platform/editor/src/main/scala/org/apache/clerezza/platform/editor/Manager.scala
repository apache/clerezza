/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.clerezza.platform.editor

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import org.apache.clerezza.rdf.core.Graph
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

@Component
@Service(Array(classOf[Object]))
@Property(name="javax.ws.rs", boolValue=Array(true))
@Path("/tools/editor/create")
class Manager {
  
  @Reference
  var cgp: ContentGraphProvider = null;
  
  @GET
  def sayHello = "Size of content graph: "+cgp.getContentGraph.size
  
  @PUT
  def putData(graph: Graph) = {
    //TODO implement
    graph
  }

}
