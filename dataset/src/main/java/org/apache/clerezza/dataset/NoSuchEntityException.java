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

/**
 * is thrown on an attempt to perform an operation on an entity (i.e. a
 * <code>ImmutableGraph</code> or <code>Graph</code> that does not exist.
 *
 * @author reto
 */
public class NoSuchEntityException extends RuntimeException {
    private IRI entityName;

    /**
     * creates an exception indicating that the entity with the specified name
     * does not exist.
     * 
     * @param entityName the name for which no entity exists
     */
    public NoSuchEntityException(IRI entityName) {
        super("No such entity: "+entityName);
        this.entityName = entityName;
    }

    /**
     * the name for which no entity exists.
     * 
     * @return the name of the entity that doesn't exist
     */
    public IRI getEntityName() {
        return entityName;
    }
}
