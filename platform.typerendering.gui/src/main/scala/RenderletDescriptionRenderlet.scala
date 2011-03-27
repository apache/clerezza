package org.apache.clerezza.platform.typerendering.gui

import javax.ws.rs.core.MediaType
import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.ontologies._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.ontologies.DC

/**
 * A Renderlet for the descriptions of RenderletDescription
 */
class RenderletDescriptionRenderlet extends SRenderlet {

	val getRdfType = Ontology.Renderlet

	override def getModePattern = "naked"

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			override def content = {
				resultDocModifier.setTitle("Renderlet Overview")
				resultDocModifier.addStyleSheet("/styles/renderlets/style.css")
				<div class="renderlet">
					<div>Renderlet: <span class="value">{res/Ontology.stringRepresentation*}</span></div>
					<div>For type: <span class="value">{res/Ontology.rdfType*}</span></div>
					<div>Producing: <span class="value">{res/Ontology.mediaType*}</span></div>
					{if ((res/Ontology.modePattern).size > 0) <div>Mode pattern: <span class="value">{res/Ontology.modePattern*}</span></div>}
					<div>Provided by: <span class="value">{res/Ontology.providingBundle*}</span></div>
				</div>
			}
		}
	}

}
