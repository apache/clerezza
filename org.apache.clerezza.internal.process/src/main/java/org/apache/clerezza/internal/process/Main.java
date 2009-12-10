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
package org.apache.clerezza.internal.process;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * jar entry point, delegating to other classes with a main method mapped to a
 * command this App takes a first argument, subsequent arguments are forwarded
 * to the invoked class.
 *
 * @author mir
 */
public class Main {

	private final static Map<String, Class> commandMap = new HashMap<String, Class>();

	static {
		commandMap.put("compile", MultiCompile.class);
		commandMap.put("verify", CheckDepManConsistency.class);
		commandMap.put("desnapshotize", DesnapshotizeProjects.class);
		commandMap.put("order", DependencyOrder.class);
		commandMap.put("experiment", IndexCli.class);
		commandMap.put("snapshotize", SnapshotizeProject.class);
	}

	public static void main(String... args) throws Exception {
		if (args.length < 1 ) {
			printUsage();
			System.exit(1);
		}

		String command = args[0];
		args = Arrays.copyOfRange(args, 1, args.length);
		final Class commandClass = commandMap.get(command);

		if (commandClass == null) {
			printUsage();
			System.exit(1);
		}
		System.out.println("invoking "+commandClass);
		commandClass.getMethod("main", args.getClass()).invoke(null, (Object)args);

	}

	private static void printUsage() {
		System.out.println("Usage: ");
		System.out.print("java -jar <Jar-File> ");
		for (String command : commandMap.keySet()) {
			System.out.print(command);
			System.out.print(' ');
		}
		System.out.println(" <arguments>");
	}
}
