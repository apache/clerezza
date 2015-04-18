package org.apache.clerezza.platform.editor.renderlets


import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import javax.ws.rs.core.MediaType
import org.apache.clerezza.commons.rdf.BlankNode
import org.apache.clerezza.commons.rdf.BlankNodeOrIRI
import org.apache.clerezza.commons.rdf.Graph
import org.apache.clerezza.commons.rdf.impl.utils.TripleImpl
import org.apache.clerezza.platform.typerendering.TypeRenderlet
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.core.serializedform.Serializer
import org.apache.clerezza.rdf.ontologies.DISCOBITS
import org.apache.clerezza.rdf.ontologies.RDF
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;

@Component
@Service(Array(classOf[TypeRenderlet]))
class OrderedContentRDFaNaked extends SRenderlet {

  @Reference
  var serializer: Serializer = null;
  
  val getRdfType = DISCOBITS.OrderedContent
    
  override val getMediaType = MediaType.TEXT_HTML_TYPE

  override def getModePattern = "rdfa-naked"

  override def renderedPage(arguments: XmlResult.Arguments) = {
    new XmlResultWithTurtle(arguments, serializer) {

      override def addTriples(shownGraph : Graph) {
        shownGraph.add(new TripleImpl((res!).asInstanceOf[BlankNodeOrIRI], RDF.`type`, DISCOBITS.OrderedContent));
        for (part <- res/DISCOBITS.contains) {
          val entry = new BlankNode
          shownGraph.add(new TripleImpl((res!).asInstanceOf[BlankNodeOrIRI], DISCOBITS.contains, entry));
          shownGraph.add(new TripleImpl(entry, RDF.`type`, DISCOBITS.Entry));
          shownGraph.add(new TripleImpl(entry, DISCOBITS.pos, part/DISCOBITS.pos!));
          shownGraph.add(new TripleImpl(entry, DISCOBITS.holds, part/DISCOBITS.holds!));
        }
      }
      
      override def specificContent = {
              <div about={res*} typeof="disco:OrderedContent">
                {for (part <- (res/DISCOBITS.contains).sortBy(part => (part/DISCOBITS.pos*).toInt)) 
                  yield <div property="disco:contains" typeof="disco:Entry">
                       <div property="disco:pos" style="display: none">{part/DISCOBITS.pos*}</div>
                       <div property="disco:holds" resource={part/DISCOBITS.holds*}>{render(part/DISCOBITS.holds, "rdfa-naked")}</div>
                    </div>
                }
              </div>
      }
    }
  }

}