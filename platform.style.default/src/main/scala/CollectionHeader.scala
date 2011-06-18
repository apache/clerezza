package org.apache.clerezza.platform.style.default

import org.apache.clerezza.rdf.ontologies._
import org.apache.clerezza.rdf.scala.utils.RichGraphNode
import org.apache.clerezza.rdf.scala.utils.Preamble._

/**
 * A Renderlet for the menu
 */
class CollectionHeader extends HeadedPageRenderlet {

	override val getRdfType = HIERARCHY.Collection

	override def defaultTitle(res: RichGraphNode) = (res*) +  " (Collection)"


}
