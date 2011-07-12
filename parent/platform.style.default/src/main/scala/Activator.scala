/*
 *
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
 *
*/

package org.apache.clerezza.platform.style.default

import org.osgi.framework.{BundleActivator, BundleContext, ServiceRegistration}
import scala.collection.JavaConversions.asJavaDictionary
import org.apache.clerezza.platform.typerendering.{TypeRenderlet, RenderletManager}
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.osgi.services.ActivationHelper
import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.rdf.core.event.{GraphEvent, FilterTriple, GraphListener}
import org.apache.clerezza.rdf.core.serializedform.{Serializer, SupportedFormat, Parser}
import java.io.{FileOutputStream, FileInputStream, File}

/**
 * Activator for a bundle using Apache Clerezza.
 */
class Activator extends ActivationHelper {

	registerRenderlet(new GlobalMenuRenderlet)
	registerRenderlet(new HeadedPageRenderlet)
	registerRenderlet(new CollectionHeader)
	registerRenderlet(new RdfListRenderlet)
	
}
