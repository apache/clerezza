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
package org.apache.clerezza.platform.launcher;

import java.io.IOException;

/**
 * Clerezza Application launcher class.
 *
 * @author daniel
 */
public class Main {
	
	public static void main(String... args) throws IOException {
		ClerezzaApp clerezza = new ClerezzaApp();

		try {
			clerezza.start(args);
		} catch (Throwable t) {
			System.err.println("Could not start Clerezza: " + t);
			t.printStackTrace();
			System.exit(clerezza.getExitCode());
		}
		try {
			clerezza.waitForStop();
		} catch (Throwable t) {
			System.err.println("Exception during Clerezza shutdown: " + t);
			t.printStackTrace();
			System.exit(-1);
		}
	}
}
