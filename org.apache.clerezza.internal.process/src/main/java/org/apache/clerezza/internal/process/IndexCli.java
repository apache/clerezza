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
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.maven.Maven;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.Profile;
import org.apache.maven.model.Repository;
import org.apache.maven.monitor.event.DefaultEventDispatcher;
import org.apache.maven.profiles.DefaultProfileManager;
import org.apache.maven.profiles.ProfileManager;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.plexus.embed.Embedder;

import org.apache.maven.project.DefaultMavenProjectBuilder;
import org.apache.maven.project.DefaultMavenProjectHelper;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.ProjectBuilderConfiguration;
import org.apache.maven.settings.DefaultMavenSettingsBuilder;
import org.apache.maven.settings.MavenSettingsBuilder;
import org.apache.maven.settings.Settings;

/**
 * Entry point for indexing CLI.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class IndexCli {

	public static void main(String[] args)
			throws Exception {
		File pomFile = new File(args[0]);
		Embedder embedder = new Embedder();
		embedder.start(new ClassWorld());


		long time = System.currentTimeMillis();

		ArtifactRepositoryFactory factory =
				(ArtifactRepositoryFactory) embedder.lookup(ArtifactRepositoryFactory.ROLE);

		ArtifactRepositoryLayout layout =
				(ArtifactRepositoryLayout) embedder.lookup(ArtifactRepositoryLayout.ROLE, "legacy");
		DefaultMavenProjectBuilder mpb = (DefaultMavenProjectBuilder) embedder.lookup( MavenProjectBuilder.ROLE );


		//MavenProject pom1 = projectBuilder.build(pomFile, config)

		MavenSettingsBuilder msb = new DefaultMavenSettingsBuilder();
		Settings settings = msb.buildSettings(new File("/home/rbn/.m2/settings.xml"));
		//Settings settings = msb.buildSettings();



		

		Profile profile = new Profile();

        profile.setId( "test-profile" );

		ArtifactRepository localRepository = new DefaultArtifactRepository(
				"local", new File(settings.getLocalRepository()).toURI().toString(),
				new DefaultRepositoryLayout());

		System.out.println("local repo: "+settings.getLocalRepository());
		System.out.println("active profile: "+settings.getActiveProfiles());
		System.out.println("all profile: "+settings.getProfiles());
		//settings.setLocalRepository(localRepository);

        Repository repo = new Repository();
        repo.setId( "test" );
        repo.setUrl("http://repo.example.org/maven2");

        profile.addRepository( repo );


        ProfileManager pm = new DefaultProfileManager( embedder.getContainer(), settings, new Properties() );
		System.out.println("active profile: "+pm.getActiveProfiles());
		System.out.println(" profiles: "+pm.getProfilesById());

		MavenProject pom = mpb.build(pomFile, localRepository, pm);

		

		time = System.currentTimeMillis() - time;

		System.out.println("created  " + pom + " in " + time + "ms");
		for (Object dep : pom.getDependencies()) {
			System.out.println(dep);
		}

		Properties executionProperties = new Properties();
		Properties userProperties = new Properties();
		List<String> goals = Collections.singletonList("install");
		Maven maven = (Maven) embedder.lookup( Maven.ROLE );
		System.out.println("execution base: "+pomFile.getParent());
		MavenExecutionRequest request = new DefaultMavenExecutionRequest(localRepository,
				settings, new DefaultEventDispatcher(), goals,
				pomFile.getParent(), pm, executionProperties, userProperties, true);
		maven.execute(request);

	}
}

