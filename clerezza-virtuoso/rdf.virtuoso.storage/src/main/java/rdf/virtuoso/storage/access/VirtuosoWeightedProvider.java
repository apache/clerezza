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
package rdf.virtuoso.storage.access;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rdf.virtuoso.storage.VirtuosoMGraph;
import virtuoso.jdbc4.VirtuosoConnection;
import virtuoso.jdbc4.VirtuosoException;
import virtuoso.jdbc4.VirtuosoResultSet;
import virtuoso.jdbc4.VirtuosoStatement;

/**
 * A {@link org.apache.clerezza.rdf.core.access.WeightedTcProvider} for Virtuoso.
 * 
 * @scr.component metatype="true" immediate="true"
 * @scr.services interface="org.apache.clerezza.rdf.core.access.WeightedTcProvider"
 * @scr.property name="weight" type="Integer" value="110"
 * @scr.property name="host" type="String" value="localhost"
 * @scr.property name="port" type="Integer" value="1111"
 * @scr.property name="user" type="String" value="dba"
 * @scr.property name="password" type="String" value="dba"
 * 
 * @author enridaga
 * 
 */
@Component(metatype=true, immediate=true)
@Service(WeightedTcProvider.class)
public class VirtuosoWeightedProvider implements WeightedTcProvider {
	@Property(value="localhost", description="The host running the Virtuoso server")
	public static final String HOST = "host";
	@Property(intValue=1111, description="The port number")
	public static final String PORT= "port";
	@Property(value="dba", description="User name")
	public static final String USER = "user";
	@Property(value="dba", description="User password")
	public static final String PASSWORD = "password";
	
	/**
	 * Virtuoso JDBC Driver class
	 */
	public static final String DRIVER = "virtuoso.jdbc4.Driver";
	
	public static final int DEFAULT_WEIGHT = 110;
	
	@Property(intValue=DEFAULT_WEIGHT, description="Weight assigned to this provider")
	public static final String WEIGHT = "weight";
	
	/**
	 * MAP OF LOADED GRAPHS
	 */
	private Map<UriRef, VirtuosoMGraph> graphs = new HashMap<UriRef, VirtuosoMGraph>();

	/**
	 * Weight
	 */
	private int weight = DEFAULT_WEIGHT;

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory
			.getLogger(VirtuosoWeightedProvider.class);
	private String host;
	private Integer port;
	private String user;
	private String pwd;
	private String connStr;

	/**
	 * Creates a new {@link VirtuosoWeightedProvider}.
	 * 
	 * Before the weighted provider can be used, the method
	 * <code>activate</code> has to be called.
	 */
	public VirtuosoWeightedProvider() {
		logger.debug("Created VirtuosoWeightedProvider.");
	}

	public VirtuosoWeightedProvider(String jdbcConnectionString,
			String jdbcUser, String jdbcPassword) {
		connStr = jdbcConnectionString;
		user = jdbcUser;
		pwd = jdbcPassword;
	}

	/**
	 * Activates this component.<br />
	 * 
	 * @param cCtx
	 *            Execution context of this component. A value of null is
	 *            acceptable when you set the property connection
	 * @throws ConfigurationException
	 * @throws IllegalArgumentException
	 *             No component context given and connection was not set.
	 */
	@Activate
	public void activate(ComponentContext cCtx) {
		logger.info("activate(ComponentContext {})", cCtx);
		logger.info("Activating VirtuosoWeightedProvider...");

		if (cCtx == null ) {
				logger.error("No component context given and connection was not set");
			throw new IllegalArgumentException(
					"No component context given and connection was not set");
		} else if (cCtx != null) {
			logger.debug("Context is given: {}", cCtx);
			String pid = (String) cCtx.getProperties().get(
					Constants.SERVICE_PID);
			try { 
				// FIXME The following should not be needed...
				try {
					this.weight = (Integer) cCtx.getProperties().get(WEIGHT);
				} catch (NumberFormatException nfe) {
					logger.warn(nfe.toString());
					logger.warn("Setting weight to defaults");
					this.weight = DEFAULT_WEIGHT;
				}

				/**
				 * Retrieve connection properties
				 */
				 host = (String) cCtx.getProperties().get(HOST);
				 port = (Integer) cCtx.getProperties().get(PORT);
				 user = (String) cCtx.getProperties().get(USER);
				 pwd = (String) cCtx.getProperties().get(PASSWORD);

				// Build connection string
				 connStr = new StringBuilder().append("jdbc:virtuoso://")
						.append(host).append(":").append(port).append("/CHARSET=UTF-8").toString();
				
				// Check connection
				VirtuosoConnection connection = getConnection(connStr, user, pwd);
				
				// Debug activation
				if (logger.isDebugEnabled()) {
					logger.debug("Component context properties: ");
					logger.debug("> host: {}", host);
					logger.debug("> port: {}", port);
					logger.debug("> user: {}", user);
					// We hide the password in log files:
					MessageDigest algorithm;
					try {
						algorithm = MessageDigest.getInstance("MD5");
					} catch (NoSuchAlgorithmException e) {
						throw new RuntimeException(e);
					}
					algorithm.reset();
					algorithm.update(pwd.getBytes());
					byte messageDigest[] = algorithm.digest();

					StringBuffer hexString = new StringBuffer();
					for (int i = 0; i < messageDigest.length; i++) {
						hexString.append(Integer
								.toHexString(0xFF & messageDigest[i]));
					}
					String foo = messageDigest.toString();
					logger.debug("> password: {}", foo);
				}
				logger.info("Connection to {} initialized. User is {}",
						connStr, user);
				
				// everything went ok
				connection.close();
			} catch (VirtuosoException e) {
				logger.error(
						"A problem occurred while intializing connection to Virtuoso",
						e);
				logger.error("Be sure you have configured the connection parameters correctly in the OSGi/SCR configuration");
				cCtx.disableComponent(pid);
				throw new ComponentException(e.getLocalizedMessage());
			} catch (SQLException e) {
				logger.error(
						"A problem occurred while intializing connection to Virtuoso",
						e);
				logger.error("Be sure you have configured the connection parameters correctly in the OSGi/SCR configuration");
				cCtx.disableComponent(pid);
				throw new ComponentException(e.getLocalizedMessage());
			} catch (ClassNotFoundException e) {
				logger.error(
						"A problem occurred while intializing connection to Virtuoso",
						e);
				logger.error("Be sure you have configured the connection parameters correctly in the OSGi/SCR configuration");
				cCtx.disableComponent(pid);
				throw new ComponentException(e.getLocalizedMessage());
			}
		}
		logger.info("Activated VirtuosoWeightedProvider.");
	}

	/**
	 * Deactivates this component.
	 * 
	 * @param cCtx
	 *            component context provided by OSGi
	 */
	@Deactivate
	public void deactivate(ComponentContext cCtx) {
		logger.debug("deactivate(ComponentContext {})", cCtx);
		// XXX Anything to do here?
		logger.info("Shutdown complete.");
	}
	
	public VirtuosoConnection getConnection() throws SQLException, ClassNotFoundException{
		return getConnection(connStr, user, pwd);
	}
	private VirtuosoConnection getConnection(String connStr, String user, String pwd)
 throws SQLException, ClassNotFoundException {
		logger.debug("getConnection(String {}, String {}, String *******)",
				connStr, user);
		/**
		 * FIXME For some reasons, it looks the DriverManager is instantiating a
		 * new virtuoso.jdbc4.Driver instance upon any activation. (Enable debug
		 * to see this in the stderr stream)
		 */
		logger.debug("Loading JDBC Driver");
		Class.forName(VirtuosoWeightedProvider.DRIVER, true, this.getClass()
				.getClassLoader());
		if (logger.isDebugEnabled()) {
			logger.debug("Activating logging for DriverManager in stderr");
			// FIXME! How to redirect logging to our logger???
			DriverManager.setLogWriter(new PrintWriter(System.err));
		}
		VirtuosoConnection c = (VirtuosoConnection) DriverManager.getConnection(connStr, user,
				pwd);
		c.setAutoCommit(true);
		return c;
	}

	/**
	 * Retrieves the Graph (unmodifiable) with the given UriRef If no graph
	 * exists with such name, throws a NoSuchEntityException
	 */
	@Override
	public Graph getGraph(UriRef name) throws NoSuchEntityException {
		logger.debug("getGraph(UriRef {}) ", name);
		// If it is read-only, returns the Graph
		// If it is not read-only, returns the getGraph() version of the MGraph
		VirtuosoMGraph g = loadGraphOnce(name);
		if (g instanceof Graph) {
			return (Graph) g;
		} else {
			return g.getGraph();
		}
	}

	/**
	 * Retrieves the MGraph (modifiable) with the given UriRef. If no graph
	 * exists with such name, throws a NoSuchEntityException.
	 * 
	 * @return mgraph
	 */
	@Override
	public MGraph getMGraph(UriRef name) throws NoSuchEntityException {
		logger.debug("getMGraph(UriRef {}) ", name);
		VirtuosoMGraph g = loadGraphOnce(name);
		if (g instanceof Graph) {
			// We have this graph but only in read-only mode!
			throw new NoSuchEntityException(name);
		}
		return g;
	}

	/**
	 * Load the graph once. It check whether a graph object have been alrady
	 * created for that UriRef, if yes returns it.
	 * 
	 * If not check if at least 1 triple is present in the quad for such graph
	 * identifier. If yes, creates a new graph object and loads it in the map,
	 * referring to it on next calls.
	 * 
	 * If no triples exists, the graph does not exists or it is not readable.
	 * 
	 * 
	 * @param name
	 * @return
	 */
	private VirtuosoMGraph loadGraphOnce(UriRef name) {
		logger.debug("loadGraphOnce({})", name);
		
		// Check whether the graph have been already loaded once
		if (graphs.containsKey(name)) {
			logger.debug("{} is already loaded", name);
			return graphs.get(name);
		}else{
			VirtuosoMGraph graph = null;
			logger.debug("Attempt to load {}", name);
			// Let's create the graph object
			String SQL = "SPARQL SELECT ?G WHERE { GRAPH ?G {?A ?B ?C} . FILTER(?G = "
					+ name + ")} LIMIT 1";
	
			Statement st = null;
			VirtuosoResultSet rs = null;
			VirtuosoConnection connection = null;
			Exception e = null;
			try {
				connection = getConnection(connStr, user, pwd);
				st = connection.createStatement();
				logger.debug("Executing SQL: {}", SQL);
				st.execute(SQL);
				rs = (VirtuosoResultSet) st.getResultSet();
				if (rs.next() == false) {
					// The graph is empty, it is not readable or does not exists
					logger.warn("Graph does not exists: {}", name);
					throw new NoSuchEntityException(name);
				} else {
					// The graph exists and it is readable ...
					logger.debug("Graph {} is readable", name);
					// is it writable?
					logger.debug("Is {} writable?", name);
					if (canModify(name)) {
						logger.debug("Creating writable MGraph for graph {}", name);
						graphs.put(name, new VirtuosoMGraph(
								name.getUnicodeString(), this));
					} else {
						logger.debug("Creating read-only Graph for graph {}", name);
						graphs.put(name, new VirtuosoMGraph(
								name.getUnicodeString(), this)
								.asVirtuosoGraph());
					}
					graph = graphs.get(name);
				}
				
			} catch (VirtuosoException ve) {
				logger.error("Error while executing query/connection.", ve);
				e = ve;
			} catch (SQLException se) {
				logger.error("Error while executing query/connection.", se);
				e = se;
			} catch (ClassNotFoundException ce) {
				logger.error("Error while executing query/connection.", ce);
				e = ce;
			}finally{
				try { if (rs != null) rs.close(); } catch (Exception ex) {};
			    try { if (st != null) st.close(); } catch (Exception ex) {};
				if(connection!=null){
					try {
						connection.close();
					} catch (VirtuosoException e1) {
						logger.error("Cannot close connection", e1);
					}
				}
			}
			if(e!=null){
				throw new RuntimeException(e);
			}
			return graph;
		}
		
	}

	/**
	 * Generic implementation of the get(M)Graph method. If the named graph is
	 * modifiable, behaves the same as getMGraph(UriRef name), elsewhere,
	 * behaves as getGraph(UriRef name)
	 */
	@Override
	public TripleCollection getTriples(UriRef name)
			throws NoSuchEntityException {
		logger.debug("getTriples(UriRef {}) ", name);
		return loadGraphOnce(name);
	}

	/**
	 * Returns the list of graphs in the virtuoso quad store. The returned set
	 * is unmodifiable.
	 * 
	 * @return graphs
	 */
	@Override
	public Set<UriRef> listGraphs() {
		logger.debug("listGraphs()");
		Set<UriRef> graphs = new HashSet<UriRef>();
		// XXX Add the active (possibly empty) mgraphs
		graphs.addAll(this.graphs.keySet());
		// Returns the list of graphs in the virtuoso quad store
		String SQL = "SPARQL SELECT DISTINCT ?G WHERE {GRAPH ?G {?S ?P ?O} }";
		VirtuosoConnection connection = null;
		Exception e = null;
		VirtuosoStatement st = null;
		VirtuosoResultSet rs = null;
		try {
			connection = getConnection();
			st = (VirtuosoStatement) connection
					.createStatement();
			logger.debug("Executing SQL: {}", SQL);
			rs = (VirtuosoResultSet) st.executeQuery(SQL);
			while (rs.next()) {
				UriRef graph = new UriRef(rs.getString(1));
				logger.debug(" > Graph {}", graph);
				graphs.add(graph);
			}
		} catch (VirtuosoException e1) {
			logger.error("Error while executing query/connection.", e1);
			e = e1;
		} catch (SQLException e1) {
			logger.error("Error while executing query/connection.", e1);
			e = e1;
		} catch (ClassNotFoundException e1) {
			logger.error("Error while executing query/connection.", e1);
			e = e1;
		}finally{

			try { if (rs != null) rs.close(); } catch (Exception ex) {};
		    try { if (st != null) st.close(); } catch (Exception ex) {};
			if(connection!=null){
				try {
					connection.close();
				} catch (VirtuosoException e1) {
					logger.error("Cannot close connection", e1);
				}
			}
		}
		if(e!=null){
			throw new RuntimeException(e);
		}
		return Collections.unmodifiableSet(graphs);
	}

	@Override
	public Set<UriRef> listMGraphs() {
		logger.debug("listMGraphs()");
		Set<UriRef> graphs = listGraphs();
		Set<UriRef> mgraphs = new HashSet<UriRef>();
		logger.debug("Modifiable graphs:");
		for (UriRef u : graphs) {
			if (canModify(u)) {
				logger.debug(" > {}", u);
				mgraphs.add(u);
			}
		}
		return Collections.unmodifiableSet(mgraphs);
	}

	private long getPermissions(String graph) {
		VirtuosoConnection connection = null;
		ResultSet rs = null;
		Statement st = null;
		logger.debug("getPermissions(String {})", graph);
		Exception e = null;
		Long result = null;
		try {
			connection = getConnection();
			String sql = "SELECT DB.DBA.RDF_GRAPH_USER_PERMS_GET ('" + graph
					+ "','" + connection.getMetaData().getUserName() + "') ";
			logger.debug("Executing SQL: {}", sql);
			 st = connection.createStatement();
			st.execute(sql);
			rs = st.getResultSet();
			rs.next();
			result = rs.getLong(1);
			logger.debug("Permission: {}", result);
		} catch (VirtuosoException ve) {
			logger.error("A virtuoso SQL exception occurred.");
			e = ve;
		} catch (SQLException se) {
			logger.error("An SQL exception occurred.");
			e = se;
		} catch (ClassNotFoundException e1) {
			logger.error("An ClassNotFoundException occurred.");
			e = e1;
		} finally {
			try { if (rs != null) rs.close(); } catch (Exception ex) {};
		    try { if (st != null) st.close(); } catch (Exception ex) {};
			if(connection!=null){
				try {
					connection.close();
				} catch (VirtuosoException e1) {
					logger.error("Cannot close connection", e1);
				}
			}
		}
		if (e != null) {
			throw new RuntimeException(e);
		}
		return result;
	}

	public boolean canRead(UriRef graph) {
		logger.debug("canRead(UriRef {})", graph);
		return (isRead(getPermissions(graph.getUnicodeString())));
	}

	public boolean canModify(UriRef graph) {
		logger.debug("canModify(UriRef {})", graph);
		return (isWrite(getPermissions(graph.getUnicodeString())));
	}

	private boolean testPermission(long value, int bit) {
		logger.debug("testPermission(long {},int {})", value, bit);
		return BigInteger.valueOf(value).testBit(bit);
	}

	private boolean isRead(long permission) {
		logger.debug("isRead(long {})", permission);
		return testPermission(permission, 1);
	}

	private boolean isWrite(long permission) {
		logger.debug("isWrite(long {})", permission);
		return testPermission(permission, 2);
	}

	@Override
	public Set<UriRef> listTripleCollections() {
		logger.debug("listTripleCollections()");
		// I think this should behave the same as listGraphs() in our case.
		return listGraphs();
	}

	private VirtuosoMGraph createVirtuosoMGraph(UriRef name)
			throws UnsupportedOperationException, EntityAlreadyExistsException {
		logger.debug("createVirtuosoMGraph(UriRef {})", name);
		// If the graph already exists, we throw an exception
		try {
			loadGraphOnce(name);
			throw new EntityAlreadyExistsException(name);
		} catch (NoSuchEntityException nsee) {
			if (canModify(name)) {
				graphs.put(name, new VirtuosoMGraph(name.getUnicodeString(),
							this));
				return graphs.get(name);
			} else {
				logger.error("Cannot create MGraph {}", name);
				throw new UnsupportedOperationException();
			}
		}
	}

	/**
	 * Creates an initially empty MGraph. If the name already exists in the
	 * store, throws an {@see EntityAlreadyExistsException}
	 */
	@Override
	public MGraph createMGraph(UriRef name)
			throws UnsupportedOperationException, EntityAlreadyExistsException {
		logger.debug("createMGraph(UriRef {})", name);
		return createVirtuosoMGraph(name);
	}

	/**
	 * Creates a new graph with the given triples, then returns the readable
	 * (not modifiable) version of the graph
	 * 
	 */
	@Override
	public Graph createGraph(UriRef name, TripleCollection triples)
			throws UnsupportedOperationException, EntityAlreadyExistsException {
		logger.debug("createGraph(UriRef {}, TripleCollection {})", name,
				triples);
		VirtuosoMGraph mgraph = createVirtuosoMGraph(name);
		mgraph.addAll(triples);
		return mgraph.getGraph();
	}

	/**
	 * Clears the given graph and removes it from the loaded graphs.
	 * 
	 */
	@Override
	public void deleteTripleCollection(UriRef name)
			throws UnsupportedOperationException, NoSuchEntityException,
			EntityUndeletableException {
		logger.debug("deleteTripleCollection(UriRef {})", name);
		TripleCollection g = (VirtuosoMGraph) getTriples(name);
		if (g instanceof Graph) {
			throw new EntityUndeletableException(name);
		} else {
			((MGraph) g).clear();
			graphs.remove(name);
		}
	}

	/**
	 * Returns the names of a graph. Personally don't know why a graph should
	 * have more then 1 identifier. Anyway, this does not happen with Virtuoso
	 * 
	 * @return names
	 */
	@Override
	public Set<UriRef> getNames(Graph graph) {
		logger.debug("getNames(Graph {})", graph);
		return Collections.singleton(new UriRef(((VirtuosoMGraph) graph)
				.getName()));
	}

	/**
	 * Returns the weight of this provider.
	 * 
	 */
	@Override
	public int getWeight() {
		logger.debug("getWeight()");
		/**
		 * The weight
		 */
		return this.weight;
	}

	/**
	 * Sets the weight
	 * 
	 * @param weight
	 */
	public void setWeight(int weight) {
		logger.debug("setWeight(int {})",weight);
		this.weight = weight;
	}
}
