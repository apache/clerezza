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
package org.apache.clerezza.rdf.web.core;

import org.apache.clerezza.platform.Constants;
import org.apache.clerezza.platform.dashboard.GlobalMenuItem;
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider;
import org.apache.clerezza.platform.typerendering.scalaserverpages.ScalaServerPagesService;
import org.apache.clerezza.rdf.core.BNode;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.access.security.TcPermission;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.web.ontologies.BACKUP;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.rdf.ontologies.PLATFORM;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.net.URL;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This JAX-RS resource provides a method to retrieve a zip file containing
 * all triple collections that the use may access. The triple collection are
 * serialized in N-Triples format. The URI path of this resource is
 * "/admin/backup".
 *
 * This class adds a global menu-item for users that can read the system graph
 * (as there's currently no dedicated backup-permission).
 *
 * @author hasan, reto
 */
@Component
@Service({Object.class, GlobalMenuItemsProvider.class})
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/admin/backup")
public class Backup implements GlobalMenuItemsProvider {

    final Logger logger = LoggerFactory.getLogger(Backup.class);
    @Reference
    private ScalaServerPagesService scalaServerPagesService;
    private Set<ServiceRegistration> serviceRegistrations = new HashSet<ServiceRegistration>();

    /**
     * The activate method is called when SCR activates the component configuration.
     *
     * @param componentContext
     */
    protected void activate(ComponentContext componentContext) {
        URL templateURL = getClass().getResource("backup-management.ssp");
        serviceRegistrations.add(scalaServerPagesService.registerScalaServerPage(templateURL, BACKUP.BackupAdminPage, "naked",
                MediaType.APPLICATION_XHTML_XML_TYPE));
    }

    protected void deactivate(ComponentContext context) {
        for (ServiceRegistration r : serviceRegistrations) {
            r.unregister();
        }
    }

    /**
     * Get a zipped file containing all triple collections which the
     * user may access. The resource is accessible through the URI path
     * "/admin/backup/download".
     * The triple collections are serialized in N-Triples format before being
     * archived in a single zipped file.
     * A mapping of the names of the files in the archive to triple collection
     * names is available as well in the archive as a text file named
     * triplecollections.nt.
     *
     * @return a response that will cause the creation of a zipped file
     */
    @GET
    @Path("download")
    @Produces("application/zip")
    public Response download() {
        AccessController.checkPermission(new BackupPermission());
        return AccessController.doPrivileged(new PrivilegedAction<Response>() {

            @Override
            public Response run() {
                ResponseBuilder responseBuilder = Response.status(Status.OK).
                        entity(Backup.this);
                responseBuilder.header("Content-Disposition",
                        "attachment; filename=backup" + getCurrentDate() + ".zip");
                return responseBuilder.build();
            }
        });

    }

    @GET
    public GraphNode overviewPage() {
        MGraph resultGraph = new SimpleMGraph();
        GraphNode result = new GraphNode(new BNode(), resultGraph);
        result.addProperty(RDF.type, BACKUP.BackupAdminPage);
        result.addProperty(RDF.type, PLATFORM.HeadedPage);
        return result;
    }

    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    public Set<GlobalMenuItem> getMenuItems() {
        //need backup or restore permission for the menu item to be shown
        Set<GlobalMenuItem> result = new HashSet<GlobalMenuItem>();
        try {
            AccessController.checkPermission(new BackupPermission());
        } catch (AccessControlException e) {
            try {
                AccessController.checkPermission(new RestorePermission());
            } catch (AccessControlException e1) {
                return result;
            }
        }
        result.add(new GlobalMenuItem("/admin/backup",
                "BCK", "Backup and Restore", 5, "Administration"));
        return result;
    }
}
