package org.apache.clerezza.platform.typerendering.gui

import org.apache.clerezza.rdf.core.BNode
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.osgi.framework.{BundleActivator, BundleContext, ServiceRegistration}
import scala.collection.JavaConversions._
import org.apache.clerezza.platform.dashboard.GlobalMenuItem
import org.apache.clerezza.platform.dashboard.GlobalMenuItemsProvider
import org.apache.clerezza.platform.typerendering.{TypeRenderlet, RenderletManager}
import java.util.HashSet
import javax.ws.rs._
import org.apache.clerezza.rdf.utils.GraphNode;
import org.apache.clerezza.rdf.scala.utils._
import org.apache.clerezza.rdf.ontologies.{RDF, DC, PLATFORM}
import org.apache.clerezza.rdf.core.access.security.TcPermission
import java.security.{AccessControlException, AccessController}

/**
 * Activator for a bundle using Apache Clerezza.
 */
class Activator extends BundleActivator {

	private var renderletsOverview: ServiceRegistration = null
	private var renderletRegistration, menuProviderRegistration: ServiceRegistration = null
	private var bundleContext: BundleContext = null


	final val path = "admin/renderlets/overview"
	@Path(path)
	object RenderletsOverview {
		@GET def get() = {
			val resultMGraph = new SimpleMGraph();
			val preamble = new Preamble(resultMGraph)
			import preamble._
			val resultNode = new GraphNode(new BNode(), resultMGraph);
			resultNode.addProperty(RDF.`type` , Ontology.RenderletOverviewPage);
			resultNode.addProperty(RDF.`type` , PLATFORM.HeadedPage);
			resultNode.addProperty(RDF.`type` , RDF.List);
			val renderletList = resultNode.asList;
			for (sr <- bundleContext.getServiceReferences(classOf[TypeRenderlet].getName, null)) {
				val renderlet = bundleContext.getService(sr).asInstanceOf[TypeRenderlet]
				val rendRes = new BNode()
				rendRes.addProperty(RDF.`type`, Ontology.Renderlet);
				rendRes.addPropertyValue(Ontology.mediaType,
										renderlet.getMediaType.toString)
				if (renderlet.getModePattern != null) rendRes.addPropertyValue(Ontology.modePattern,
										renderlet.getModePattern)
				rendRes.addProperty(Ontology.rdfType,
										renderlet.getRdfType)
				rendRes.addPropertyValue(Ontology.stringRepresentation,
										renderlet.toString)
				rendRes.addPropertyValue(Ontology.providingBundle,
										sr.getBundle.getLocation)
				renderletList.add(rendRes)
			}
			resultNode;
		}
	}

	object MenuProvider extends GlobalMenuItemsProvider {
		override def getMenuItems: java.util.Set[GlobalMenuItem] = {
			import collection.JavaConversions._
			val result = new HashSet[GlobalMenuItem]();
			try {
				//TODO should have a more general way to say that a user has some administrative priviledges
				AccessController.checkPermission(new TcPermission("urn:x-localinstance:/content.graph", "readwrite"))
			}
			catch {
				case e: AccessControlException => {
					return result
				}
			}
			result.add(new GlobalMenuItem("/"+path,"renderlet-overview", "Renderlet Overview", -999, "Administration"))
			result
		}
	}

	/**
	 * called when the bundle is started, this method initializes the provided service
	 */
	def start(context: BundleContext) {
		this.bundleContext = context
		val args = scala.collection.mutable.Map("javax.ws.rs" -> true)
		renderletsOverview = context.registerService(classOf[Object].getName,
													 RenderletsOverview, args)
		val renderlet = new RenderletDescriptionRenderlet
		val serviceReference = context.getServiceReference(classOf[RenderletManager].getName)
		renderletRegistration = context.registerService(classOf[TypeRenderlet].getName,
														renderlet, null)
		menuProviderRegistration = context.registerService(classOf[GlobalMenuItemsProvider].getName,
														MenuProvider, null)
	}

	/**
	 * called when the bundle is stopped, this method unregisters the provided service
	 */
	def stop(context: BundleContext) {
		renderletsOverview.unregister()
		renderletRegistration.unregister()
		menuProviderRegistration.unregister()
		this.bundleContext = null
	}

}
