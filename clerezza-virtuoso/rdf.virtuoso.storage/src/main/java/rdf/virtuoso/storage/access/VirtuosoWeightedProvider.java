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
	private Map<UriRef, VirtuosoMGraph> graphs = null;

	/**
	 * JDBC Connection to Virtuoso DBMS
	 */
	private VirtuosoConnection connection = null;

	/**
	 * Weight
	 */
	private int weight = DEFAULT_WEIGHT;

	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory
			.getLogger(VirtuosoWeightedProvider.class);

	/**
	 * Creates a new {@link VirtuosoWeightedProvider}.
	 * 
	 * Before the weighted provider can be used, the method
	 * <code>activate</code> has to be called.
	 */
	public VirtuosoWeightedProvider() {
		logger.debug("Created VirtuosoWeightedProvider.");
	}

	/**
	 * Creates a new {@link VirtuosoWeightedProvider}
	 * 
	 * @param connection
	 */
	public VirtuosoWeightedProvider(VirtuosoConnection connection) {
		logger.debug("Created VirtuosoWeightedProvider with connection: {}",
				connection);
		this.connection = connection;
	}

	/**
	 * Creates a new {@link VirtuosoWeightedProvider}
	 * 
	 * @param connection
	 * @param weight
	 */
	public VirtuosoWeightedProvider(VirtuosoConnection connection, int weight) {
		logger.debug(
				"Created VirtuosoWeightedProvider with connection = {} and weight = {}.",
				connection, weight);
		this.weight = weight;
		this.connection = connection;
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
		if (cCtx == null && connection == null) {
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
				String host = (String) cCtx.getProperties().get(HOST);
				int port = (Integer) cCtx.getProperties().get(PORT);
				String user = (String) cCtx.getProperties().get(USER);
				String pwd = (String) cCtx.getProperties().get(PASSWORD);

				// Build connection string
				String connStr = new StringBuilder().append("jdbc:virtuoso://")
						.append(host).append(":").append(port).toString();
				// Init connection
				this.initConnection(connStr, user, pwd);

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
		try {
			if (this.connection != null) {
				if (this.connection.isClosed()) {
					logger.debug("Connection is already closed");
				} else {
					logger.debug("Closing connection");
					// We close the connection
					this.connection.close();
				}
			}
		} catch (Exception re) {
			logger.warn(re.toString(), re);
			throw new RuntimeException(re);
		}
		logger.info("Shutdown complete.");
	}

	/**
	 * Initialize the JDBC connection
	 * 
	 * @param connStr
	 * @param user
	 * @param pwd
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	private void initConnection(String connStr, String user, String pwd)
			throws SQLException, ClassNotFoundException {
		logger.debug("initConnection(String {}, String {}, String *******)",
				connStr, user);
		if (this.connection != null) {
			logger.debug("Connection already instantiated: {}", this.connection);
			logger.debug("Closing connection");
			this.connection.close();
		}
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
		connection = (VirtuosoConnection) DriverManager.getConnection(connStr,
				user, pwd);
		logger.debug("Connection initialized: {}", connection);
	}

	/**
	 * Whether the connection is active or not
	 * 
	 * @return
	 */
	public boolean isConnectionAlive() {
		logger.debug("isConnectionAlive() : {}", connection);
		if (this.connection == null) {
			logger.warn("Connection is null");
			return false;
		}
		if (this.connection.isClosed()) {
			logger.warn("Connection is closed");
			return false;
		}
		if (this.connection.isConnectionLost()) {
			logger.warn("Connection is lost");
			return false;
		}
		return true;
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
	 * WARNING! THIS SHOULD BE THE ONLY METHOD TO ACCESS THE MAP graphs
	 * 
	 * @param name
	 * @return
	 */
	private VirtuosoMGraph loadGraphOnce(UriRef name) {
		logger.debug("loadGraphOnce({})", name);
		// Is it the first itme we invoke a graph here?
		if (graphs == null) {
			graphs = new HashMap<UriRef, VirtuosoMGraph>();
		}
		// Check whether the graph have been already loaded once
		if (graphs.containsKey(name)) {
			logger.debug("{} is already loaded", name);
			return graphs.get(name);
		}

		logger.debug("Attempt to load {}", name);
		// Let's create the graph object
		String SQL = "SPARQL SELECT ?G WHERE { GRAPH ?G {?A ?B ?C} . FILTER(?G = "
				+ name + ")} LIMIT 1";

		Statement st;
		try {
			st = connection.createStatement();
			logger.debug("Executing SQL: {}", SQL);
			st.execute(SQL);
			VirtuosoResultSet rs = (VirtuosoResultSet) st.getResultSet();
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
							name.getUnicodeString(), connection));
				} else {
					logger.debug("Creating read-only Graph for graph {}", name);
					graphs.put(name, new VirtuosoMGraph(
							name.getUnicodeString(), connection)
							.asVirtuosoGraph());
				}
				return graphs.get(name);
			}
		} catch (VirtuosoException e) {
			logger.error("Error while executing query/connection.", e);
			throw new RuntimeException(e);
		} catch (SQLException e) {
			logger.error("Error while executing query/connection.", e);
			throw new RuntimeException(e);
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
		// Returns the list of graphs in the virtuoso quad store
		String SQL = "SPARQL SELECT DISTINCT ?G WHERE {GRAPH ?G {?S ?P ?O} }";
		try {
			VirtuosoStatement st = (VirtuosoStatement) this.connection
					.createStatement();
			logger.debug("Executing SQL: {}", SQL);
			VirtuosoResultSet rs = (VirtuosoResultSet) st.executeQuery(SQL);
			while (rs.next()) {
				UriRef graph = new UriRef(rs.getString(1));
				logger.debug(" > Graph {}", graph);
				graphs.add(graph);
			}
		} catch (VirtuosoException e) {
			logger.error("Error while executing query/connection.", e);
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
		try {
			logger.debug("getPermissions(String {})", graph);
			ResultSet rs;
			String sql = "SELECT DB.DBA.RDF_GRAPH_USER_PERMS_GET ('" + graph
					+ "','" + connection.getMetaData().getUserName() + "') ";
			logger.debug("Executing SQL: {}", sql);
			Statement st = connection.createStatement();
			st.execute(sql);
			rs = st.getResultSet();
			rs.next();
			long result = rs.getLong(1);
			logger.debug("Permission: {}", result);
			return result;
		} catch (VirtuosoException e) {
			logger.error("A virtuoso SQL exception occurred.");
			throw new RuntimeException(e);
		} catch (SQLException e) {
			logger.error("An SQL exception occurred.");
			throw new RuntimeException(e);
		}
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
						connection));
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
