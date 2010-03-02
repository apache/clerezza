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
package org.apache.clerezza.triaxrs.providers;

class BaseMediaType implements Cloneable {

	static Object WILDCARD_TYPE = new BaseMediaType("*", "*");

	public BaseMediaType(String type, String subtype) {
		super();
		this.subtype = subtype;
		this.type = type;
	}
	private String type,  subtype;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BaseMediaType other = (BaseMediaType) obj;
		if (this.type != other.type && (this.type == null || !this.type.
				equals(other.type))) {
			return false;
		}
		if (this.subtype != other.subtype && (this.subtype == null || !this.subtype.
				equals(other.subtype))) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 61 * hash + (this.type != null ? this.type.hashCode() : 0);
		hash =
				61 * hash + (this.subtype != null ? this.subtype.hashCode() : 0);
		return hash;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new BaseMediaType(type, subtype);
	}


}