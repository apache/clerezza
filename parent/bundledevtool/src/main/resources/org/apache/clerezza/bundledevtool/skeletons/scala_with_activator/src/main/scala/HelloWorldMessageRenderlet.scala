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
import org.apache.clerezza.rdf.ontologies.DC

/**
 * A Renderlet for HelloWorldMessage
 */
class HelloWorldMessageRenderlet extends SRenderlet {

	val getRdfType = Ontology.HelloWordMessageType

	override def getModePattern = "naked"

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			override def content = {
				resultDocModifier.addStyleSheet("/styles/hello-world/style.css")
				<div xmlns="http://www.w3.org/1999/xhtml" id="tx-content">
					<h2>A Message</h2>
					<div class="message">{res/DC.description*}</div>
				</div>
			}
		}
	}

}
