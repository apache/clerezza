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
package org.apache.clerezza.platform.typerendering;

import java.util.ArrayList;
import javax.ws.rs.ext.RuntimeDelegate;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.access.LockableMGraphWrapper;
import org.apache.clerezza.rdf.core.impl.SimpleMGraph;
import org.apache.clerezza.triaxrs.delegate.RuntimeDelegateImpl;

/**
 * @author mir
 * 
 */
public class RendereringManagerTest extends RendereringTest {

	private RenderletRendererFactoryImpl manager;

	@Override
	protected RenderletManager createNewRenderletManager() {
		RenderletRendererFactoryImpl renderer = new RenderletRendererFactoryImpl();
		manager = renderer;
		renderer.rdfTypePrioList = new ArrayList<Resource>();		
		RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
		renderer.bindConfigGraph(new LockableMGraphWrapper(new SimpleMGraph()));
		renderer.registerRenderletService(renderletMockA.pid, renderletMockA);
		renderer.registerRenderletService(renderletMockB.pid, renderletMockB);
		return renderer;
	}

	protected RendererFactory getRendererFactory() {
		return manager;
	}
}
