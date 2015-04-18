package org.apache.clerezza.platform.editor.renderlets

import org.apache.clerezza.rdf.core._
import org.apache.clerezza.rdf.scala.utils.Preamble._
import javax.ws.rs.core.MediaType
import org.apache.clerezza.platform.typerendering.TypeRenderlet
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.ontologies.DISCOBITS
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;import scala.xml.Unparsed
import scala.xml._
import scala.xml.transform._



abstract class DiscobitsContentEtch extends SRenderlet {
    
  override val getMediaType = MediaType.TEXT_HTML_TYPE

  override def getModePattern = "edit"

  override def renderedPage(arguments: XmlResult.Arguments) = {
    new XmlResult(arguments) {
      override def content = {
        val initScript = """
            
            """
        val html = <html xmlns:disco="http://discobits.org/ontology#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"> 
          
            <head>
                <meta charset="utf-8"/>
                <link type="text/css" href="/style/style.css" rel="stylesheet" />
                <link rel="stylesheet" href="/tools/editor/styles/etch.css" />
                {for (part <- res/DISCOBITS.contains;  if ((part/DISCOBITS.pos*) == "0")) yield
                <title>Editing: {part/DISCOBITS.holds/DISCOBITS.infoBit*}</title> }
                <script src="/tools/editor/scripts/RDFaProcessor.1.3.0.js"></script>
                <script src="/tools/editor/scripts/RDFa.1.3.0.js"></script>
            </head>
          <body>
            {render(res, "rdfa-naked")}
            <script src="/tools/editor/scripts/jquery.min.js"></script>
            <script src="/tools/editor/scripts/underscore-min.js"></script>
            <script src="/tools/editor/scripts/backbone-min.js"></script>
            <script src="/tools/editor/scripts/etch.js"></script>
            <script src="/tools/editor/scripts/backbone.stickit.js"></script>
            <script src="/tools/editor/scripts/rdf-ext.js" type="text/javascript"></script>
            <script src="/tools/editor/scripts/greenrdfstore.js"></script>
            <script src="/tools/editor/scripts/editor.js" type="text/javascript"></script>
            <script>
              {Unparsed(initScript)}
            </script>
          </body>
        </html>
        
        //From: http://www.w3.org/TR/html5/syntax.html#syntax     
        //"A single newline may be placed immediately after the start tag of pre 
        //and textarea elements. If the element's contents are intended to 
        //start with a newline, two consecutive newlines thus need to be 
        //included by the author."
        object preRule extends RewriteRule {
          override def transform(n: Node): Seq[Node] = n match {
            case e:Elem
            	if(e.label == "pre") => e.child(0) match   {
            		case t : Text =>
            		  if (t.text(0) == '\n') {
             				val newText = Text("\n"+t.text)
     					    	e.copy(child = newText ++ e.child.tail)
     					    } else {
     					    	e
     					    }
     					  case _ => e
     					}
            case other => other
          }
        }
        
        object htmlLineBreaks extends RuleTransformer(preRule)

        htmlLineBreaks(html)
      }
    }
  }

}

@Component
@Service(Array(classOf[TypeRenderlet]))
class TitledContentEtch extends DiscobitsContentEtch {

  val getRdfType = DISCOBITS.TitledContent 
}
            
@Component
@Service(Array(classOf[TypeRenderlet]))
class HtmlInfoDiscobitEtch extends DiscobitsContentEtch {

  override val getRdfType = DISCOBITS.XHTMLInfoDiscoBit 
    
}

@Component
@Service(Array(classOf[TypeRenderlet]))
class OrderedContentDiscobitEtch extends DiscobitsContentEtch {

  override val getRdfType = DISCOBITS.OrderedContent
    
}