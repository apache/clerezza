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
package org.apache.clerezza.platform.dashboard;

/**
 * This class keeps information about an item for the global menu.
 *
 * @author mir
 */
public class GlobalMenuItem implements Comparable<GlobalMenuItem> {

	private String path;
	private String label;
	private String description;
	private String identifier;
	private int priority;
	private String groupIdentifier;

	/**
	 * Creates a new <code>GlobalMenuItem</code>.
	 *
	 * @param relativeUri of the resource to be shown in the menu.
	 * @param identifier The identifier of the menu item.
	 * @param label Specifies the label of the menu entry.
	 * @param priority the priority, higher numbers appear first in the menu
	 * @param groupIdentifier Specifies the the identifier of the group of which
	 *		the menu item is part.
	 */
	public GlobalMenuItem(String path, String identifier, String label,
			int priority, String groupIdentifier) {
		if (label == null) {
			throw new IllegalArgumentException("label may not be null");
		}
		this.path = path;
		this.label = label;
		this.identifier = identifier;
		this.priority = priority;
		this.groupIdentifier = groupIdentifier;
	}

	/**
	 * Creates a new <code>GlobalMenuItem</code>.
	 *
	 * @param relativeUri of the resource to be shown in the menu.
	 * @param identifier The identifier of the menu item.
	 * @param label Specifies the label of the menu entry.
	 * @param description The description of the menu item.
	 * @param priority the priority, higher numbers appear first in the menu
	 * @param groupIdentifier Specifies the the identifier of the group of which
	 *		the menu item is part.
	 */
	public GlobalMenuItem(String path, String identifier, String label, String description,
			int priority, String groupIdentifier) {
		if (label == null) {
			throw new IllegalArgumentException("label may not be null");
		}
		this.path = path;
		this.label = label;
		this.description = description;
		this.identifier = identifier;
		this.priority = priority;
		this.groupIdentifier = groupIdentifier;
	}

	/**
	 * Returns the path of the resource to be shown in the menu. The
	 * path is additional to the bundle prefix and if existing to the
	 * <code>javax.ws.rs.Path</code> annotation of the
	 * <code>GlobalMenuItemsProvider</code> that returned this
	 * <code>GlobalMenuItem</code>.
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Returns the label of the menu entry
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Returns the description of the menu entry.
	 * @return the groupIdentifier
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Return the identifier of the menu entry.
	 *
	 * @return
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the the priority within the group, which is used for sorting
	 * the menu Items, higher numbers appear first in the menu
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Returns the the identifier of the group of which this menu item is part,
	 * null if this is a top-level item.
	 * @return the groupIdentifier
	 */
	public String getGroupIdentifier() {
		return groupIdentifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final GlobalMenuItem other = (GlobalMenuItem) obj;
		if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
			return false;
		}
		if (!this.label.equals(other.label)) {
			return false;
		}
		if ((this.description == null) ? (other.description != null) : !this.description.equals(other.description)) {
			return false;
		}
		if (this.priority != other.priority) {
			return false;
		}
		if ((this.groupIdentifier == null) ? (other.groupIdentifier != null) : !this.groupIdentifier.equals(other.groupIdentifier)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 53 * hash + (this.path != null ? this.path.hashCode() : 0);
		hash = 53 * hash + this.label.hashCode();
		hash = 53 * hash + (this.description != null ? this.description.hashCode() : 0);
		hash = 53 * hash + this.priority;
		hash = 53 * hash + (this.groupIdentifier != null ? this.groupIdentifier.hashCode() : 0);
		return hash;
	}

	

	@Override
	public int compareTo(GlobalMenuItem o) {
		if (getPriority() == o.getPriority()) {
			if (this.equals(o)) {
				return 0;
			} else {
				return getLabel().compareTo(o.getLabel());
			}
		}
		return o.getPriority() - getPriority();
	}
}
