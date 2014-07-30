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
package org.apache.clerezza.rdf.virtuoso.storage.access;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.PooledConnection;

import org.apache.clerezza.rdf.core.Graph;
import org.apache.clerezza.rdf.core.MGraph;
import org.apache.clerezza.rdf.core.TripleCollection;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.access.EntityAlreadyExistsException;
import org.apache.clerezza.rdf.core.access.EntityUndeletableException;
import org.apache.clerezza.rdf.core.access.NoSuchEntityException;
import org.apache.clerezza.rdf.core.access.TcManager;
import org.apache.clerezza.rdf.core.access.QueryableTcProvider;
import org.apache.clerezza.rdf.core.access.TcProvider;
import org.apache.clerezza.rdf.core.access.WeightedTcProvider;
import org.apache.clerezza.rdf.virtuoso.storage.VirtuosoGraph;
import org.apache.clerezza.rdf.virtuoso.storage.VirtuosoMGraph;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jdbc4.VirtuosoConnectionPoolDataSource;
import virtuoso.jdbc4.VirtuosoException;

/**
 * A {@link org.apache.clerezza.rdf.core.access.WeightedTcProvider} for
 * Virtuoso.
 * 
 * @author enridaga
 * 
 */
@Component(metatype = true, immediate = true)
@Service({WeightedTcProvider.class, TcProvider.class, QueryableTcProvider.class})
@Properties({
		@Property(name = "password", description = "User password"),
		@Property(name = "host", description = "The host running the Virtuoso server"),
		@Property(name = "port", description = "The port number"),
		@Property(name = "user", description = "User name"),
		@Property(name = "weight", intValue = 110, description = "Weight assigned to this provider"),
		@Property(name = TcManager.GENERAL_PURPOSE_TC, boolValue = true) })
public class VirtuosoWeightedProvider implements WeightedTcProvider, QueryableTcProvider {

	// JDBC driver class (XXX move to DataAccess?)
	public static final String DRIVER = "virtuoso.jdbc4.Driver";

	// Default value for the property "weight"
	public static final int DEFAULT_WEIGHT = 110;

	// Names of properties in OSGi configuration
	public static final String HOST = "host";
	public static final String PORT = "port";
	public static final String USER = "user";
	public static final String PASSWORD = "password";
	public static final String WEIGHT = "weight";

	// Name of the graph used to contain the registry of the created graphs
	public static final String ACTIVE_GRAPHS_GRAPH = "urn:x-virtuoso:active-graphs";

	// Loaded graphs
	private Map<UriRef, VirtuosoMGraph> graphs = new HashMap<UriRef, VirtuosoMGraph>();

	// DataAccess registry
	private Set<DataAccess> dataAccessSet = new HashSet<DataAccess>();

	// ConnectionPool
	private VirtuosoConnectionPoolDataSource pds = null;
	
	// Logger
	private Logger logger = LoggerFactory
			.getLogger(VirtuosoWeightedProvider.class);

	// Fields
	private String host;
	private Integer port;
	private String user;
	private String pwd;
	
	private int weight = DEFAULT_WEIGHT;
	private String charset = "UTF-8";
	private String roundrobin = "0";

	private DataAccess sparqlDataAccess;
	
	/**
	 * Creates a new {@link VirtuosoWeightedProvider}.
	 * 
	 * Before the weighted provider can be used, the method
	 * <code>activate</code> has to be called.
	 */
	public VirtuosoWeightedProvider() {
		logger.debug("Created VirtuosoWeightedProvider.");
	}

	public VirtuosoWeightedProvider(String host, Integer port,
			String jdbcUser, String jdbcPassword) {
		this.host = host;
		this.port = port;
		this.user = jdbcUser;
		this.pwd = jdbcPassword;
		initConnectionPoolDataSource();
		this.sparqlDataAccess = createDataAccess();
	}

	private void initConnectionPoolDataSource(){
		if(pds != null){
			try {
				pds.close();
			} catch (SQLException e) {
				logger.error("Cannot close connection pool datasource", e);
			}finally{
				pds = null;
			}
		}
		// Build connection string
		pds = new VirtuosoConnectionPoolDataSource();
		try {
			pds.setInitialPoolSize(10);
		} catch (SQLException e) {
			logger.error("Cannot set initial pool size", e);
		}
		pds.setUsepstmtpool(true);
		pds.setServerName(host);
		pds.setPortNumber(port);
		pds.setUser(user);
		pds.setPassword(pwd);
		pds.setCharset(charset);
		pds.setRoundrobin(roundrobin.equals("1"));
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
	public void activate(ComponentContext cCtx) throws ConfigurationException {
		logger.trace("activate(ComponentContext {})", cCtx);
		logger.info("Activating VirtuosoWeightedProvider...");

		if (cCtx == null) {
			logger.error("No component context given and connection was not set");
			throw new IllegalArgumentException(
					"No component context given and connection was not set");
		} else if (cCtx != null) {
			logger.debug("Context is given: {}", cCtx);
			String pid = (String) cCtx.getProperties().get(
					Constants.SERVICE_PID);
			try {

				// Bind logging of DriverManager
				if (logger.isDebugEnabled()) {
					logger.debug("Activating logging for DriverManager");
					// DriverManager.setLogWriter(new PrintWriter(System.err));
					DriverManager.setLogWriter(new PrintWriter(new Writer() {
						private Logger l = LoggerFactory
								.getLogger(DriverManager.class);
						private StringBuilder b = new StringBuilder();

						@Override
						public void write(char[] cbuf, int off, int len)
								throws IOException {
							b.append(cbuf, off, len);
						}

						@Override
						public void flush() throws IOException {
							l.debug("{}", b.toString());
							b = new StringBuilder();
						}

						@Override
						public void close() throws IOException {
							l.debug("{}", b.toString());
							l.debug("Log DriverManager PrintWriter closed");
						}
					}));
				}

				weight = readIntegerProperty( cCtx.getProperties(), WEIGHT, DEFAULT_WEIGHT );

				/**
				 * Initialize connection properties
				 */
				// We take the configuration of the SCR component
				Object phost = cCtx.getProperties().get(HOST);
				Object pport = readIntegerProperty( cCtx.getProperties(), PORT, null );
				Object puser = cCtx.getProperties().get(USER);
				Object ppwd = cCtx.getProperties().get(PASSWORD);

				// If the component is not configured, we inspect system properties
				// Maybe this is a first launch, otherwise we set a value as default
				if(phost == null && System.getProperty("virtuoso.host") != null){
					phost = System.getProperty("virtuoso.host");
				} else if(phost == null){
					phost = "localhost"; 
				}
				if(pport == null && System.getProperty("virtuoso.port") != null){
					pport = Integer.valueOf(System.getProperty("virtuoso.port"));
				} else if(pport == null){
					pport = Integer.valueOf(1111); 
				}
				if(puser == null && System.getProperty("virtuoso.user") != null){
					puser = System.getProperty("virtuoso.user");
				} else if(puser == null){
					puser = "dba"; 
				}
				if(ppwd == null && System.getProperty("virtuoso.password") != null){
					ppwd = System.getProperty("virtuoso.password");
				} else if(ppwd == null){
					ppwd = "dba"; 
				}
				// We set the configuration
				host = (String) phost;
				port = (Integer) pport;
				user = (String) puser;
				pwd = (String) ppwd;

				logger.info("Connecting to Virtuoso on '{}' with username '{}'", host + ":" + port, user);
				initConnectionPoolDataSource();
				// Prepare SPARQL data access
				this.sparqlDataAccess = createDataAccess();
				
				// Check connection
				Connection connection = getConnection();
				boolean ok = connection.isValid(10); // Please answer in 10 sec!
				connection.close();
				if(!ok){
					logger.error("Connection test failed: {}", ok);
					throw new ComponentException("A problem occurred while initializing connection to Virtuoso.");
				}
				logger.info("Connection {} initialized. User is {}", connection, user);
				// everything went ok
			} catch (VirtuosoException e) {
				logger.error(
						"A problem occurred while initializing connection to Virtuoso",
						e);
				logger.error("Be sure you have configured the connection parameters correctly in the OSGi/SCR configuration");
				cCtx.disableComponent(pid);
				throw new ComponentException(e.getLocalizedMessage());
			} catch (SQLException e) {
				logger.error(
						"A problem occurred while initializing connection to Virtuoso",
						e);
				logger.error("Be sure you have configured the connection parameters correctly in the OSGi/SCR configuration");
				cCtx.disableComponent(pid);
				throw new ComponentException(e.getLocalizedMessage());
			} 
		}
		// Load remembered graphs
		Set<UriRef> remembered = readRememberedGraphs();
		for (UriRef name : remembered) {
			if (canModify(name)) {
				graphs.put(name, new VirtuosoMGraph(name.getUnicodeString(),
						createDataAccess()));
			} else {
				graphs.put(name, new VirtuosoGraph(name.getUnicodeString(),
						createDataAccess()));
			}
		}
		logger.info("Activated VirtuosoWeightedProvider.");
	}

	public static final String getConnectionString(String hostName,
			Integer portNumber) {
		return new StringBuilder().append("jdbc:virtuoso://").append(hostName)
				.append(":").append(portNumber).append("/charset=UTF-8/log_enable=2")
				.toString();
	}
	
	private Integer readIntegerProperty( Dictionary<?, ?> properties, String key, Integer defaultValue ) throws ConfigurationException {
		// Start if with default.
		Integer value = defaultValue;

		Object propertyValue = properties.get( key );
		if(propertyValue instanceof Number){
			value = ((Number)propertyValue).intValue();
		} else if(propertyValue != null){
			try {
				value = new BigDecimal(propertyValue.toString()).intValueExact();
			} catch (RuntimeException e) {
				throw new ConfigurationException( key, "Unable to parse integer!", e);
			}
		}
		return value;
	}

	private Set<UriRef> readRememberedGraphs() {
		logger.trace(" readRememberedGraphs()");
		String SQL = "SPARQL SELECT DISTINCT ?G FROM <" + ACTIVE_GRAPHS_GRAPH
				+ "> WHERE { ?G a <urn:x-virtuoso/active-graph> }";
		Connection connection = null;
		Exception e = null;
		Statement st = null;
		ResultSet rs = null;
		Set<UriRef> remembered = new HashSet<UriRef>();
		try {
			connection = getConnection();
			st = connection.createStatement();
			logger.debug("Executing SQL: {}", SQL);
			rs = (ResultSet) st.executeQuery(SQL);
			while (rs.next()) {
				UriRef name = new UriRef(rs.getString(1));
				logger.debug(" > Graph {}", name);
				remembered.add(name);
			}
		} catch (VirtuosoException e1) {
			logger.error("Error while executing query/connection.", e1);
			e = e1;
		} catch (SQLException e1) {
			logger.error("Error while executing query/connection.", e1);
			e = e1;
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception ex) {
				logger.error("Cannot close result set", ex);
			}
			try {
				if (st != null)
					st.close();
			} catch (Exception ex) {
				logger.error("Cannot close statement", ex);
			}
			try{
				if(connection != null) connection.close();
			}catch (Exception ex) {
				logger.error("Cannot close connection", ex);
			}
		}
		if (e != null) {
			throw new RuntimeException(e);
		}
		return remembered;
	}

	private void rememberGraphs(UriRef... graphs) {
		logger.trace(" saveActiveGraphs()");
		if (graphs.length > 0) {
			// Returns the list of graphs in the virtuoso quad store
			String SQL = "SPARQL INSERT INTO <" + ACTIVE_GRAPHS_GRAPH
					+ "> { `iri(??)` a <urn:x-virtuoso/active-graph> }";
			Connection connection = null;
			Exception e = null;
			PreparedStatement st = null;
			ResultSet rs = null;
			try {
				try {
					connection = getConnection();
					st = (PreparedStatement) connection
							.prepareStatement(SQL);
					logger.debug("Executing SQL: {}", SQL);
					for (UriRef u : graphs) {
						logger.trace(" > remembering {}", u);
						st.setString(1, u.getUnicodeString());
						st.executeUpdate();
					}
				} catch (Exception e1) {
					logger.error("Error while executing query/connection.", e1);
					e = e1;
				}
			} finally {
				try {
					if (rs != null)
						rs.close();
				} catch (Exception ex) {
					logger.error("Cannot close result set", ex);
				}
				try {
					if (st != null)
						st.close();
				} catch (Exception ex) {
					logger.error("Cannot close statement", ex);
				}
				try{
					if(connection != null) connection.close();
				}catch (Exception ex) {
					logger.error("Cannot close connection", ex);
				}
			}
			if (e != null) {
				throw new RuntimeException(e);
			}
		}
	}

	private void forgetGraphs(UriRef... graphs) {
		logger.trace(" forgetGraphs()");
		if (graphs.length > 0) {
			// Returns the list of graphs in the virtuoso quad store
			String SQL = "SPARQL WITH <"
					+ ACTIVE_GRAPHS_GRAPH
					+ "> DELETE { ?s ?p ?v } WHERE { ?s ?p ?v . FILTER( ?s = iri(??) ) }";
			Exception e = null;
			Connection connection = null;
			PreparedStatement st = null;
			ResultSet rs = null;
			try {
				connection = getConnection();
				st = (PreparedStatement) connection
						.prepareStatement(SQL);
				logger.debug("Executing SQL: {}", SQL);
				for (UriRef u : graphs) {
					logger.trace(" > remembering {}", u);
					st.setString(1, u.getUnicodeString());
					st.executeUpdate();
				}
			} catch (SQLException e1) {
				logger.error("Error while executing query/connection.", e1);
				e = e1;
			} finally {

				try {
					if (rs != null)
						rs.close();
				} catch (Exception ex) {
					logger.error("Cannot close result set", ex);
				}
				try {
					if (st != null)
						st.close();
				} catch (Exception ex) {
					logger.error("Cannot close statement", ex);
				}
				try{
					if(connection != null) connection.close();
				}catch (Exception ex) {
					logger.error("Cannot close connection", ex);
				}
			}
			if (e != null) {
				throw new RuntimeException(e);
			}
		}
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
		// Save active (possibly empty) graphs to a dedicated graph
		rememberGraphs();
		// XXX Important. Close all opened resources
		for (DataAccess mg : dataAccessSet) {
			mg.close();
		}
		try {
			pds.close();
		} catch (SQLException e) {
			logger.error("Cannot close connection pool data source", e);
		}
		logger.info("Shutdown complete.");
	}

	public Connection getConnection() throws SQLException {
		return AccessController
				.doPrivileged(new PrivilegedAction<Connection>() {
					public Connection run() {
						try {
							PooledConnection pconn = pds.getPooledConnection();
							return pconn.getConnection();
						} catch (Throwable e) {
							throw new RuntimeException(e);
						}
					}
				});
	}

//	private VirtuosoConnection getConnection(final String connStr,final String user,
//			final String pwd) throws SQLException, ClassNotFoundException {
//		logger.debug("getConnection(String {}, String {}, String *******)",
//				connStr, user);
//		/**
//		 * FIXME For some reasons, it looks the DriverManager is instantiating a
//		 * new virtuoso.jdbc4.Driver instance upon any activation. (Enable DEBUG
//		 * to see this)
//		 */
//		logger.debug("Loading JDBC Driver");
//		try {
//			VirtuosoConnection c = AccessController
//					.doPrivileged(new PrivilegedAction<VirtuosoConnection>() {
//						public VirtuosoConnection run() {
//							try {
//								Class.forName(VirtuosoWeightedProvider.DRIVER,
//										true, this.getClass().getClassLoader());
//								return (VirtuosoConnection) DriverManager
//										.getConnection(connStr, user, pwd);
//							} catch (ClassNotFoundException e) {
//								throw new RuntimeException(e);
//							} catch (SQLException e) {
//								throw new RuntimeException(e);
//							}
//						}
//					});
//			c.setAutoCommit(true);
//			return c;
//		} catch (SQLException e) {
//			throw new RuntimeException(e);
//		}
//	}

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
	 * Load the graph once. It check whether a graph object have been already
	 * created for that UriRef, if yes returns it.
	 * 
	 * If not check if at least 1 triple is present in the quad for such graph
	 * identifier. If yes, creates a new graph object and loads it in the map,
	 * referring to it on next calls.
	 *
	 * This method returns a VirtuosoGraph if the graph is read-only
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
		} else {
			VirtuosoMGraph graph = null;
			logger.debug("Attempt to load {}", name);
			// Let's create the graph object
			String SQL = "SPARQL SELECT ?G WHERE { GRAPH ?G {[] [] []} . FILTER(?G = "
					+ name + ")} LIMIT 1";

			Statement st = null;
			ResultSet rs = null;
			Connection connection = null;
			Exception e = null;
			try {
				connection = getConnection();
				st = connection.createStatement();
				logger.debug("Executing SQL: {}", SQL);
				st.execute(SQL);
				rs = (ResultSet) st.getResultSet();
				if (rs.next() == false) {
					// The graph is empty, it is not readable or does not exists
					logger.debug("Graph does not exists: {}", name);
					throw new NoSuchEntityException(name);
				} else {
					// The graph exists and it is readable ...
					logger.trace("Graph {} is readable", name);
					// is it writable?
					logger.trace("Is {} writable?", name);
					if (canModify(name)) {
						logger.trace("Creating writable graph {}",
								name);
						graphs.put(name,
								new VirtuosoMGraph(name.getUnicodeString(),
										createDataAccess()));
					} else {
						logger.trace("Creating read-only graph {}",
								name);
						graphs.put(name,
								new VirtuosoMGraph(name.getUnicodeString(),
										createDataAccess()).asVirtuosoGraph());
					}
					graph = graphs.get(name);
				}

			} catch (VirtuosoException ve) {
				logger.error("Error while executing query/connection.", ve);
				e = ve;
			} catch (SQLException se) {
				logger.error("Error while executing query/connection.", se);
				e = se;
			} finally {
				try {
					if (rs != null)
						rs.close();
				} catch (Exception ex) {
					logger.error("Cannot close result set", ex);
				}
				;
				try {
					if (st != null)
						st.close();
				} catch (Exception ex) {
					logger.error("Cannot close statement", ex);
				}
				try{
					if(connection != null) connection.close();
				}catch (Exception ex) {
					logger.error("Cannot close connection", ex);
				}
			}
			if (e != null) {
				throw new RuntimeException(e);
			}
			return graph;
		}

	}

	public DataAccess createDataAccess() {
		DataAccess da = new DataAccess(pds);
		dataAccessSet.add(da);
		// Remember all opened ones
		return da;
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
		String SQL = "SPARQL SELECT DISTINCT ?G WHERE {GRAPH ?G {[] [] []} }";
		Connection connection = null;
		Exception e = null;
		Statement st = null;
		ResultSet rs = null;
		try {
			connection = getConnection();
			st = (Statement) connection.createStatement();
			logger.debug("Executing SQL: {}", SQL);
			rs = (ResultSet) st.executeQuery(SQL);
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
		} finally {

			try {
				if (rs != null)
					rs.close();
			} catch (Exception ex) {
				logger.error("Cannot close result set", ex);
			}
			;
			try {
				if (st != null)
					st.close();
			} catch (Exception ex) {
				logger.error("Cannot close statement", ex);
			}
			;
			if (connection != null) {
				try {
					connection.close();
				} catch (Throwable e1) {
					logger.error("Cannot close connection", e1);
				}
			}
		}
		if (e != null) {
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
		Connection connection = null;
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
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception ex) {
				logger.error("Cannot close result set", ex);
			}
			try {
				if (st != null)
					st.close();
			} catch (Exception ex) {
				logger.error("Cannot close statement", ex);
			}
			try{
				if(connection != null) connection.close();
			}catch (Exception ex) {
				logger.error("Cannot close connection", ex);
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
						createDataAccess()));
				rememberGraphs(name);
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
			forgetGraphs(name);
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
		logger.debug("setWeight(int {})", weight);
		this.weight = weight;
	}
	
	/**
	 * Executes a SPARQL query
	 */
	@Override
	public Object executeSparqlQuery(String query, UriRef defaultGraphUri) {
		return this.sparqlDataAccess.executeSparqlQuery(query, defaultGraphUri);
	}
}
