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

/**
 *
 * @author reto
 */
class ArtifactUid {
	private String groupId, artifactId;

	public ArtifactUid(String groupId, String artifactId) {
		this.groupId = groupId;
		this.artifactId = artifactId;
	}

	ArtifactUid(String idOfProject) {
		String[] splitted = idOfProject.split(":");
		groupId = splitted[0];
		artifactId = splitted[1];
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getGroupId() {
		return groupId;
	}
	
	@Override
	public String toString() {
		return this.groupId + ":" + this.artifactId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ArtifactUid other = (ArtifactUid) obj;
		if (this.groupId != other.groupId && (this.groupId == null || !this.groupId.equals(other.groupId))) {
			return false;
		}
		if (this.artifactId != other.artifactId && (this.artifactId == null || !this.artifactId.equals(other.artifactId))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 19 * hash + (this.groupId != null ? this.groupId.hashCode() : 0);
		hash = 19 * hash + (this.artifactId != null ? this.artifactId.hashCode() : 0);
		return hash;
	}

	

}
