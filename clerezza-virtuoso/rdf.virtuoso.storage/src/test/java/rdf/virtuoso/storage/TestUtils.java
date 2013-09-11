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
package rdf.virtuoso.storage;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.apache.clerezza.rdf.core.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rdf.virtuoso.storage.access.VirtuosoWeightedProvider;
import virtuoso.jdbc4.VirtuosoConnection;
import virtuoso.jdbc4.VirtuosoExtendedString;
import virtuoso.jdbc4.VirtuosoRdfBox;
import virtuoso.jdbc4.VirtuosoResultSet;

/**
 * Utilities for tests
 * 
 * @author enridaga
 * 
 */
public class TestUtils {

	public static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";

	private static VirtuosoConnection connection = null;
	private static String jdbcConnectionString = null;
	private static String jdbcUser = null;
	private static String jdbcPassword = null;
	private static String jdbcDriver = null;

	static Logger log = LoggerFactory.getLogger(TestUtils.class);
	public static boolean SKIP = false;
	static {
		String skipProperty = System.getProperty("virtuoso.test");
		if (skipProperty == null) {
			log.warn("virtuoso.skip property is not set. We skip tests by default.");
			SKIP = true;
		} else
			SKIP = !skipProperty.equals("true");
	}

	public static VirtuosoConnection getConnection()
			throws ClassNotFoundException, SQLException {
		if (connection == null) {
			initConnection();
		}
		return connection;
	}

	private static void initConnection() throws ClassNotFoundException,
			SQLException {
		if (SKIP) {
			log.warn("SKIPPING ALL TESTS!");
			return;
		}
		String host = System.getProperty("virtuoso.host");
		String port = System.getProperty("virtuoso.port");
		jdbcUser = System.getProperty("virtuoso.user");
		jdbcPassword = System.getProperty("virtuoso.password");
		jdbcDriver = System.getProperty("virtuoso.driver");
		if (host == null) {
			host = "localhost";
			log.info("Missing param 'host', setting to default: {}", host);
		}
		if (port == null) {
			port = "1111";
			log.info("Missing param 'port', setting to default: {}", port);
		}
		if (jdbcUser == null) {
			jdbcUser = "dba";
			log.info("Missing param 'user', setting to default: {}", jdbcUser);
		}
		if (jdbcPassword == null) {
			jdbcPassword = "dba";
			log.info("Missing param 'password', setting to default: {}",
					jdbcPassword);
		}
		if (jdbcDriver == null) {
			jdbcDriver = "virtuoso.jdbc4.Driver";
			log.info("Missing param 'password', setting to default: {}",
					jdbcDriver);
		}

		StringBuilder cb = new StringBuilder();
		cb.append("jdbc:virtuoso://");
		cb.append(host);
		cb.append(":");
		cb.append(port);
		jdbcConnectionString = cb.toString();
		Class.forName(VirtuosoWeightedProvider.DRIVER);
		log.info("Create connection");
		connection = (VirtuosoConnection) DriverManager.getConnection(
				jdbcConnectionString, jdbcUser, jdbcPassword);

		log.debug("Connection URL: {}", jdbcConnectionString);
		log.debug("Connection user: {}", jdbcUser);
	}

	public static void stamp(ResultSet rs) {
		try {
			ResultSetMetaData rsmd;

			StringBuilder output = new StringBuilder();
			output.append(System.getProperty("line.separator"));
			output.append("------------------------------");
			output.append(System.getProperty("line.separator"));
			rsmd = rs.getMetaData();

			while (rs.next()) {
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String s = rs.getString(i);
					Object o = ((VirtuosoResultSet) rs).getObject(i);
					if (o instanceof VirtuosoExtendedString) {
						// In case is IRI
						VirtuosoExtendedString vs = (VirtuosoExtendedString) o;
						if (vs.iriType == VirtuosoExtendedString.IRI
								&& (vs.strType & 0x01) == 0x01) {
							output.append(" <" + vs.str + "> ");
							log.debug(" {} is IRI", vs.str);
						} else if (vs.iriType == VirtuosoExtendedString.BNODE) {
							log.debug(" {} is BNODE", vs.str);
							output.append(" <" + vs.str + "> ");
						} else {
							// In case is untyped literal
							log.debug(" {} is an untyped literal", vs.str);
							output.append(vs.str);
						}
					} else if (o instanceof VirtuosoRdfBox) {
						// In case is typed literal
						VirtuosoRdfBox rb = (VirtuosoRdfBox) o;
						output.append(rb.rb_box + " lang=" + rb.getLang()
								+ " type=" + rb.getType());
						log.debug(" {} is an typed literal", rb.rb_box);

					} else if (rs.wasNull()) {
						log.debug(" NULL ");
						output.append("NULL");
					} else {
						// Is simple untyped string
						output.append(s);
					}

				}
				output.append("\n");
			}

			output.append(System.getProperty("line.separator"));
			output.append("------------------------------");
			output.append(System.getProperty("line.separator"));
			log.info(output.toString());
		} catch (Exception e) {
			log.error("ERROR", e);
		}
	}

	public static void stamp(Triple next) {
		log.info("> TRIPLE : "+next.getSubject().toString() + " "
				+ next.getPredicate().toString() + " "
				+ next.getObject().toString());
	}

}
