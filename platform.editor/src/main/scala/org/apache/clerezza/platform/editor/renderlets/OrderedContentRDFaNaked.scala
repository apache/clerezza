package org.apache.clerezza.platform.editor


import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import javax.ws.rs.core.MediaType
import org.apache.clerezza.platform.typerendering.TypeRenderlet
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.ontologies.DISCOBITS
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

@Component
@Service(Array(classOf[TypeRenderlet]))
class OrderedContentRDFaNaked extends SRenderlet {

	val getRdfType = DISCOBITS.OrderedContent
    
  override val getMediaType = MediaType.TEXT_HTML_TYPE

	override def getModePattern = "rdfa-naked"

	override def renderedPage(arguments: XmlResult.Arguments) = {
		new XmlResult(arguments) {
			override def content = {
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