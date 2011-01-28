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
package org.apache.clerezza.platform.content;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import org.apache.clerezza.rdf.core.Literal;
import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TypedLiteral;

import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.ontologies.DISCOBITS;
import org.apache.clerezza.rdf.ontologies.RDF;

/**
 * Represents an InfoDiscoBit
 *
 * @author reto
 */
public class InfoDiscobit {

	private GraphNode infoBit;


	/**
	 *
	 * @param infoBit
	 * @return an instance of InfoDiscobit or null if node is not an InfoDiscoBit
	 */
	public static InfoDiscobit createInstance(GraphNode node) {
		Lock l = node.readLock();
		l.lock();
		try {
			Iterator<Resource> types = node.getObjects(RDF.type);
			while(types.hasNext()) {
				if (types.next().equals(DISCOBITS.InfoDiscoBit)){
					return new InfoDiscobit(node);
				}
			}
			return null;
		} finally {
			l.unlock();
		}
	}

	InfoDiscobit(GraphNode infoBit) {
		this.infoBit = infoBit;
	}

	public String getContentType() {
		Lock readLock = infoBit.readLock();
		readLock.lock();
		try {
			Iterator<Literal> mediaTypeLits = infoBit.getLiterals(DISCOBITS.mediaType);
			if (mediaTypeLits.hasNext()) {
				return mediaTypeLits.next().getLexicalForm();
			}
		} finally {
			readLock.unlock();
		}
		return null;
	}
	
	public byte[] getData() {
		byte[] result = null;
		Lock readLock = infoBit.readLock();
		readLock.lock();
		try {
			Iterator<Literal> mediaTypeLits = infoBit.getLiterals(DISCOBITS.infoBit);
			if (mediaTypeLits.hasNext()) {
				final Literal literalValue = mediaTypeLits.next();
				if (literalValue instanceof TypedLiteral) {
					result = LiteralFactory.getInstance().createObject(
							(new byte[0]).getClass(), (TypedLiteral) literalValue);
				}
			}
		} finally {
			readLock.unlock();
		}		
		return result;	
	};

}
