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
package org.apache.clerezza.platform.launcher;

import org.wymiwyg.commons.util.arguments.CommandLine;

/**
 *
 * @author mir
 */
public interface LauncherArguments {

	@CommandLine(longName = "revert", shortName = {"R"}, required = false,
	description = "Command to revert platform bundles to default. all|missing|<bundle-uri-pattern>")
	public String getRevertParam();

	@CommandLine(longName = "help", shortName = {"H"}, required = false,
	isSwitch = true, description = "Show help on command line arguments")
	public boolean getHelp();

	@CommandLine(longName = "log", shortName = {"L"}, required = false,
	description = "set the log-level, the value is one of the following: " +
	"TRACE, DEBUG, INFO, WARN, ERROR, FATAL, or NONE")
	public String getLogLevel();

	@CommandLine(longName = "port", shortName = {"P"}, required = false,
	description = "The port on which the default webservice shall listen")
	public String getPort();
	
	@CommandLine(longName = "https_port", shortName = {}, required = false,
	description = "The port on which the https secure webserver shall listen")
	public String getSecurePort();
	
	@CommandLine(longName = "https_keystore_path", shortName = {}, required = false,
	description = "The folder with the keystore for https")
	public String getKeyStorePath();
	
	@CommandLine(longName = "https_keystore_password", shortName = {}, required = false,
	description = "The folder with the keystore for https")
	public String getKeyStorePassword();
	
	@CommandLine(longName = "https_keystore_type", shortName = {}, required = false,
	description = "The type of the key-store")
	public String getKeyStoreType();
	
	@CommandLine(longName = "https_keystore_clientauth", shortName = {}, required = false,
	description = "Client Auth request, one of \"none\", \"want\" or \"need\"")
	public String getClientAuth();
	
	
}
