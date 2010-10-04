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

import org.apache.clerezza.rdf.metadata.MetaDataGenerator;
import java.util.HashSet;
import java.util.Iterator;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import javax.ws.rs.core.MediaType;
import org.apache.clerezza.platform.content.collections.CollectionCreator;

import org.apache.clerezza.rdf.core.LiteralFactory;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.NonLiteral;
import org.apache.clerezza.rdf.core.Triple;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.LockableMGraph;
import org.apache.clerezza.rdf.ontologies.DISCOBITS;
import org.apache.clerezza.rdf.ontologies.RDF;
import org.apache.clerezza.rdf.utils.GraphNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author reto
 */
public abstract class AbstractDiscobitsHandler implements DiscobitsHandler {

	private static final Logger logger = LoggerFactory.getLogger(AbstractDiscobitsHandler.class);

	/**
	 *
	 * @return the MGraph to be used to retrieve and create discobits
	 */
	protected abstract MGraph getMGraph();

	/**
	 * A <code>Set</code> containing <code>MetaDataGenerator</code>s to be used
	 * to add meta data to data putted by the handler.
	 *
	 * @return a Set containing meta data generators
	 */
	protected abstract Set<MetaDataGenerator> getMetaDataGenerators();

	
	@Override
	public void put(UriRef infoDiscoBitUri, MediaType mediaType,
			byte[] data) {

		GraphNode infoDiscoBitNode;
		final LockableMGraph mGraph = (LockableMGraph) getMGraph();
		infoDiscoBitNode = new GraphNode(infoDiscoBitUri, mGraph);
		CollectionCreator collectionCreator = new CollectionCreator(mGraph);
		collectionCreator.createContainingCollections(infoDiscoBitUri);
		Lock writeLock = mGraph.getLock().writeLock();
		writeLock.lock();
		try {
			infoDiscoBitNode.addProperty(RDF.type, DISCOBITS.InfoDiscoBit);
			TypedLiteral dataLiteral = LiteralFactory.getInstance().createTypedLiteral(data);
			infoDiscoBitNode.deleteProperties(DISCOBITS.infoBit);
			infoDiscoBitNode.addProperty(DISCOBITS.infoBit, dataLiteral);
			TypedLiteral mediaTypeLiteral = LiteralFactory.getInstance().createTypedLiteral(mediaType.toString());
			infoDiscoBitNode.deleteProperties(DISCOBITS.mediaType);
			infoDiscoBitNode.addProperty(DISCOBITS.mediaType,mediaTypeLiteral);
		} finally {
			writeLock.unlock();
		}
		Set<MetaDataGenerator> metaDataGenerators = getMetaDataGenerators();
		synchronized(metaDataGenerators) {
			for(MetaDataGenerator generator : metaDataGenerators) {
				try {
					generator.generate(infoDiscoBitNode, data, mediaType);
				} catch (RuntimeException ex) {
					logger.error("Exception in MetaDataGenerator ", ex);
				}
			}
		}
	}

	@Override
	public  void remove(NonLiteral node) {
		MGraph mGraph = getMGraph();		
		Iterator<Triple> properties = mGraph.filter(node, null, null);
		//copying properties to set, as we're modifying underlying graph
		Set<Triple> propertiesSet = new HashSet<Triple>();
		while (properties.hasNext()) {
			propertiesSet.add(properties.next());
		}
		properties = propertiesSet.iterator();
		while (properties.hasNext()) {
			Triple triple = properties.next();
			UriRef predicate = triple.getPredicate();
			if (predicate.equals(DISCOBITS.contains)) {
				try {
					GraphNode containedNode = new GraphNode((NonLiteral)triple.getObject(), mGraph);
					//The following includes triple
					containedNode.deleteNodeContext();
				} catch (ClassCastException e) {
					throw new RuntimeException("The value of "+predicate+" is expected not to be a literal");
				}
				//as some other properties of node could have been in the context of the object
				remove(node);
				return;
			}			
		}
		GraphNode graphNode = new GraphNode(node, mGraph);
		graphNode.deleteNodeContext();
	}

	@Override
	public byte[] getData(UriRef uriRef) {
		MGraph mGraph = getMGraph();
		GraphNode node = new GraphNode(uriRef, mGraph);
		final InfoDiscobit infoDiscobit = InfoDiscobit.createInstance(node);
		if (infoDiscobit == null) {
			return null;
		}
		return infoDiscobit.getData();
	}

	@Override
	public MediaType getMediaType(UriRef uriRef) {
		MGraph mGraph = getMGraph();
		GraphNode node = new GraphNode(uriRef, mGraph);
		final InfoDiscobit infoDiscobit = InfoDiscobit.createInstance(node);
		if (infoDiscobit == null) {
			return null;
		}
		return MediaType.valueOf(infoDiscobit.getContentType());
	}
}
