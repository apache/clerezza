package org.apache.clerezza.platform.typerendering.scala

import java.io.IOException
import java.io.OutputStream
import java.net.URI
import javax.ws.rs.core.MediaType
import org.apache.clerezza.platform.typerendering.Renderlet.RequestProperties
import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.utils.GraphNode
import org.osgi.service.component.ComponentContext
import org.apache.clerezza.rdf.ontologies._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._

/**
 * This abstract Renderlet is overwritten to support the rendering of a 
 * particular RDF type in scala.
 * <br/>
 * Overwriting classes weill define the method renderedPage(Arguments), rdfType 
 * and optionally mode.
 * <br/>
 * This class makes sure the renderlet is registered with the renderlet manager
 * when the component is activated.
 * <br/>
 * There's typically only one instance of a PageRenderlet while a new instance
 * of RenderedPage is generated for each request.
 *
 */
abstract class PageRenderlet extends Renderlet {

	println("constructoing PageRenderlet")

	def renderedPage(renderingArguments: RenderedPage.Arguments): RenderedPage
	def rdfType: UriRef
	def mode = "naked"

	var renderletManager: RenderletManager = null;

	def ifx[T](con:  => Boolean)(f: => T) :  T = {
		if (con) f else null.asInstanceOf[T]
	}

	val resultDocModifier = org.apache.clerezza.platform.typerendering.ResultDocModifier.getInstance();


	def activate(context: ComponentContext) = {
		println("activating Page Renderlet "+this.getClass);
		renderletManager.registerRenderlet(this.getClass.getName,
				null,
				rdfType, mode,
				MediaType.APPLICATION_XHTML_XML_TYPE, true);
	}



	@throws(classOf[IOException])
	override def render(res: GraphNode, context: GraphNode,
					renderer: CallbackRenderer ,
					renderingSpecification:  URI,
					mode: String,
					mediaType: MediaType,
					requestProperties: RequestProperties,
					os: OutputStream) = {
			if (os == null) {
				throw new IllegalArgumentException("Exception!")
			}
			val renderingSpecificationOption = if (renderingSpecification != null) {Some(renderingSpecification)} else {None}
			val modeOption = if (mode != null) {Some(mode)} else {None}
			renderedPage(
				RenderedPage.Arguments(res, context, renderer,
								   renderingSpecificationOption, modeOption, mediaType, os));

	}
	
	

	def bindRenderletManager(m: RenderletManager)  = {
		renderletManager = m
	}

	def unbindRenderletManager(m: RenderletManager)  = {
		renderletManager = null
	}

}

