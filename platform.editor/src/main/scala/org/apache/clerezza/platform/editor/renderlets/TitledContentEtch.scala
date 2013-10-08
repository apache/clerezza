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
class TitledContentEtch extends SRenderlet {

  val getRdfType = DISCOBITS.TitledContent 
    
  override val getMediaType = MediaType.TEXT_HTML_TYPE

  override def getModePattern = "edit"

  override def renderedPage(arguments: XmlResult.Arguments) = {
    new XmlResult(arguments) {
      override def content = {
        val initScript = """
                       var discoBitsCollection = new Backbone.Collection();
            (function() {
                
                function saveAllModified() {
                    var modifiedModels = discoBitsCollection.filter(function(model) {return model.hasChanged()})
                    var modified = new Backbone.Collection(modifiedModels);
                    alert("would save: " +modifiedModels+" models "+ JSON.stringify(modified.toJSON()))
                }
                
                var InfoBit = Backbone.Model.extend({
                    defaults: {
                        "@type": 'disco:XHTMLInfoDiscoBit',
                        "disco:infoBit": 'Some content'
                    },
                    initialize: function() {
                        console.log('This model has been initialized.');
                        var m = this;
                        this.on('change', function(msg) {
                            console.log('A value for this model has changed: ');
                            console.log(m.changed);
                            console.log(m.get("@id"));
                            console.log('A value for this model has changed: '+m.hasChanged(null));
                            m.foo = "bar"
                        });
                    }
                });

                var InfoBitView = Backbone.View.extend({
                    initialize: function() {
                        _.bindAll(this, 'save')
                        this.model.bind('save', this.save);
                        var infoBit = $(this.el).find('[property="disco:infoBit"]').html()
                        console.log(this.model.hasChanged(null))
                        //this.model.set("disco:infoBit", infoBit)
                        console.log(this.model.hasChanged(null))
                        this.stickit();
                    },
                    events: {
                        'mousedown .editable': 'editableClick'
                    },
                    bindings: {
                        '[property="disco:infoBit"]': 'disco:infoBit'
                    },
                    editableClick: etch.editableInit,
                    save: function() {

                        // normally you would call model.save() here but this is a demo
                        // $(this.el).find('.editable').effect('highlight', {color: 'yellow'});
                        // $('.save-event').fadeIn('fast', function() {
                        //     setTimeout($(this).fadeOut('slow'), 10000);
                        // });
                        console.log("this modified: ");
                        console.log(this.model.get("@id"));
                        console.log(this.model.changed);
                        console.log("this is modified: "+this.model.hasChanged(null));
                        saveAllModified();
                    }

                });

                //$article = $('[property="disco:infoBit"]');
                $('[property="disco:infoBit"]').addClass("editable")
                $('[property="disco:infoBit"]').attr("data-button-class", "all")
                //this ensure two way binding with stickit
                $('[property="disco:infoBit"]').attr("contenteditable", "true")
                $article = $('[typeof="disco:XHTMLInfoDiscoBit"]');
                _.forEach($article, function(art) {
                    console.log(art);
                    var infoBit = $(art).find('[property="disco:infoBit"]').html()
                    var about = $(art).attr('about')
                    var model = new InfoBit({
                        "@id": about,
                        "disco:infoBit": infoBit
                    });
                    new InfoBitView({model: model, el: art, tagName: art.tagName});
                    discoBitsCollection.add(model)
                });
                //var view = new articleView({model: model, el: $article[0], tagName: $article[0].tagName});

            })()

            Backbone.on('all', function(s) {
                console.log('Handling all: ' + s);
            });"""
        <html xmlns:disco="http://discobits.org/ontology#"> 
          {for (part <- res/DISCOBITS.contains;  if ((part/DISCOBITS.pos*) == "0")) yield
            <head>
                <link type="text/css" href="/style/style.css" rel="stylesheet" />
                <link rel="stylesheet" href="/tools/editor/styles/etch.css" />
                <title>Editing: {part/DISCOBITS.holds/DISCOBITS.infoBit*}</title>
                
            </head>
          }
          <body>
            {render(res, "rdfa-naked")}
            <script src="/tools/editor/scripts/jquery.min.js"></script>
            <script src="/tools/editor/scripts/underscore-min.js"></script>
            <script src="/tools/editor/scripts/backbone-min.js"></script>
            <script src="/tools/editor/scripts/etch.js"></script>
            <script src="/tools/editor/scripts/backbone.stickit.js"></script>
            <script>
              {Unparsed(initScript)}
            </script>
          </body>
        </html>
      }
    }
  }

}