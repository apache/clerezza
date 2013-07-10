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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.clerezza.jaxrs.utils.RedirectUtil;
import org.apache.clerezza.jaxrs.utils.form.FormFile;
import org.apache.clerezza.jaxrs.utils.form.MultiPartBody;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

/**
 * A service to restore the triple collections of a clerezza platform instance
 *
 * @author reto
 */
@Component
@Service({Object.class, PlatformRestorer.class})
@Property(name = "javax.ws.rs", boolValue = true)
@Path("/admin/backup/restore")
public class PlatformRestorer {

    @Reference
    private Restorer restorer;

    @Reference
    private TcManager tcManager;

    /**
     * Restores the triple collection of TcManager from a backup
     *
     * @param backupData the backup data
     */
    public void restore(InputStream backupData) throws IOException {
        restorer.restore(backupData, tcManager);
    }

    @POST
    public Response restore(MultiPartBody body, @Context final UriInfo uriInfo) 
            throws Throwable {
        AccessController.checkPermission(new RestorePermission());
        FormFile[] files = body.getFormFileParameterValues("file");
        if (files.length != 1) {
            throw new RuntimeException("Must submit exactly one file");
        }
        final FormFile file = files[0];
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Response>() {

                @Override
                public Response run() throws IOException {
                    restore(new ByteArrayInputStream(file.getContent()));
                    return RedirectUtil.createSeeOtherResponse("/admin/backup", uriInfo);
                }
            });
        } catch (PrivilegedActionException ex) {
            throw ex.getCause();
        }
    }

}
