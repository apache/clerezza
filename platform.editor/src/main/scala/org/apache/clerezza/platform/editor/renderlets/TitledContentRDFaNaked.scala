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
class TitledContentRDFaNaked extends SRenderlet {

  val getRdfType = DISCOBITS.TitledContent 
    
  override val getMediaType = MediaType.TEXT_HTML_TYPE

  override def getModePattern = "rdfa-naked"

  override def renderedPage(arguments: XmlResult.Arguments) = {
    new XmlResult(arguments) {
      override def content = {
              <div typeof="disco:TitledContent" about={res*}>
              {for (part <- res/DISCOBITS.contains;  if ((part/DISCOBITS.pos*) == "0")) 
                yield <span property="disco:holds" typeof="disco:Entry">
                   <span property="disco:pos" style="display: none">0</span>
                   <h1 resource={part/DISCOBITS.holds*} property="disco:holds">{render(part/DISCOBITS.holds, "rdfa-naked")}</h1>
                  </span>}
              {for (part <- res/DISCOBITS.contains;  if ((part/DISCOBITS.pos*) == "1")) 
                yield <div property="disco:contains" typeof="disco:Entry">
                       <div property="disco:pos" style="display: none">{part/DISCOBITS.pos*}</div>
                       <div property="disco:holds" resource={part/DISCOBITS.holds*}>{render(part/DISCOBITS.holds, "rdfa-naked")}</div>
                    </div>}
              </div>
      }
    }
  }

}