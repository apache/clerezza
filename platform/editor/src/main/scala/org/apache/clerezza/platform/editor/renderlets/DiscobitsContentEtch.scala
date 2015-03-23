package org.apache.clerezza.platform.editor

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
            var discoBitsCollection = new Backbone.Collection();
            $(function() {
                
                GreenRdfStore.getGraph(document, function(origGraph) {
                    //now that we have the orig graph we no longer need the content atr
                    $("span[property='disco:infoBit']").removeAttr("content");
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
                    function saveAllModified() {
                        GreenRdfStore.getGraph(document, function(newGraph) {
                            //alert("orig: "+origGraph.toNT());
                            //alert("new: "+ newGraph.toNT());
                            $.post( "/tools/editor/post", { assert: newGraph.toNT(), revoke: origGraph.toNT(), rdfFormat: 'text/turtle' }, function( data ) {
                                alert("saved");
                                origTurtle = newTurtle;
                              }) .fail(function( data) {
                                errdata = data
                                alert( "error: " + data.statusText);
                              });
                        });

                    }
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
                    
                    
                    
                });
                /*var store = rdfstore.create();
                
                    CallbackProcessor.prototype = new RDFaProcessor();
                    CallbackProcessor.prototype.constructor=RDFaProcessor;
                    function CallbackProcessor() {
                       RDFaProcessor.call(this);
                    }

                    CallbackProcessor.prototype.addTriple = function(origin,subject,predicate,object) {
                       alert("New triple: "+subject+", predicate "+predicate+", object "+object.value+", "+object.language+", "+object.type);
                       //RDFaProcessor.prototype.addTriple.call(this, origin, subject, predicate, object);
                       //graph.add(env.createTriple())
                    }*/
                    //var gp = new GraphRDFaProcessor();
                    /*function WrappedGraphProcessor() {
                        GraphRDFaProcessor.call(this)
                    }
                    WrappedGraphProcessor.prototype = new GraphRDFaProcessor();
                    WrappedGraphProcessor.prototype.addTriple= function(origin,subject,predicate,object) {
                            alert("uff "+origin+","+subject+","+predicate+","+object);
                            if (origin.getAttribute("content")) {
                                object.value = origin.getAttribute("content");
                            }
                            GraphRDFaProcessor.prototype.addTriple.call(this, origin, subject, predicate, object);
                        }
                    var gp = new WrappedGraphProcessor();
                    //var gp = new CallbackProcessor();
                    gp.target.graph = new RDFaGraph();
                    gp.process(document);
                    var origTurtle = gp.target.graph.toString();
                    alert(origTurtle);*/
                    
                            
                //});
                

                

                
                //var view = new articleView({model: model, el: $article[0], tagName: $article[0].tagName});

            });
        
            Backbone.on('all', function(s) {
                console.log('Handling all: ' + s);
            });
        
            
        
            /*document.addEventListener(
              "rdfa.loaded",
              function() {
                 _.forEach(document.getElementsByType("http://discobits.org/ontology#Entry"), function(e) {
                  $(e).css('background-color', 'blue');
                });
                console.log('all colored');
                //console.log('activating: '+RDFaProcessor);
                CallbackProcessor.prototype = new RDFaProcessor();
                CallbackProcessor.prototype.constructor=RDFaProcessor;
                function CallbackProcessor() {
                   RDFaProcessor.call(this);
                }

                CallbackProcessor.prototype.newSubjectOrigin = function(origin,subject) {
                   console.log("New origin for "+subject);
                }

                CallbackProcessor.prototype.addTriple = function(origin,subject,predicate,object) {
                   console.log("New triple: "+subject+", predicate "+predicate+
                               ", object "+object.value+", "+object.language+", "+object.type);
                }
                console.log('activated: '+CallbackProcessor);
                processor = new CallbackProcessor();
                processor.finishedHandlers.push(
                    function(node) {
                       alert("Done!");
                    }
                 );
                 processor.process(document);
                 console.log('done');
              },
              false
            );*/
            """
        val html = <html xmlns:disco="http://discobits.org/ontology#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"> 
          
            <head>
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
            <script src="/tools/editor/scripts/rdfstore.js" type="text/javascript"></script>
            <script src="/tools/editor/scripts/greenrdfstore.js"></script>
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