package org.apache.clerezza.platform.typerendering.scala

import java.io.OutputStream
import java.io.PrintWriter
import java.net.URI
import javax.ws.rs.core.MediaType
import scala.xml._
import org.apache.clerezza.platform.typerendering._
import org.apache.clerezza.rdf.utils.GraphNode
import org.apache.clerezza.rdf.ontologies._
import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.utils._
import org.apache.clerezza.rdf.scala.utils.Preamble._


/**
 * PageRenderlet.renderedPage returns an instance of this class, implementing
 * the content method to produce an XML Elmenet suitable as response to the
 * request yieldingto the arguments passed to the constructor.
 */
abstract class RenderedPage(arguments: RenderedPage.Arguments) {
	val RenderedPage.Arguments(
					res: GraphNode,
					context: GraphNode,
					sharedRenderingValues: java.util.Map[String, Object],
					renderer: CallbackRenderer,
					renderingSpecificationOption:  Option[URI],
					modeOption: Option[String],
					mediaType: MediaType,
					os: OutputStream) = arguments;
	val mode = modeOption match {
		case Some(x) => x
		case None => null
	}

	def render(resource : GraphNode) : Seq[Node] = {
		modeOption match {
			case Some(m) => render(resource, m)
			case None => render(resource, "naked")
		}
	}

	def render(resource : GraphNode, mode : String) = {
		def parseNodeSeq(string : String)  = {
			_root_.scala.xml.XML.loadString("<elem>"+string+"</elem>").child
		}
		val baos = new java.io.ByteArrayOutputStream
		renderer.render(resource, context, mode, baos)
		parseNodeSeq(new String(baos.toByteArray))
	}

	object $ {
		def apply(key: String) = sharedRenderingValues.get(key)
		def update(key: String, value: Object) = sharedRenderingValues.put(key, value)
	}

	def ifx[T](con:  => Boolean)(f: => T) :  T = {
		if (con) f else null.asInstanceOf[T]
	}

	val resultDocModifier = org.apache.clerezza.platform.typerendering.ResultDocModifier.getInstance();

	val out = new PrintWriter(os)

	out.println(
		content
	)
	out.flush()

	def content : Elem;


}
object RenderedPage {
	case class Arguments(res: GraphNode, context: GraphNode,
					sharedRenderingValues: java.util.Map[String, Object],
					renderer: CallbackRenderer ,
					renderingSpecificationOption:  Option[URI],
					modeOption: Option[String],
					mediaType: MediaType,
					os: OutputStream);
}