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
package org.apache.clerezza.ontologiesplugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.clerezza.rdf.schemagen.SchemaGen;
import org.apache.clerezza.rdf.schemagen.SchemaGenArguments;

/**
 * Generates java source code out of an ontology described in various RDF
 * formats. The generated java file contains constants for rdf classes and
 * properties described by the ontology.
 *
 *
 * @goal generate
 *
 * @phase generate-sources
 */
public class OntologyJavaClassCreatorMojo extends AbstractMojo {

	/**
	 * Path to the root directory
	 *
	 * @parameter expression="${basedir}"
	 */
	private String baseDir;

	/**
	 * Path to the root dir of the RDF+XML files. Default is
	 * src/main/ontologies.
	 *
	 * @parameter optional
	 */
	private String resourcePath;

	/**
	 * Namespace of ontologies
	 *
	 * @parameter optional
	 */
	private Properties namespaceOfOntology;

	/**
     * Additional source directories.
     *
     * @parameter optional
     */
    private File [] sources;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

	private Map<String, String> supportedFormats = new HashMap<String, String>();

	@Override
	public void execute() throws MojoExecutionException {
		supportedFormats.put(".nt", "text/rdf+nt");
		supportedFormats.put(".n3", "text/rdf+n3");
		supportedFormats.put(".rdf", "application/rdf+xml");
		supportedFormats.put(".ttl", "text/turtle");
		supportedFormats.put(".turtle", "text/turtle");
		File file = null;

		if (resourcePath == null) {
			resourcePath = baseDir + File.separator + "src" + File.separator
					+ "main" + File.separator + "ontologies";
		} else if (resourcePath.contains("/")) {
			resourcePath = resourcePath.replace("/", File.separator);
		} else if (resourcePath.contains("\\")) {
			resourcePath = resourcePath.replace("\\", File.separator);
		}
		file = new File(resourcePath);
		checkDir(file);
		if(sources != null) {
			for ( int i = 0; i < sources.length; ++i ) {
				project.addCompileSourceRoot( this.sources[i].getAbsolutePath() );
				getLog().info( "Source directory: " + this.sources[i] + " added." );
			}
		}
	}

	private void checkDir(File ontologiesDir) {
		for (File file : ontologiesDir.listFiles()) {
			if (file.isDirectory()) {
				checkDir(file);
			} else {
				String fileName = file.getName();
				int indexOfLastDot = fileName.lastIndexOf(".");
				if (indexOfLastDot != -1) {
					String fileEnding = fileName.substring(indexOfLastDot);
					if (supportedFormats.containsKey(fileEnding)) {
						createJavaClassFile(file, fileEnding);
					}
				}
			}
		}
	}

	private void createJavaClassFile(final File file, final String fileEnding) {
		final String fileName = file.getName();
		final String absolutePath = file.getAbsolutePath();
		final String className = fileName.replace(fileEnding, "").toUpperCase();
		final String pathToJavaClass = absolutePath.substring(
				absolutePath.indexOf(resourcePath) + resourcePath.length() + 1)
				.replace(fileName, "");
		final String packageName = pathToJavaClass.replace(File.separator, ".")
				+ className;
		SchemaGenArguments arguments = new SchemaGenArguments() {

			public URL getSchemaUrl() {
				try {
					return file.toURI().toURL();
				} catch (MalformedURLException e) {
					getLog().error(e.getMessage(), e);
				}
				return null;
			}

			public String getNamespace() {
				if (namespaceOfOntology != null
						&& namespaceOfOntology.containsKey(fileName)) {
					return namespaceOfOntology.getProperty(fileName);
				}
				return null;
			}

			public String getFormatIdentifier() {
				return supportedFormats.get(fileEnding);
			}

			public String getClassName() {
				return packageName;
			}
		};

		SchemaGen schemaGen;
		try {
			schemaGen = new SchemaGen(arguments);
		} catch (IOException e) {
			getLog().error(e.getMessage(), e);
			return;
		} catch (URISyntaxException e) {
			getLog().error(e.getMessage(), e);
			return;
		}
		String rootPath = baseDir + File.separator + "target"
				+ File.separator + "generated-sources" + File.separator
				+ "main" + File.separator + "java" + File.separator;
		File dir = new File(rootPath + pathToJavaClass);
		dir.mkdirs();
		PrintWriter out = null;
		try {
			out = new PrintWriter(new File(rootPath + pathToJavaClass + className + ".java"), "utf-8");
		} catch (FileNotFoundException e) {
			getLog().error(e.getMessage(), e);
			return;
		} catch (UnsupportedEncodingException e) {
			getLog().error(e.getMessage(), e);
			throw new RuntimeException("utf-8 not supported!");
		}
		try {
			schemaGen.writeClass(out);
		} finally {
			out.flush();
		}
	}
}
