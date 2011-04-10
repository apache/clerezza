package skeleton

import javax.ws.rs._
import org.apache.clerezza.rdf.core.BNode
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.rdf.ontologies.{DC, RDF}
import org.apache.clerezza.rdf.utils.GraphNode
import org.osgi.framework.BundleContext
import org.apache.clerezza.osgi.services.ServicesDsl
import org.apache.clerezza.platform.graphprovider.content.ContentGraphProvider

/**
 * The classical Hello World root resource
 */
@Path("hello-world")
class HelloWorld(context: BundleContext) {
	val servicesDsl = new ServicesDsl(context)
	import servicesDsl._
	@GET def get() = {
		val resultMGraph = new SimpleMGraph();
		val graphNode = new GraphNode(new BNode(), resultMGraph);
		graphNode.addProperty(RDF.`type` , Ontology.HelloWordMessageType);
		val cgp: ContentGraphProvider = $[ContentGraphProvider]
		graphNode.addPropertyValue(DC.description,"Hello world of "+cgp.getContentGraph.size);
		graphNode;

	}
}
