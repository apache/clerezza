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
package org.apache.clerezza.rdf.virtuoso.storage;

import org.apache.clerezza.rdf.core.BNode;

public class VirtuosoBNode extends BNode {
	private String skolemId;
	public VirtuosoBNode(String skolemId) {
		this.skolemId = skolemId;
	}
	
	public String getSkolemId(){
		return skolemId;
	}
	
	public String asSkolemIRI(){
		return new StringBuilder().append('<').append(skolemId).append('>').toString();
	}
	
	public String toString(){
		return skolemId;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof VirtuosoBNode) && (obj.toString().equals(toString()));
	}
}
