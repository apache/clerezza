package skeleton

import javax.ws.rs.core.MediaType
import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.rdf.core.UriRef
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.ontologies._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._

/**
 * A Renderlet for HelloWorldMessage
 */
class HelloWorldMessageRenderlet extends PageRenderlet {

	val rdfType = Ontology.HelloWordMessageType
	override def mode = "naked"

	override def renderedPage(arguments: RenderedPage.Arguments): RenderedPage = {
		new RenderedPage(arguments) {
			override def content = {
				resultDocModifier.addStyleSheet("/styles/wall/wall.css")
				<div xmlns="http://www.w3.org/1999/xhtml" id="tx-content">
					<h2>Wall</h2>
				</div>
			}
		}
	}

}
