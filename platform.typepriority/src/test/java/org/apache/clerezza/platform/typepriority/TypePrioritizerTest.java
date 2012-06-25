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
package org.apache.clerezza.platform.typepriority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.platform.config.SystemConfig;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.rdf.core.test.LockableMGraphWrapperForTesting;
import org.apache.clerezza.rdf.ontologies.FOAF;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.RdfList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author rbn
 */
public class TypePrioritizerTest {

	TypePrioritizer typePrioritizer;

	@Before
	public void before() {
		typePrioritizer = new TypePrioritizer();
		MGraph mGraph = new SimpleMGraph();
		RdfList rdfList = new RdfList(TypePrioritizer.typePriorityListUri, mGraph);
		rdfList.add(FOAF.Person);
		rdfList.add(FOAF.Group);
		rdfList.add(FOAF.Agent);
		final LockableMGraph systemGraph = new LockableMGraphWrapperForTesting(mGraph);
		typePrioritizer.bindSystemGraph(systemGraph);
	}

	@Test
	public void oderList() {
		List<UriRef> l = new ArrayList<UriRef>();
		l.add(FOAF.Agent);
		l.add(RDF.Bag);
		l.add(FOAF.Person);
		Iterator<UriRef> iter = typePrioritizer.iterate(l);
		Assert.assertEquals(FOAF.Person, iter.next());
		Assert.assertEquals(FOAF.Agent, iter.next());
		Assert.assertEquals(RDF.Bag, iter.next());
		Assert.assertFalse(iter.hasNext());
	}

}
