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
package org.apache.clerezza.pladocplugin;

import java.util.Locale;

import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.doxia.site.renderer.SiteRenderer;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import java.util.Set;
import java.util.logging.Handler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.StringMap;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.logging.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.apache.clerezza.pladocplugin.api.*;

import org.apache.maven.plugin.MojoExecution;
import org.codehaus.plexus.component.repository.ComponentDependency;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Generates java source code out of an ontology described in various RDF
 * formats. The generated java file contains constants for rdf classes and
 * properties described by the ontology.
 *
 *
 * @goal pladoc-report
 *
 * @phase site
 */
public class PladocReportMojo extends AbstractMavenReport {


	/**
	 * Location where generated html will be created.
	 *
	 * @parameter expression="${project.reporting.outputDirectory}"
	 */
	private String outputDirectory;
	/**
	 * Doxia Site Renderer
	 *
	 * @parameter expression="${component.org.codehaus.doxia.site.renderer.SiteRenderer}"
	 * @required @readonly
	 */
	private SiteRenderer siteRenderer;
	/**
	 * Maven Project
	 *
	 * @parameter expression="${project}"
	 * @required @readonly
	 */
	private MavenProject project;

	/**
	 * Path to the root directory
	 *
	 * @parameter expression="${basedir}"
	 */
	private String baseDirName;
	/**
	 * Path to the root directory
	 *
	 * @parameter expression="${mojoExecution}"
	 */
	private MojoExecution mojoExecution;
	private Log log;
	private String RELATIVE_PLADOC_DIR = "pladoc/";

	@Override
	protected void executeReport(Locale arg0) throws MavenReportException {
		try {
			Map configMap = new StringMap(false);
			// Configure the Felix instance to be embedded.
			configMap.put("org.osgi.framework.system.packages.extra",
					"org.apache.clerezza.pladocplugin.api");
			File tempFile = File.createTempFile("felix", "cache");
			tempFile.delete();
			tempFile.mkdir();
			tempFile.deleteOnExit();
			configMap.put("org.osgi.framework.storage",
					tempFile.getAbsolutePath());
			Felix felix = new Felix(configMap);

			felix.start();
			final BundleContext bundleContext = felix.getBundleContext();

			Set<Bundle> installedBundles = new HashSet<Bundle>();
			for (Object dep : mojoExecution.getMojoDescriptor().getPluginDescriptor().getIntroducedDependencyArtifacts()) {
				Artifact artifact = (Artifact) dep;
				log.info("Dep-artifact " + artifact + " scope " + artifact.getScope());
			}
			for (Object dep : mojoExecution.getMojoDescriptor().getPluginDescriptor().getDependencies()) {
				log.info("Dependency " + dep + " class " + dep.getClass());
				ComponentDependency dependency = (ComponentDependency) dep;
				log.info("type " + dependency.getType());
			}
			log.info("components");
			for (Object dep : mojoExecution.getMojoDescriptor().getPluginDescriptor().getArtifacts()) {
				Artifact artifact = (Artifact) dep;
				log.info("Articatf " + artifact + " scope " + artifact.getScope());
				if (artifact.getType().equals("jar")) {
					try {
						String fileUriBase = "file://";
						if (File.separator.equals("\\")) {
							fileUriBase = "file:///";
						}
						Bundle bundle = bundleContext.installBundle(fileUriBase + artifact.getFile().getAbsolutePath());
						installedBundles.add(bundle);
						if (artifact.getFile().getAbsolutePath().indexOf("triax") > 0) {
							bundle.start();
						}
						log.debug("Installed: " + bundle);
					} catch (Exception e) {
						log.debug("Exception installing " + artifact.getFile().getAbsolutePath());
					}
				}
			}
			for (Bundle bundle : installedBundles) {
				try {
					log.debug("Trying to activate: " + bundle);
					bundle.start();
				} catch (Exception e) {
					log.warn("Exception activating " + bundle + ": " + e);
				}
			}

			log.info("Framework activates with following bundles:");
			for (Bundle bundle : bundleContext.getBundles()) {
				log.info(bundle.getLocation() + ": " + getBundleStateDescription(bundle.getState()));
			}
			ServiceTracker tracker1 = new ServiceTracker(bundleContext,
					Handler.class.getName(), null);
			tracker1.open();
			System.out.println("Handler: " + tracker1.waitForService(15000));
			ServiceTracker tracker = new ServiceTracker(bundleContext,
					GeneratorService.class.getName(), null);
			tracker.open();
			Object rendererFactoryObj = tracker.waitForService(15000);

			log.debug(GeneratorService.class + " service : " + rendererFactoryObj);
			log.debug(GeneratorService.class + " service : " + rendererFactoryObj.getClass());
			GeneratorService generatorService = (GeneratorService) rendererFactoryObj;
			final File baseDir = new File(baseDirName);
			File documentationFile = new File(baseDir,
					"src/main/resources/META-INF/documentation.nt");
			log.debug("using documentation file: " + documentationFile);
			File targetDir;
			if (documentationFile.exists()) {
				targetDir = new File(baseDir, "target/site/" + RELATIVE_PLADOC_DIR);
				targetDir.mkdirs();
				generatorService.process(documentationFile, targetDir);
			} else {
				log.warn("IMPORTANT: No documentation found in this module!");
				return;
			}

			Sink sink = getSink();
			sink.head();
			sink.title();
			sink.text("Pladoc Documentation");
			sink.title_();
			sink.head_();
			sink.body();
			sink.section1();
			sink.sectionTitle1();
			sink.text("Pladoc Documentation");
			sink.sectionTitle1_();
			sink.lineBreak();
			sink.lineBreak();
			sink.text("The Pladoc documentation comprises: ");
			sink.lineBreak();
			for (String fileName : targetDir.list()) {
				sink.link(RELATIVE_PLADOC_DIR + fileName);
				sink.text(fileName);
				sink.link_();
				sink.lineBreak();
			}

			sink.section1_();
			sink.body_();
			sink.flush();
			sink.close();
		} catch (Exception ex) {
			throw new MavenReportException("Exception creating PlaDoc ", ex);
		}
	}

	@Override
	public void setLog(Log log) {
		this.log = log;
		super.setLog(log);
	}

	private String getBundleStateDescription(int state) {
		switch (state) {
			case 2:
				return "Installed";
			case 4:
				return "Resolved";
			case 8:
				return "Starting";
			case 16:
				return "Stopping";
			case 32:
				return "Active";
		}
		return "Unkown";
	}

	@Override
	protected SiteRenderer getSiteRenderer() {
		return siteRenderer;
	}

	@Override
	protected String getOutputDirectory() {
		return outputDirectory;

	}

	@Override
	protected MavenProject getProject() {
		return project;
	}

	@Override
	public String getOutputName() {
		return "pladoc-report";
	}

	@Override
	public String getName(Locale locale) {
		return "PlaDoc Report";
	}

	@Override
	public String getDescription(Locale locale) {
		return "Platform documentation";
	}
}
