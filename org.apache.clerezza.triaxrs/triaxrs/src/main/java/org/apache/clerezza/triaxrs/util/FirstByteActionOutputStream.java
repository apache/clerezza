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
package org.apache.clerezza.triaxrs.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An outputstream that executes an action when the firts character is writter,
 * if no chracter is written the actin is executed when closing the stream
 * @author reto
 */
//not using filter-ouputstream so only write(b) has to be overriden
public class FirstByteActionOutputStream extends OutputStream {
	private OutputStream base;
	private Runnable action;
	private boolean first = true;

	public FirstByteActionOutputStream(OutputStream base, Runnable action) {
		this.base = base;
		this.action = action;
	}

	@Override
	public void write(int b) throws IOException {
		if (first) {
			action.run();
			first = false;
		}
		base.write(b);
	}

	@Override
	public void close() throws IOException {
		if (first) {
			action.run();
		}
		base.close();
	}

	@Override
	public void flush() throws IOException {
		base.flush();
	}

}
