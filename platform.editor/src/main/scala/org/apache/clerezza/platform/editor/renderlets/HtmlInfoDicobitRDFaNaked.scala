package org.apache.clerezza.platform.editor


import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import javax.ws.rs.core.MediaType
import org.apache.clerezza.platform.typerendering.TypeRenderlet
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.ontologies.DISCOBITS
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;import scala.xml.Unparsed


@Component
@Service(Array(classOf[TypeRenderlet]))
class HtmlInfoDicobitRDFaNaked extends SRenderlet {

  val getRdfType = DISCOBITS.XHTMLInfoDiscoBit 
    
  override val getMediaType = MediaType.TEXT_HTML_TYPE

  override def getModePattern = "rdfa-naked"

  override def renderedPage(arguments: XmlResult.Arguments) = {
    new XmlResult(arguments) {
      override def content = {
              <div typeof="disco:XHTMLInfoDiscoBit" about={res*}>
                <span property="disco:infoBit">
                {Unparsed(res/DISCOBITS.infoBit*)}
                </span>
              </div>
      }
    }
  }

}