package org.apache.clerezza.platform.typerendering.gui

import org.apache.clerezza.rdf.core.UriRef

object Ontology {

	private def p(s: String) = new UriRef("http://clerezza.org/2011/25/renderletgui#"+s)

	val RenderletOverviewPage = p("RenderletOverviewPage")
	val Renderlet = p("Renderlet")
	val renderlet = p("renderlet")
	val modePattern = p("modePattern")
	val mediaType = p("mediaType")
	val rdfType = p("renderedType")
	val providingBundle = p("providingBundle")
	val stringRepresentation = p("stringRepresentation")

}
