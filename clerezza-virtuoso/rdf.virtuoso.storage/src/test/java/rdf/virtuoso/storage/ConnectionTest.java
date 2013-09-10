package rdf.virtuoso.storage;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jdbc4.VirtuosoConnection;
import virtuoso.jdbc4.VirtuosoException;

/**
 * Tests the connection to the Virtuoso DBMS
 * 
 * @author enridaga
 */
public class ConnectionTest {

	static final Logger log = LoggerFactory.getLogger(ConnectionTest.class);

	private static VirtuosoConnection connection = null;

	@BeforeClass
	public static void before() throws ClassNotFoundException, SQLException {
		connection = TestUtils.getConnection();
	}

	@Test
	public void testIsClosed() {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		assertFalse(connection.isClosed());
	}

	@Test
	public void testIsConnectionLost() {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		assertFalse(connection.isConnectionLost());
	}

	@Test
	public void testIsReadOnly() throws VirtuosoException {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		assertFalse(connection.isReadOnly());
	}

	@Test
	public void testConnection() {
		log.info("testConnection()");
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		try {

			Statement st = connection.createStatement();
			log.debug("Populate graph <mytest>");
			String[] queries = {
					"sparql clear graph <mytest>",
					"sparql insert into graph <mytest> { <xxx> <P01> \"test1\"@en }",
					"sparql insert into graph <mytest> { <xxx> <P01> \"test2\"@it }",
					"sparql PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> insert into graph <mytest> { <xxx> <P01> \"test3\"^^xsd:string }",
					"sparql insert into graph <mytest> { <xxx> <P01> \"test4\" }",
					"sparql insert into graph <mytest> { <xxx> <P01> \"test5\" . <xxx> <P02> _:b1"
							+ " . _:b1 <P03> <yyy> " + " . _:b3 <P05> <zzz> "
							+ " . _:b3 <P05> <ppp> " +
							// This is to consider that we can force it
							" .  <nodeID://b10005> <P05> <ooo> " + " }",
					"sparql insert into graph <mytest> { <nodeID://b10005> <property> <nodeID://b10007>}",
					"sparql insert into graph <mytest> { <enridaga> <property> \"Literal value\"^^<http://datatype#type>}",
					"sparql insert into graph <mytest> { <nodeID://b10005> <property> <nodeID://b10007>}" };
			for (String q : queries) {
				log.debug("Querying: {}", q);
				st.execute(q);
			}

			String query = "sparql SELECT * from <mytest> WHERE {?s ?p ?o}";
			log.debug("Querying: {}", query);
			ResultSet rs = st.executeQuery(query);
			TestUtils.stamp(rs);
		} catch (SQLException e) {
			log.error("SQL ERROR: ", e);
			assertTrue(false);
		} catch (Exception e) {
			log.error("SQL ERROR: ", e);
			assertTrue(false);
		}
	}

	@Test
	public void test() throws ClassNotFoundException, SQLException {
		if (TestUtils.SKIP) {
			log.warn("SKIPPED");
			return;
		}
		DatabaseMetaData dm = connection.getMetaData();
		log.debug("Username is {}", dm.getUserName());
		Properties p = connection.getClientInfo();
		if (p == null) {
			log.warn("Client info is null...");
		} else
			for (Entry<Object, Object> e : p.entrySet()) {
				log.info("Client info property: {} => {}", e.getKey(),
						e.getValue());
			}
		String SQL = "SELECT DISTINCT id_to_iri(G) FROM DB.DBA.RDF_QUAD quad ";
		VirtuosoConnection cn = TestUtils.getConnection();
		long startAt = System.currentTimeMillis();
		// get the list of quad using SQL
		log.debug("Executing SQL: {}", SQL);
		cn.createStatement().executeQuery(SQL);
		long endAt = System.currentTimeMillis();
		log.debug("Using SQL: {}ms", endAt - startAt);
		SQL = "SPARQL SELECT DISTINCT ?G WHERE {GRAPH ?G {?S ?P ?O} }";
		startAt = System.currentTimeMillis();
		// get the list of quad using SQL+SPARQL
		log.debug("Executing SQL: {}", SQL);
		cn.createStatement().executeQuery(SQL);
		endAt = System.currentTimeMillis();
		log.debug("Using SQL+SPARQL: {}ms", endAt - startAt);
	}
}
