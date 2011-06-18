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
	registerRenderlet(new TitledContentRenderlet)
	registerRenderlet(new CollectionHeader)
	
}
