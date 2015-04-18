function getGraphFromTurtle(callback) {
    function getTurtleSections() {
        var result = [];
        var i = -1;
        var scripts = document.getElementsByTagNameNS("http://www.w3.org/1999/xhtml", "script");
        for (var i = 0; i < scripts.length; i++) {
            if (scripts[i] && (scripts[i].getAttribute("type") === "text/turtle")) {
                result.push(scripts[i].text.toString());
            }
        }
        return result;
    }

    function parseMultiple(turtleStrings, callback) {
        var graph = rdf.createGraph();
        var i = -1;
        function processScript() {
            i++;
            if (i === turtleStrings.length) {
                callback(graph);
            }
            var currentTurtle = turtleStrings[i];
            //console.log('parsing ' + currentTurtle);
            rdf.parseTurtle(currentTurtle, function (g) {
                //console.log('parsed ' + g.toString());
                graph.addAll(g);
                processScript();
            });
        }
        processScript();
    }
    var turtleSections = getTurtleSections();
    parseMultiple(turtleSections, callback);

}

var discoBitsCollection = new Backbone.Collection();
$(function () {

    getGraphFromTurtle(function (origGraph) {
        var InfoBit = Backbone.Model.extend({
            defaults: {
                "@type": 'disco:XHTMLInfoDiscoBit',
                "disco:infoBit": 'Some content'
            },
            initialize: function () {
                console.log('This model has been initialized.');
                var m = this;
                this.on('change', function (msg) {
                    console.log('A value for this model has changed: ');
                    console.log(m.changed);
                    console.log(m.get("@id"));
                    console.log('A value for this model has changed: ' + m.hasChanged(null));
                    m.foo = "bar"
                });
            }
        });
        function saveAllModified() {
            GreenRdfStore.getGraph(document, function (newGraph) {
                //alert("orig: "+origGraph.toNT());
                //alert("new: "+ newGraph.toNT());
                rdf.serializeTurtle(origGraph, function (origTurtle) {
                    rdf.serializeTurtle(newGraph, function (newTurtle) {
                        $.post("/tools/editor/post", {assert: newTurtle, revoke: origTurtle, rdfFormat: 'text/turtle'}, function (data) {
                            alert("saved");
                            origGraph = newGraph;
                        }).fail(function (data) {
                            errdata = data
                            alert("error: " + data.statusText);
                        });
                    });
                });

            });

        }
        var InfoBitView = Backbone.View.extend({
            initialize: function () {
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
            save: function () {

                // normally you would call model.save() here but this is a demo
                // $(this.el).find('.editable').effect('highlight', {color: 'yellow'});
                // $('.save-event').fadeIn('fast', function() {
                //     setTimeout($(this).fadeOut('slow'), 10000);
                // });
                console.log("this modified: ");
                console.log(this.model.get("@id"));
                console.log(this.model.changed);
                console.log("this is modified: " + this.model.hasChanged(null));
                saveAllModified();
            }

        });
        //$article = $('[property="disco:infoBit"]');
        $('[property="disco:infoBit"]').addClass("editable")
        $('[property="disco:infoBit"]').attr("data-button-class", "all")
        //this ensure two way binding with stickit
        $('[property="disco:infoBit"]').attr("contenteditable", "true")
        $('[property="disco:infoBit"]').children().attr("contenteditable", "true")
        $article = $('[typeof="disco:XHTMLInfoDiscoBit"]');
        _.forEach($article, function (art) {
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

Backbone.on('all', function (s) {
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


 