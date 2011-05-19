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
import java.io.InputStream;
import java.net.URL;
import org.wymiwyg.commons.util.dirbrowser.PathNode;

/**
 * Maven Artifact Description
 * 
 * @author daniel
 */
class MavenArtifactDesc implements Comparable<MavenArtifactDesc> {

	//one of these is null
	URL bundleUrl;
	PathNode pathNode;
	String groupId;
	String artifactId;
	String version;

	/**
	 * Constructor with bundle URL.
	 *
	 * @param groupId	the group id.
	 * @param artifactId	the artifact id.
	 * @param version	the version.
	 * @param bundleUrl	the bundle URL.
	 */
	MavenArtifactDesc(String groupId, String artifactId, String version, URL bundleUrl) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.bundleUrl = bundleUrl;
	}

	/**
	 * Constructor with bundle path.
	 * 
	 * @param groupId	the group id.
	 * @param artifactId	the artifact id.
	 * @param version	the version.
	 * @param pathNode	the bundle path node.
	 */
	MavenArtifactDesc(String groupId, String artifactId, String version, PathNode pathNode) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.pathNode = pathNode;
	}

	/**
	 * Parse Maven Artifact Description from an URL.
	 *
	 * @param bundleUrl the URL.
	 * @return	A new MavenArtifactDesc object.
	 */
	static MavenArtifactDesc parseFromURL(URL bundleUrl) {
		String string = bundleUrl.toString();
		int posSlashM1 = string.lastIndexOf('/');
		int posSlashM2 = string.lastIndexOf('/', posSlashM1 - 1);
		int posSlashM3 = string.lastIndexOf('/', posSlashM2 - 1);
		String version = string.substring(posSlashM2 + 1, posSlashM1);
		String artifactId = string.substring(posSlashM3 + 1, posSlashM2);
		String groupId = getGroupId(string.substring(0, posSlashM3));
		return new MavenArtifactDesc(groupId, artifactId, version, bundleUrl);
	}

	/**
	 * Parse Maven Artifact Description from a path node.
	 *
	 * @param pathNode	The path node.
	 * @return	A new MavenArtifactDesc object.
	 */
	static MavenArtifactDesc parseFromPath(PathNode pathNode) {
		String string = pathNode.getPath();
		int posSlashM1 = string.lastIndexOf('/');
		int posSlashM2 = string.lastIndexOf('/', posSlashM1 - 1);
		int posSlashM3 = string.lastIndexOf('/', posSlashM2 - 1);
		String version = string.substring(posSlashM2 + 1, posSlashM1);
		String artifactId = string.substring(posSlashM3 + 1, posSlashM2);
		String groupId = getGroupId(string.substring(0, posSlashM3));
		return new MavenArtifactDesc(groupId, artifactId, version, pathNode);
	}

	/**
	 * Assembles group-id from the diretories after "bundles/"
	 *
	 * @param string a path-string. Must contain "bundles/".
	 */
	static String getGroupId(String string) {
		int startPos = string.indexOf("bundles/") + 8;
		startPos = string.indexOf('/', startPos) + 1;
		return string.substring(startPos).replace('/', '.');
	}
	
	/**
	 * Returns an URI in the form "mvn:groupId/artifactId/version"
	 *
	 * @return the URI.
	 */
	public String getShortUri() {
		return "mvn:" + groupId + "/" + artifactId;
	}

	/**
	 * Returns a string representation of this object in the form
	 * "mvn:groupId/artifactId/version"
	 *
	 * @return	the string representation of this Maven Artifact Description.
	 */
	@Override
	public String toString() {
		return "mvn:" + groupId + "/" + artifactId + "/" + version;
	}

	InputStream getInputStream() throws IOException {
		if (bundleUrl == null) {
			return pathNode.getInputStream();
		}
		return bundleUrl.openStream();
	}

	@Override
	public int compareTo(MavenArtifactDesc o) {
		return toString().compareTo(o.toString());
	}
}
