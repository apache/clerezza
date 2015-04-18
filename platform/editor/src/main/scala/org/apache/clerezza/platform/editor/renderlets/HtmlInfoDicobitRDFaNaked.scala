package org.apache.clerezza.platform.editor.renderlets

import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import java.io.ByteArrayOutputStream
import javax.ws.rs.core.MediaType
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI
import org.apache.clerezza.commons.rdf.Graph
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl
import org.apache.clerezza.commons.rdf.impl.utils.simple.SimpleGraph
import org.apache.clerezza.platform.typerendering.TypeRenderlet
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.core.serializedform.Serializer
import org.apache.clerezza.rdf.ontologies.DISCOBITS
import org.apache.clerezza.rdf.ontologies.RDF
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;
import scala.xml.NodeBuffer
import scala.xml.Unparsed;

@Component
@Service(Array(classOf[TypeRenderlet]))
class HtmlInfoDicobitRDFaNaked extends SRenderlet {

  @Reference
  var serializer: Serializer = null;

  val getRdfType = DISCOBITS.XHTMLInfoDiscoBit

  override val getMediaType = MediaType.TEXT_HTML_TYPE

  override def getModePattern = "rdfa-naked"

  override def renderedPage(arguments: XmlResult.Arguments) = {
    new XmlResultWithTurtle(arguments, serializer) {

      override def addTriples(shownGraph : Graph) {
        shownGraph.add(new TripleImpl((res!).asInstanceOf[BlankNodeOrIRI], RDF.`type`, DISCOBITS.XHTMLInfoDiscoBit));
        shownGraph.add(new TripleImpl((res!).asInstanceOf[BlankNodeOrIRI], DISCOBITS.infoBit, res / DISCOBITS.infoBit!));
      }
      
      override def specificContent = {
        <div typeof="disco:XHTMLInfoDiscoBit" about={ res* }>
          <span property="disco:infoBit" datatype="rdf:XMLLiteral">{ Unparsed(res / DISCOBITS.infoBit*) }</span>
        </div>
      }
    }
  }

}