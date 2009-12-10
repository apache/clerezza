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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.Maven;
import org.apache.maven.model.Dependency;
import org.apache.maven.reactor.MavenExecutionException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.embed.Embedder;
import org.codehaus.plexus.util.dag.DAG;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * A utility to compile a bunch of projects in the right order.
 * 
 * @author reto, tio
 */
public class MultiCompile {

	/**
	 *
	 * @param args a list of project-folders to compile
	 * @throws java.lang.Exception
	 */
	public static void main(String[] args) throws Exception {

		final File[] directories = new File[args.length];
		for (int i = 0; i < args.length; i++) {
			directories[i] = new File(args[i]);

		}
		for (DependencyOrder.ProcessedDepenedency dep : DependencyOrder.getTopologicalOrder(directories)) {
			if (!dep.isCompiled()) {
				compile(dep.getDirectory());
			}
		}
	}

	static void compile(File projectDir) throws IOException, MavenExecutionException, XmlPullParserException {
		File pomFile = new File(projectDir, "pom.xml");
		ProcessBuilder processBuilder = new ProcessBuilder("mvn","install");
		processBuilder.directory(projectDir);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		InputStream processOut = process.getInputStream();
		for (int ch = processOut.read(); ch != -1; ch = processOut.read()) {
			System.out.write(ch);
		}
		try {
			process.waitFor();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
		final int exitValue = process.exitValue();
		if (exitValue != 0) {
			System.out.println("External Maven terminated abnormaly: "+exitValue);
			System.in.read();
			throw new RuntimeException("External Maven terminated abnormaly: "+exitValue);
		}
	}

}
