/*
 * functions to read RDFa to an rdfstore-js graph
 */

function GreenRdfStore() {
}

GreenRdfStore.getGraph = function(element, callback) {
    var store = rdfstore.create();
    store.graph(function(success,graph) {
        var env = store.rdf;
        CallbackProcessor.prototype = new RDFaProcessor();
        CallbackProcessor.prototype.constructor=RDFaProcessor;
        function CallbackProcessor() {
           RDFaProcessor.call(this);
        }
        var bNodeTable = {};
        CallbackProcessor.prototype.addTriple = function(origin,subject,predicate,object) {
            function convertNonLiteral(node) { 
                if (node.startsWith("_:")) {
                    if (!bNodeTable[node]) {
                        bNodeTable[node] = env.createBlankNode(node);
                    }
                    return bNodeTable[node];
                } else {
                    return env.createNamedNode(node);s
                }
            }
            function serializeNodeList(nodeList) {
                //code taken from Greenturtle's GraphRDFaProcessor
                var serializer = new XMLSerializer();
                var value = "";
                for (var x=0; x<nodeList.length; x++) {
                   if (nodeList[x].nodeType==Node.ELEMENT_NODE) {
                      var prefixMap = RDFaPredicate.getPrefixMap(nodeList[x]);
                      var prefixes = [];
                      for (var prefix in prefixMap) {
                         prefixes.push(prefix);
                      }
                      prefixes.sort();
                      var e = nodeList[x].cloneNode(true);
                      for (var p=0; p<prefixes.length; p++) {
                         e.setAttributeNS("http://www.w3.org/2000/xmlns/",prefixes[p].length==0 ? "xmlns" : "xmlns:"+prefixes[p],prefixMap[prefixes[p]]);
                      }
                      value += serializer.serializeToString(e);
                   } else if (nodeList[x].nodeType==Node.TEXT_NODE) {
                      value += nodeList[x].nodeValue;
                   }
                }
                return value;
            }
            //alert("New triple: "+subject+", predicate "+predicate+", object "+object.value+", "+object.language+", "+object.type);
            var subjectRS = convertNonLiteral(subject);
            
            var predicateRS = env.createNamedNode(predicate);
            if (object.type === "http://www.w3.org/1999/02/22-rdf-syntax-ns#object") {
                var objectRS = convertNonLiteral(object.value);
            } else {
                if (origin.getAttribute("content")) {
                    //according to the spec this attribute should be ignored for xmlLiterals, we don't
                    var value = origin.getAttribute("content");
                } else {
                    if (object.value.length) {
                        var value = serializeNodeList(object.value);
                    } else {
                        var value = object.value;
                    }
                }
                var objectRS = env.createLiteral(value.toString(), object.language, object.type);
            }
            graph.add(env.createTriple(subjectRS, predicateRS, objectRS));
        };
        var gp = new CallbackProcessor();
                    //gp.target.graph = new RDFaGraph();
        gp.process(element);
        callback(graph)
    });
};