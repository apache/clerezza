/*
 * Copyright 2015 The Apache Software Foundation.
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
package org.apache.clerezza.platform.content;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs exceptions. This is only here lacking a better place.
 *
 * @author developer
 */
@Component()
@Service(Object.class)
@Property(name = "javax.ws.rs", boolValue = true)
@Provider
public class JaxRsExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger log = LoggerFactory.getLogger(JaxRsExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        log.error("toResponse() caught exception", exception);
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }
        return Response.status(getStatusCode(exception))
                .entity(getEntity(exception))
                .build();
    }

    /*
     * Get appropriate HTTP status code for an exception.
     */
    private int getStatusCode(Throwable exception) {
        return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }

    /*
     * Get response body for an exception.
     */
    private Object getEntity(Throwable exception) {
        StringWriter errorMsg = new StringWriter();
        exception.printStackTrace(new PrintWriter(errorMsg));
        return errorMsg.toString();            
    }
}
