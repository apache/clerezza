package org.apache.clerezza.platform.style.default

import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.rdf.ontologies._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import org.apache.clerezza.platform.typerendering.scala._

/**
 * A Renderlet for rdf:ListS
 */
class RdfListRenderlet extends SRenderlet {

	val getRdfType = RDF.List

	
	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			override def content = {
				<div id="tx-content" class="list">
					{for (entry <- res!!) yield
						<div class="entry">
							{render(entry, mode)}
						</div>
					}
				</div>
			}
		}
	}

}
