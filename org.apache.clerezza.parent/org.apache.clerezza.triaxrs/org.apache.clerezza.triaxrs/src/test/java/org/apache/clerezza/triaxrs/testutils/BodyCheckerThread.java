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
package org.apache.clerezza.triaxrs.testutils;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.clerezza.triaxrs.JaxRsHandler;
import org.apache.clerezza.triaxrs.mock.ResponseImpl;
import org.wymiwyg.wrhapi.HandlerException;
import org.wymiwyg.wrhapi.Request;

/**
 * A thread that repeatedly handles a specified <code>Request</code> with 
 * a specified <code>JaxRsHandler</code> and compares the body of the
 * response with a given body. It does this for a specified number of 
 * iterations. 
 * 
 * @author mir
 * 
 */
public class BodyCheckerThread extends Thread {

	private Logger logger = LoggerFactory.getLogger(BodyCheckerThread.class);
	private JaxRsHandler handler;
	private Request request;
	private long iterations = 0;
	private byte[] body;
	private boolean failed = true;
	private Exception encounteredException = null;

	public BodyCheckerThread(JaxRsHandler handler, Request request, byte[] body, long iterations) {
		this.handler = handler;
		this.request = request;
		this.body = body;
		this.iterations = iterations;
		
	}

	@Override
	public void run() {
		for(long count = 0; count < iterations; count++){
			ResponseImpl responseImpl;
			try {
				responseImpl = new ResponseImpl();
				handler.handle(request, responseImpl);
				responseImpl.consumeBody();
				
				byte[] requestBody = responseImpl.getBodyBytes();

				logger.debug("{} {}", new String(body), new String(requestBody));

				if (!Arrays.equals(body, requestBody)) {
					failed = true;
					break;
				} else {
					failed = false;
				}
			} catch (HandlerException e) {
				failed = true;
				e.printStackTrace();
				encounteredException = e;
			}
		}
	}

	/**
	 * Return true, if the body of the response is always equals
	 * the specified body. Returns false, if the body was not equal
	 * or in case an exception occurred.
	 * 
	 * @return
	 */
	public boolean hasFailed() {
		return failed;
	}
	
	/**
	 * Returns a catched exception or null.
	 * 
	 * @return
	 */
	public Exception getEncounteredException() {
		return encounteredException;
	}
}