/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.stanbol.commons.web.base.jersey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.apache.felix.scr.annotations.References;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Jersey-based RESTful endpoint for the Stanbol Enhancer engines and store.
 * <p>
 * This OSGi component serves as a bridge between the OSGi context and the Servlet context available to JAX-RS
 * resources.
 */
@Component(immediate = true, metatype = true)
@References({
    @Reference(name="component", referenceInterface=Object.class, 
        target="(javax.ws.rs=true)", 
		cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE, 
        policy=ReferencePolicy.DYNAMIC)})
public class JerseyEndpoint {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Property(value = "/")
    public static final String ALIAS_PROPERTY = "org.apache.stanbol.commons.web.alias";

    @Property(value = "/static")
    public static final String STATIC_RESOURCES_URL_ROOT_PROPERTY = "org.apache.stanbol.commons.web.static.url";
    
    @Reference
    HttpService httpService;

    protected ComponentContext componentContext;

    protected ServletContext servletContext;


    protected final List<String> registeredAliases = new ArrayList<String>();

    protected Set<String> exposedHeaders;
    private Set<Object> components = new HashSet<Object>();

    public Dictionary<String,String> getInitParams() {
        Dictionary<String,String> initParams = new Hashtable<String,String>();
        // make jersey automatically turn resources into Viewable models and
        // hence lookup matching freemarker templates
        initParams.put("com.sun.jersey.config.feature.ImplicitViewables", "true");
        return initParams;
    }

    @Activate
    protected void activate(ComponentContext ctx) throws IOException,
                                                 ServletException,
                                                 NamespaceException,
                                                 ConfigurationException {
        componentContext = ctx;
        initJersey();
        
    }

    /** Initialize the Jersey subsystem */
    private synchronized void initJersey() throws NamespaceException, ServletException {
        if (componentContext == null) {
            //we have not yet been activated
            return;
        }
        //end of STANBOL-1073 work around
        if (componentContext == null) {
            log.debug(" ... can not init Jersey Endpoint - Component not yet activated!");
            //throw new IllegalStateException("Null ComponentContext, not activated?");
            return;
        }

        shutdownJersey();

        log.info("(Re)initializing the Stanbol Jersey subsystem");

        // register all the JAX-RS resources into a a JAX-RS application and bind it to a configurable URL
        // prefix
        DefaultApplication app = new DefaultApplication();
        String applicationAlias = (String) componentContext.getProperties().get(ALIAS_PROPERTY);

        app.contributeSingletons(components);

        // bind the aggregate JAX-RS application to a dedicated servlet
        ServletContainer container = new ServletContainer(
                ResourceConfig.forApplication(app));
        httpService.registerServlet(applicationAlias, container, getInitParams(), null);
        registeredAliases.add(applicationAlias);

        // forward the main Stanbol OSGi runtime context so that JAX-RS resources can lookup arbitrary
        // services
        servletContext = container.getServletContext();
        servletContext.setAttribute(BundleContext.class.getName(), componentContext.getBundleContext());
        log.info("JerseyEndpoint servlet registered at {}", applicationAlias);
    }

    /** Shutdown Jersey, if there's anything to do */
    private synchronized void shutdownJersey() {
        log.debug("Unregistering aliases {}", registeredAliases);
        for (String alias : registeredAliases) {
            httpService.unregister(alias);
        }
        registeredAliases.clear();
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {
        shutdownJersey();
        servletContext = null;
        componentContext = null;
    }
    
    protected void bindComponent(Object component) throws IOException,
                                                          ServletException,
                                                          NamespaceException  {
        components.add(component);
        initJersey();
    }

    protected void unbindComponent(Object component) throws IOException,
                                                          ServletException,
                                                          NamespaceException  {
        components.remove(component);
        initJersey();
    }    
}
