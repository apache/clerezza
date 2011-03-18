package skeleton

import javax.ws.rs._
import org.apache.clerezza.rdf.core.BNode
import org.apache.clerezza.rdf.core.impl.SimpleMGraph
import org.apache.clerezza.rdf.ontologies.{DC, RDF}
import org.apache.clerezza.rdf.utils.GraphNode;

/**
 * The classical Hello World root resource
 */
@Path("hello-world")
class HelloWorld {
	@GET def get() = {
		val resultMGraph = new SimpleMGraph();
		val graphNode = new GraphNode(new BNode(), resultMGraph);
		graphNode.addProperty(RDF.`type` , Ontology.HelloWordMessageType);
		graphNode.addPropertyValue(DC.description,"Hello world");
		graphNode;

	}
}
