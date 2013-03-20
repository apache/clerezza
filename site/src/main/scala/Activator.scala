package org.apache.clerezza.site

import org.osgi.framework.{BundleActivator, BundleContext, ServiceRegistration}
import scala.collection.JavaConversions.asJavaDictionary
import org.apache.clerezza.platform.typerendering.{TypeRenderlet, RenderletManager}
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider
import org.apache.clerezza.rdf.core.access.TcManager
import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.platform.Constants
import org.apache.clerezza.rdf.core.event.{GraphEvent, FilterTriple, GraphListener}
import org.apache.clerezza.rdf.core.serializedform.{Serializer, SupportedFormat, Parser}
import java.io.{FileOutputStream, FileInputStream, File}

/**
 * Activator for a bundle using Apache Clerezza.
 */
class Activator extends BundleActivator {

	var renderletRegistration, 
	titledContentRenderletRegistration,
	globalMenuRenderletRegistration: ServiceRegistration = null
	var graphListenerOption: Option[GraphListener] = null

	/**
	 * called when the bundle is started, this method initializes the provided service
	 */
	def start(context: BundleContext) {
		val servicesDsl = new ServicesDsl(context)
		import servicesDsl._

		val renderlet = new HeadedPageRenderlet
		renderletRegistration = context.registerService(classOf[TypeRenderlet].getName,
												  renderlet, null)
		titledContentRenderletRegistration = context.registerService(classOf[TypeRenderlet].getName,
												  new TitledContentRenderlet, null)
		globalMenuRenderletRegistration = context.registerService(classOf[TypeRenderlet].getName,
												  new GlobalMenuRenderlet, null)
		context.installBundle("mvn:org.apache.clerezza/rdf.stable.serializer").start();
		context.installBundle("mvn:org.apache.clerezza/tools.offline").start();
		val path = {
			val bl = context.getBundle.getLocation
			bl.substring(bl.indexOf(':')+1)
		}
		val graphFile = new File(new File(path), "graph.nt");
		doWith {
			(tcManager: TcManager, parser: Parser) =>  {
				val contentGraph = tcManager.getMGraph(Constants.CONTENT_GRAPH_URI)
				val fileGraph = parser.parse(new FileInputStream(graphFile), SupportedFormat.N_TRIPLE)
				if (contentGraph.size > fileGraph.size) {
					println("content graph if bigger than the graph from file, not replacing with the content from file and not " +
						"writing any data to the file, you should manually either write the content graph to the file or clear " +
						"the content graph. Restart this bundle after resolving the issue.")
					graphListenerOption = None
				} else {
					contentGraph.clear
					contentGraph.addAll(fileGraph)
					println("the content graph has been replaced with "+graphFile)
					object graphListener extends GraphListener {
						val serializer = $[Serializer]
						override def graphChanged(events: java.util.List[GraphEvent]) {
							serializer.serialize(new FileOutputStream(graphFile), contentGraph,SupportedFormat.N_TRIPLE)
						}
					}
					contentGraph.addGraphListener(graphListener, new FilterTriple(null, null, null), 2000)
					graphListenerOption = Some(graphListener)
					println("A GraphListener has been added that writes changes to the content graph to graph.nt")
				}
			}
		}
	}


	/**
	 * called when the bundle is stopped, this method unregisters the provided service
	 */
	def stop(context: BundleContext) {
		renderletRegistration.unregister()
		titledContentRenderletRegistration.unregister()
		globalMenuRenderletRegistration.unregister()
		val servicesDsl = new ServicesDsl(context)
		import servicesDsl._
		val tcManager = $[TcManager]
		val contentGraph = tcManager.getMGraph(Constants.CONTENT_GRAPH_URI)
		graphListenerOption match {
			case Some(l) => contentGraph.removeGraphListener(l)
			case None => ;
		}
		println("bye")
	}

}
