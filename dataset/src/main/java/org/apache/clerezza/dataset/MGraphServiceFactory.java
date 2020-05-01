/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor  license  agreements.  See the NOTICE file distributed
 * with this work  for  additional  information  regarding  copyright
 * ownership.  The ASF  licenses  this file to you under  the  Apache
 * License, Version 2.0 (the "License"); you may not  use  this  file
 * except in compliance with the License.  You may obtain  a copy  of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless  required  by  applicable law  or  agreed  to  in  writing,
 * software  distributed  under  the  License  is  distributed  on an
 * "AS IS"  BASIS,  WITHOUT  WARRANTIES  OR  CONDITIONS  OF ANY KIND,
 * either  express  or implied.  See  the License  for  the  specific
 * language governing permissions and limitations under  the License.
 */
package org.apache.clerezza.dataset;

import org.apache.clerezza.IRI;
import org.apache.clerezza.dataset.security.TcAccessController;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

/**
 * @see <a href="http://www.osgi.org/javadoc/r4v41/org/osgi/framework/ServiceFactory.html">
 * Interface ServiceFactory</a>
 *
 * @author mir
 */
public class MGraphServiceFactory implements ServiceFactory {
    
    private TcManager tcManager;
    private IRI name;
    private final TcAccessController tcAccessController;

    MGraphServiceFactory(TcManager tcManager, IRI name,
                         TcAccessController tcAccessController) {
        this.tcManager = tcManager;
        this.name = name;
        this.tcAccessController = tcAccessController;
    }

    @Override
    public Object getService(Bundle arg0, ServiceRegistration arg1) {
        return new SecuredGraph(tcManager.getMGraph(name), name, tcAccessController);
    }

    @Override
    public void ungetService(Bundle arg0, ServiceRegistration arg1, Object arg2) {
    }
}
