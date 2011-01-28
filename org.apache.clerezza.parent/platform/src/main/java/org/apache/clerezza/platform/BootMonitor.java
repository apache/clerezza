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
package org.apache.clerezza.platform;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.script.Compilable;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import org.apache.felix.scr.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.osgi.service.component.ComponentContext;

/**
 * This component logs a message when Apache Clerezza was launched successfully.
 *
 * @author reto
 */
/*
 * For now this is done with hardcoded dependencies to services which are
 * considered to be necessary for clerezza to be considered operational. This
 * includes a hard-coded number of jax-rs resources. When this conditions are
 * met the messages is logged after a period of 1 second during which no
 * additional root resource has been registered.
 *
 *
 * A more generic system might require the bundle providing the service marking
 * a service as required component of the Apache Clerezza Platform
 *
 * see thread starting at http://www.mail-archive.com/users@felix.apache.org/msg07647.html
 */
@Component(enabled = true, immediate = true)
@Reference(name = "jaxrsResource",
cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
policy = ReferencePolicy.DYNAMIC,
referenceInterface = Object.class,
target = "(javax.ws.rs=true)")
public class BootMonitor {

	@Reference(target = "(javax.script.language=scala)")
	private ScriptEngineFactory scalaScriptEngineFactory;
	private Set<Object> rootResources =
			Collections.synchronizedSet(new HashSet<Object>());
	/**
	 * true when the user has been notified that clerezza started
	 */
	private boolean clerezzaStarted = false;
	private volatile boolean activated = false;
	private final Logger logger = LoggerFactory.getLogger(BootMonitor.class);

	protected void activate(ComponentContext context) {
		//compile a script to initialize scala-compiler (needed by scal a server pages)
		Thread t = new Thread() {

			@Override
			public void run() {
				try {
					((Compilable) scalaScriptEngineFactory.getScriptEngine()).compile("println(\"helo\")");
				} catch (ScriptException ex) {
					logger.warn(ex.toString());
				}
				activated = true;
			}
		};
		t.start();
	}

	protected void bindJaxrsResource(Object p) {
		rootResources.add(p);
		if (!clerezzaStarted && (rootResources.size() == 35)) {
			Thread t = new Thread() {

				@Override
				public void run() {
					int lastSize = 0;
					for (int i = 0; i < 100; i++) {
						if (rootResources.size() == lastSize) {
							clerezzaStarted = true;
							if (activated) {
								logger.info("The Apache Clerezza Platform is now operational.");
								return;
							}
						}
						lastSize = rootResources.size();
						try {
							Thread.sleep(500);
						} catch (InterruptedException ex) {
							throw new RuntimeException();
						}
					}
				}
			};
			t.start();
		}
	}

	protected void unbindJaxrsResource(Object p) {
		rootResources.remove(p);
	}
}
