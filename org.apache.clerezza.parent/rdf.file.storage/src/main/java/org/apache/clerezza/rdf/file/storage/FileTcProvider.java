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
package org.apache.clerezza.rdf.file.storage;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.service.component.ComponentContext;
import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.Serializer;

/**
 * The <code>FileTcProvider</code> is a <code>WeightedTcProvider</code> that
 * stores <code>MGraph</code>S in the file system. <code>Graph</code>S are not
 * supported.
 * The <code>UriRef</code> of a MGraph is location of the file in the file system
 * (e.g. "file:///home/user/myGraph.rdf"). The format of the rdf data in the file
 * depends on the file ending, for example ".rdf" or ".ttl".
 * <code>FileTcProvider</code> uses
 * <code>org.apache.clerezza.rdf.core.serializedform.Parser</code> and
 * <code>org.apache.clerezza.rdf.core.serializedform.Serializer</code> for
 * parsing and serialization, therefore the supported formats depend on the
 * availability of these services.
 * The default weight of the provider is 300.
 *
 * @scr.component
 * @scr.service interface="org.apache.clerezza.rdf.core.access.WeightedTcProvider"
 * @scr.property name="weight" type="Integer" value="300"
 * 
 * @author mir
 */
public class FileTcProvider implements WeightedTcProvider {

	/**
	 * @scr.reference
	 */
	private Parser parser;
	/**
	 * @scr.reference
	 */
	private Serializer serializer;
	
	private Map<UriRef, FileMGraph> uriRef2MGraphMap =
			new HashMap<UriRef, FileMGraph>();

	protected static File dataFile = new File("data");

	boolean initialized = false;

	private int weight = 300;


	public FileTcProvider() {
		this.parser = Parser.getInstance();
		this.serializer = Serializer.getInstance();
	}

	protected void activate(final ComponentContext componentContext) {
		weight = (Integer) componentContext.getProperties().get("weight");
		dataFile = componentContext.getBundleContext().
						getDataFile("data");
	}

	@Override
	public int getWeight() {
		return weight;
	}

	@Override
	public Graph getGraph(UriRef name) throws NoSuchEntityException {
		throw new NoSuchEntityException(name);
	}

	/**
	 * Get an <code>MGraph</code> by its name. If the file at the specified
	 * location already exists, then a MGraph is returned even though it was not
	 * created with createMGraph().
	 *
	 * @param the name of the <code>MGraph</code>
	 * @return name the <code>MGraph</code> with the specified name
	 * @throws NoSuchEntityException if there is no <code>MGraph</code>
	 *         with the specified name or the file didn't exist.
	 */
	@Override
	public MGraph getMGraph(UriRef name) throws NoSuchEntityException {
		initialize();
		MGraph mGraph = uriRef2MGraphMap.get(name);
		if (mGraph == null) {
			final String uriString = name.getUnicodeString();
			if (!uriString.startsWith("file:")) {
				throw new NoSuchEntityException(name);
			}
			File file = new File(URI.create(uriString));
			if (file.exists()) {
				return createMGraph(name);
			} else {
				throw new NoSuchEntityException(name);
			}			
		}
		return mGraph;
	}

	@Override
	public TripleCollection getTriples(UriRef name) throws NoSuchEntityException {
		return getMGraph(name);
	}

	@Override
	public Set<UriRef> listGraphs() {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public Set<UriRef> listMGraphs() {
		initialize();
		return uriRef2MGraphMap.keySet();
	}

	@Override
	public Set<UriRef> listTripleCollections() {
		return listMGraphs();
	}


	@Override
	public MGraph createMGraph(UriRef name) throws 
			UnsupportedOperationException, EntityAlreadyExistsException {
		initialize();
		if (uriRef2MGraphMap.containsKey(name)) {
			throw new EntityAlreadyExistsException(name);
		}
		FileMGraph mGraph = new FileMGraph(name, parser, serializer);
		uriRef2MGraphMap.put(name, mGraph);
		writeDataFile();
		return mGraph;
	}

	@Override
	public Graph createGraph(UriRef name, TripleCollection triples) throws
			UnsupportedOperationException, EntityAlreadyExistsException {
		throw new UnsupportedOperationException("Not supported.");
	}

	@Override
	public void deleteTripleCollection(UriRef name) throws 
			UnsupportedOperationException, NoSuchEntityException, EntityUndeletableException {
		initialize();
		FileMGraph mGraph = (FileMGraph)getMGraph(name);
		mGraph.delete();
		uriRef2MGraphMap.remove(name);
		writeDataFile();
	}

	@Override
	public Set<UriRef> getNames(Graph graph) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private void initialize() throws RuntimeException {
		if (!initialized) {
			readDataFile();
			initialized = true;
		}
	}

	private void readDataFile() throws RuntimeException {
		try {
			if (dataFile.exists()) {
				FileInputStream fstream = new FileInputStream(dataFile);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				while ((strLine = br.readLine()) != null) {
					UriRef uriRef = new UriRef(strLine);
					uriRef2MGraphMap.put(uriRef, new FileMGraph(uriRef, parser, serializer));
				}
				in.close();
			} else {
				dataFile.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void writeDataFile() {
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(dataFile);
			for (UriRef uri : uriRef2MGraphMap.keySet()) {
				fout.write((uri.getUnicodeString() + "\n").getBytes());
			}
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		finally {
			try {
				fout.close();
			} catch (IOException ex) {
				Logger.getLogger(FileTcProvider.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
