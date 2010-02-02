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
package org.apache.clerezza.jaxrsreportplugin;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.AbstractBaseJavaEntity;
import com.thoughtworks.qdox.model.Annotation;
import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaSource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.doxia.sink.Sink;
import org.codehaus.doxia.site.renderer.SiteRenderer;

/**
 * Generates a report about jaxrs resources. The report includes java doc descriptions
 * about paths, http methods, comments and parameters of the methods. The report
 * will be generated in the site phase, the default goal is jaxrs-report.
 * 
 * 
 * @goal jaxrs-report
 * 
 * @phase site
 */
public class JaxRsReportMojo extends AbstractMavenReport {

	/**
	 * Path to the root directory
	 *
	 * @parameter expression="${basedir}"
	 */
	private String baseDir;
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
	protected void executeReport(Locale arg0)
			throws MavenReportException {

		JavaClass[] classes = getJavaClassesFromSources();
		boolean pathAnnotationExists = false;
		for (JavaClass clazz : classes) {
			if (hasPathAnnotation(clazz)) {
				pathAnnotationExists = true;
				break;
			}
		}
		Sink sink = getSink();
		sink.head();
		sink.title();
		sink.text("JaxRs Report");
		sink.title_();
		sink.head_();
		sink.body();
		if(pathAnnotationExists) {
			

			for (JavaClass clazz : classes) {
				if (hasPathAnnotation(clazz)) {
					sink.section1();
					sink.sectionTitle1();
					sink.text("Class: " + clazz.getName());
					sink.sectionTitle1_();
					sink.lineBreak();
					sink.text("Package: " + clazz.getPackage().getName());
					sink.lineBreak();
					sink.lineBreak();
					for (Annotation annotation : clazz.getAnnotations()) {
						if (annotation.getParameterValue().toString().contains("javax.ws.rs.Path")) {
							sink.bold();
							sink.text("Root Resource Path: " + annotation.getNamedParameter("value").toString());
							sink.bold_();
						}
					}
					sink.lineBreak();
					sink.lineBreak();
					sink.paragraph();
					sink.text(clazz.getComment());
					sink.paragraph_();
					sink.section1_();
					sink.horizontalRule();
					for (JavaMethod mth : clazz.getMethods()) {
						if (hasPathAnnotation(mth)) {
							for (Annotation annotation : mth.getAnnotations()) {
								if (annotation.getParameterValue().toString().contains("javax.ws.rs.Path")) {
									sink.section2();
									sink.sectionTitle2();
									sink.text("Path: " + annotation.getNamedParameter("value"));
									sink.sectionTitle2_();
								}
							}
							for (Annotation annotation : mth.getAnnotations()) {
								if (annotation.getParameterValue().toString().contains("javax.ws.rs.Produces")) {
									sink.bold();
									sink.text("Produces: " + annotation.getNamedParameter("value"));
									sink.bold_();
									sink.lineBreak();
									sink.lineBreak();
								} else if (annotation.getParameterValue().toString()
										.contains("javax.ws.rs") && !annotation.getParameterValue().toString()
										.contains("javax.ws.rs.Path")) {
									sink.bold();
									sink.text("Http Method: " + annotation.getType()
											.toString().substring("javax.ws.rs.".length()));
									sink.bold_();
									sink.lineBreak();
									sink.lineBreak();
								}
							}
							sink.bold();
							sink.text("Method: " + mth.getName());
							sink.bold_();
							sink.lineBreak();
							sink.lineBreak();
							sink.paragraph();
							sink.text(mth.getComment());
							sink.paragraph_();
							DocletTag[] params = mth.getTagsByName("param");
							if (params != null) {
								sink.numberedList(params.length);
								for (DocletTag param : params) {
									sink.numberedListItem();
									sink.text("param: " + param.getValue());
									sink.numberedListItem_();
								}
								sink.numberedList_();
							}
							DocletTag returns = mth.getTagByName("return");
							if (returns != null) {
								sink.text("Returns " + returns.getValue());
							}
							sink.lineBreak();
							sink.lineBreak();
							sink.paragraph();
							sink.bold();
							sink.text("Method Declaration Signature: ");
							sink.bold_();
							sink.text(mth.getDeclarationSignature(true));
							sink.paragraph_();
							sink.section2_();
						}
					}
					sink.section1_();
				}
			}
		} else {
			sink.section1();
			sink.bold();
			sink.text("This project doesn't expose any JaxRs resources");
			sink.bold_();
			sink.section1_();
		}
		sink.body_();
		sink.flush();
		sink.close();
	}


	@Override
	public String getOutputName() {
		return "jaxrs-report";
	}

	@Override
	public String getName(Locale locale) {
		return "JaxRs Report";
	}

	@Override
	public String getDescription(Locale locale) {
		return "Description of the REST webservices";
	}

	public boolean hasPathAnnotation(AbstractBaseJavaEntity entity) {

		for (Annotation annotation : entity.getAnnotations()) {
			if (annotation.getParameterValue().toString().contains("javax.ws.rs.Path")) {
				return true;
			}

		}
		return false;
	}

	public JavaSource[] getSources() {
		String src = baseDir + File.separator + "src";
		File file = new File(src);
		JavaDocBuilder builder = new JavaDocBuilder();
		builder.addSourceTree(file);
		return builder.getSources();

	}

	private JavaClass[] getJavaClassesFromSources() {
		final JavaSource[] sources = this.getSources();
		final List<JavaClass> classes = new ArrayList<JavaClass>();
		for (int i = 0; i < sources.length; i++) {
			for (int j = 0; j < sources[i].getClasses().length; j++) {
				final JavaClass clazz = sources[i].getClasses()[j];
				classes.add(clazz);
				for (int k = 0; k < clazz.getNestedClasses().length; k++) {
					final JavaClass nestedClass = clazz.getNestedClasses()[k];
					classes.add(nestedClass);
				}
			}
		}
		return classes.toArray(new JavaClass[classes.size()]);
	}
}
