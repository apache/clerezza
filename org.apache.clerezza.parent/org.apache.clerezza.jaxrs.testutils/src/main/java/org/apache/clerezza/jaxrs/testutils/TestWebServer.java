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
package org.apache.clerezza.jaxrs.testutils;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.ws.rs.core.Application;

import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.wymiwyg.wrhapi.ServerBinding;
import org.wymiwyg.wrhapi.WebServer;
import org.wymiwyg.wrhapi.WebServerFactory;

/**
 * Creates a single webserver, the webserver is stopped when
 * the instance is garbage collected
 * 
 * @author reto
 */
public class TestWebServer {

	private int port;
	private WebServer webServer;
	private boolean stopped;

	/**
	 * starts a webserver for the given jax-rs application
	 * 
	 * @param application
	 *            the application to be exposed
	 * @return the port at which the webserver has been started
	 */
	public TestWebServer(final Application application) {
		try {
			port = PortFinder.getFreePort(InetAddress.getLocalHost());
		} catch (UnknownHostException ex) {
			throw new RuntimeException(ex);
		}

		try {
			// we bind the application in the initializer of a subclass as the
			// method is protected
			JaxRsHandler handler = new JaxRsHandler() {

				{
					registerApplicationConfig(application, "");
				}
			};
			while (true) {

				ServerBinding serverBinding = new ServerBinding() {

					@Override
					public int getPort() {
						return port;
					}

					@Override
					public InetAddress getInetAddress() {
						try {
							return InetAddress.getLocalHost();
						} catch (UnknownHostException ex) {
							throw new RuntimeException(ex);
						}
					}
				};
				try {
					webServer = WebServerFactory.newInstance().startNewWebServer(
							handler, serverBinding);
				} catch (BindException e) {
					//the port delivered by PortFinder might have already 
					//been taken by someone else
					port++;
					continue;
				}
				break;
			}
		} catch (UnknownHostException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/** 
	 * A TestWebServer gets a free port on start-up
	 * 
	 * @return the port the webserver is listening to
	 */
	public int getPort() {
		return port;
	}

	protected void finalize() throws Throwable {
		try {
			if (!stopped) {
				webServer.stop();
			}
		} finally {
			super.finalize();
		}
	}

	/**
	 * stops the webserver
	 */
	public void stop() {
		webServer.stop();
		stopped = true;
	}
}
