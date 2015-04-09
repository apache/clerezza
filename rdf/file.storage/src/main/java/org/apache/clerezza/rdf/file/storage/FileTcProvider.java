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
import org.apache.clerezza.commons.rdf.ImmutableGraph;
import org.apache.clerezza.commons.rdf.Graph;
import org.apache.clerezza.commons.rdf.Iri;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.core.serializedform.Parser;
import org.apache.clerezza.rdf.core.serializedform.Serializer;

/**
 * The <code>FileTcProvider</code> is a <code>WeightedTcProvider</code> that
 * stores <code>Graph</code>S in the file system. <code>ImmutableGraph</code>S are not
 * supported.
 * The <code>Iri</code> of a Graph is location of the file in the file system
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
    
    private Map<Iri, FileGraph> uriRef2GraphMap =
            new HashMap<Iri, FileGraph>();

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
    public ImmutableGraph getImmutableGraph(Iri name) throws NoSuchEntityException {
        throw new NoSuchEntityException(name);
    }

    /**
     * Get an <code>Graph</code> by its name. If the file at the specified
     * location already exists, then a Graph is returned even though it was not
     * created with createGraph().
     *
     * @param the name of the <code>Graph</code>
     * @return name the <code>Graph</code> with the specified name
     * @throws NoSuchEntityException if there is no <code>Graph</code>
     *         with the specified name or the file didn't exist.
     */
    @Override
    public Graph getMGraph(Iri name) throws NoSuchEntityException {
        initialize();
        Graph mGraph = uriRef2GraphMap.get(name);
        if (mGraph == null) {
            final String uriString = name.getUnicodeString();
            if (!uriString.startsWith("file:")) {
                throw new NoSuchEntityException(name);
            }
            File file = new File(URI.create(uriString));
            if (file.exists()) {
                return createGraph(name);
            } else {
                throw new NoSuchEntityException(name);
            }            
        }
        return mGraph;
    }

    @Override
    public Graph getGraph(Iri name) throws NoSuchEntityException {
        return getMGraph(name);
    }

    @Override
    public Set<Iri> listImmutableGraphs() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Set<Iri> listMGraphs() {
        initialize();
        return uriRef2GraphMap.keySet();
    }

    @Override
    public Set<Iri> listGraphs() {
        return listMGraphs();
    }


    @Override
    public Graph createGraph(Iri name) throws 
            UnsupportedOperationException, EntityAlreadyExistsException {
        initialize();
        if (uriRef2GraphMap.containsKey(name)) {
            throw new EntityAlreadyExistsException(name);
        }
        FileGraph mGraph = new FileGraph(name, parser, serializer);
        uriRef2GraphMap.put(name, mGraph);
        writeDataFile();
        return mGraph;
    }

    @Override
    public ImmutableGraph createImmutableGraph(Iri name, Graph triples) throws
            UnsupportedOperationException, EntityAlreadyExistsException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void deleteGraph(Iri name) throws 
            UnsupportedOperationException, NoSuchEntityException, EntityUndeletableException {
        initialize();
        FileGraph mGraph = (FileGraph)getGraph(name);
        mGraph.delete();
        uriRef2GraphMap.remove(name);
        writeDataFile();
    }

    @Override
    public Set<Iri> getNames(ImmutableGraph graph) {
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
                    Iri uriRef = new Iri(strLine);
                    uriRef2GraphMap.put(uriRef, new FileGraph(uriRef, parser, serializer));
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
            for (Iri uri : uriRef2GraphMap.keySet()) {
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
