package org.apache.clerezza.platform.editor

import org.apache.clerezza.rdf.core._
import impl.util.W3CDateFormat
import org.apache.clerezza.rdf.scala.utils.Preamble._
import javax.ws.rs.core.MediaType
import org.apache.clerezza.platform.typerendering.TypeRenderlet
import org.apache.clerezza.platform.typerendering.scala._
import org.apache.clerezza.rdf.ontologies.DISCOBITS
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;import scala.xml.Unparsed



/**
 * A Renderlet for rss:items
 */
@Component
@Service(Array(classOf[TypeRenderlet]))
class TitledContentCreate extends SRenderlet {

  val getRdfType = DISCOBITS.TitledContent 
    
  override val getMediaType = MediaType.TEXT_HTML_TYPE

  override def getModePattern = "create"

  override def renderedPage(arguments: XmlResult.Arguments) = {
    new XmlResult(arguments) {
      override def content = {
        val initScript = """
                        var id_counter = 1;
                        
                        jQuery(document).ready(function() {
                        // Prepare VIE
                        //global for debuging
                        v = new VIE();
                        v.use(new v.RdfaService());

                        // Load Create.js with your VIE instance
                        jQuery("body").midgardCreate({
                          vie: v,
                          url: function() {
                              return "/tools/editor/create/"; },
                          stanbolUrl: "/"
                        });
                        Backbone.sync = function(method, model) {
                          console.log("I've been passed " + method + " with " + JSON.stringify(model));
                          if(method === 'create'){ model.set('id', id_counter++); }
                          console.log("method: "+method);
                          console.log(id_counter);
                          console.log(this.ajaxSettings.contentType);
                        };
                        /*jQuery('body').midgardStorage({
                          vie: new VIE(),
                          url: function (item) { return '/some32/url'; }, 
                          saveRemote: {"contentType": "application/ld+json"}
                        });*/
                      });"""
        <html xmlns:disco="http://discobits.org/ontology#"> 
          {for (part <- res/DISCOBITS.contains;  if ((part/DISCOBITS.pos*) == "0")) yield
            <head>
                <link type="text/css" href="/style/style.css" rel="stylesheet" />
                <title>Editing: {part/DISCOBITS.holds/DISCOBITS.infoBit*}</title>

                <link rel="stylesheet" href="/tools/create/font-awesome/css/font-awesome.css" />

                <link rel="stylesheet" href="/tools/create/themes/create-ui/css/create-ui.css" />

                <link rel="stylesheet" href="/tools/create/themes/midgard-notifications/midgardnotif.css" />

                <style>
                  body {{
                  padding-left: 20%;
                  padding-right: 20%;
                  padding-top: 90px;
                  background-color: #eeeeec;
                  }}
                </style>
                <script src="/tools/create/almond-0.0.2-alpha-1.js" > </script>
                <script src="/tools/create/jquery-amd-1.7.1-alpha-1.js" > </script>
                <script src="/tools/create/jquery-ui-amd-1.8.16-alpha-1.js" > </script>
                <!-- maybe less broken -->
                <script src="https://rdfquery.googlecode.com/files/jquery.rdfquery.rules.min-1.0.js"></script>
                <script src="/tools/create/createjs-1.0.0alpha1.js" > </script>
                <script>{Unparsed(initScript)}
                </script>
            </head>
          }
          <body>
            {render(res, "rdfa-naked")}
          </body>
        </html>
      }
    }
  }

}