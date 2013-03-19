/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.clerezza.rdf.core.impl;
/*
 *
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
 *
*/


import org.junit.Test;
import junit.framework.Assert;

import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.impl.PlainLiteralImpl;
import org.apache.clerezza.rdf.core.impl.TripleImpl;
/**
 *
 * @author reto
 *
 */

public class TripleImplTest {
    
    
    @Test public void tripleEquality() {
        NonLiteral subject = new UriRef("http://example.org/");
        UriRef predicate = new UriRef("http://example.org/property");
        Resource object = new PlainLiteralImpl("property value");
        Triple triple1 = new TripleImpl(subject, predicate, object);
        Triple triple2 = new TripleImpl(subject, predicate, object);
        Assert.assertEquals(triple1.hashCode(), triple2.hashCode());
        Assert.assertEquals(triple1, triple2);    
    }

}
