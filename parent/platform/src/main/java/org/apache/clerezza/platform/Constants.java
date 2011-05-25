/*
 *  Copyright 2010 reto.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.clerezza.platform;

import org.apache.clerezza.rdf.core.UriRef;

/**
 * This class gropus some constant values used by the clerezza.platform components
 *
 * @author reto
 */
public class Constants {

	/**
	 * all hosts uri-prefix, uri scheme for references relative to the local
	 * instance.
	 */
	public static final String URN_LOCAL_INSTANCE = "urn:x-localinstance:";

	/**
	 * the uri of the system graph as string
	 */
	public static final String SYSTEM_GRAPH_URI_STRING =
			URN_LOCAL_INSTANCE+"/system.graph";
	/**
	 * the uri of the system graph
	 */
	public static final UriRef SYSTEM_GRAPH_URI =
			new UriRef(SYSTEM_GRAPH_URI_STRING);

	/**
	 * the uri of the config graph as string
	 */
	public static final String CONFIG_GRAPH_URI_STRING =
			URN_LOCAL_INSTANCE+"/config.graph";
	/**
	 * the uri of the config graph as string
	 */
	public static final UriRef CONFIG_GRAPH_URI =
			new UriRef(CONFIG_GRAPH_URI_STRING);

	/**
	 * the uri of the content graph as string
	 */
	public static final String CONTENT_GRAPH_URI_STRING =
			URN_LOCAL_INSTANCE+"/content.graph";
	/**
	 * the uri of the content graph
	 */
	public static final UriRef CONTENT_GRAPH_URI =
			new UriRef(CONTENT_GRAPH_URI_STRING);


	/**
	 * all hosts uri-prefix, uri scheme for which the resource are also named
	 * with the any schema/authority the instance can be reached with instead
	 * of this prefix
	 *
	 * @deprecated use URN_LOCAL_INSTANCE
	 */
	@Deprecated
	public static final String ALL_HOSTS_URI_PREFIX = URN_LOCAL_INSTANCE;

}
