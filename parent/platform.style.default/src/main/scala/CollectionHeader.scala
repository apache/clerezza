package org.apache.clerezza.platform.style.default

import org.apache.clerezza.rdf.ontologies._

/**
 * A Renderlet for the menu
 */
class CollectionHeader extends HeadedPageRenderlet {

	override val getRdfType = HIERARCHY.Collection

}
