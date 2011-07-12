/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/

// These are the classes corresponding to the RDF and N3 data models
//
// Designed to look like rdflib and cwm designs.
//
// Issues: Should the names start with RDF to make them
//      unique as program-wide symbols?
//
// W3C open source licence 2005.
//

RDFTracking = 0  // Are we requiring reasons for statements?

//takes in an object and makes it an object if it's a literal
function makeTerm(val) {
    //  fyi("Making term from " + val)
    if (typeof val == 'object') return val;
    if (typeof val == 'string') return new RDFLiteral(val);
    if (typeof val == 'undefined') return undefined;
    alert("Can't make term from " + val + " of type " + typeof val) // @@ add numbers
}



//	Symbol

function RDFEmpty() {
	return this;
}
RDFEmpty.prototype.termType = 'empty'
RDFEmpty.prototype.toString = function () { return "" }
RDFEmpty.prototype.toNT = function () { return "" }

function RDFSymbol_toNT(x) {
    return ("<" + x.uri + ">")
}

function toNT() {
    return RDFSymbol_toNT(this)
}

function RDFSymbol(uri) {
    this.uri = uri
    return this
}
	
RDFSymbol.prototype.termType = 'symbol'
RDFSymbol.prototype.toString = toNT
RDFSymbol.prototype.toNT = toNT


//	Blank Node

var RDFNextId = 0;  // Gobal genid
RDFGenidPrefix = "genid:"
NTAnonymousNodePrefix = "_:n"

function RDFBlankNode(id) {
    /*if (id)
    	this.id = id;
    else*/
    this.id = RDFNextId++
    return this
}

RDFBlankNode.prototype.termType = 'bnode'

RDFBlankNode.prototype.toNT = function() {
    return NTAnonymousNodePrefix + this.id
}
RDFBlankNode.prototype.toString = RDFBlankNode.prototype.toNT  

//	Literal

//LiteralSmush = []

function RDFLiteral(value, lang, datatype) {
	if (typeof value == 'object') {	
		var serializer = new XMLSerializer();
		this.value = serializer.serializeToString(value);
		this.elementValue = value.cloneNode(true); //cloning so taht literal doesn't become mutable
	} else {
		//console.debug("value: " +value+" type "+ typeof value);
    	this.value = value.toString();
    }
    this.lang=lang;	  // string
    this.datatype=datatype;  // term
    this.toString = RDFLiteralToString
    this.toNT = RDFLiteral_toNT
    //if (LiteralSmush[this.toNT()]) return LiteralSmush[this.toNT()];
    //else LiteralSmush[this.toNT()]=this;
    return this
}

RDFLiteral.prototype.termType = 'literal'

function RDFLiteral_toNT() {
    var str = this.value
    if (typeof str != 'string') {
	throw Error("Value of RDF literal is not string: "+str)
    }
    str = str.replace(/\\/g, '\\\\');  // escape
    str = str.replace(/\"/g, '\\"');
    str = '"' + str + '"'

    if (this.datatype){
    //alert(this.datatype.termType+"   "+typeof this.datatype)
	str = str + '^^' + this.datatype//.toNT()
    }
    if (this.lang) {
	str = str + "@" + this.lang
    }
    return str
}

function RDFLiteralToString() {
    return this.value
}
    
RDFLiteral.prototype.toString = RDFLiteralToString   
RDFLiteral.prototype.toNT = RDFLiteral_toNT

function RDFCollection() {
    this.id = RDFNextId++
    this.elements = []
    this.closed = false
}

RDFCollection.prototype.termType = 'collection'

RDFCollection.prototype.toNT = function() {
    return NTAnonymousNodePrefix + this.id
}
RDFCollection.prototype.toString = RDFCollection.prototype.toNT 

RDFCollection.prototype.append = function (el) {
    this.elements.push(el)
}

RDFCollection.prototype.close = function () {
    this.closed = true
}

//	Statement
//
//  This is a triple with an optional reason.
//
//   The reason can point to provenece or inference
//
function RDFStatement_toNT() {
    return (this.subject.toNT() + " "
	    + this.predicate.toNT() + " "
	    +  this.object.toNT() +" .")
}

function RDFStatement(subject, predicate, object, why) {
    this.subject = makeTerm(subject)
    this.predicate = makeTerm(predicate)
    this.object = makeTerm(object)
    if (typeof why !='undefined') {
	this.why = why
    } else if (RDFTracking) {
	fyi("WARNING: No reason on "+subject+" "+predicate+" "+object)
    }
    return this
}

RDFStatement.prototype.toNT = RDFStatement_toNT
RDFStatement.prototype.toString = RDFStatement_toNT
	

//	Formula
//
//	Set of statements.

function RDFFormula() {
    this.statements = []
    this.constraints = []
    this.initBindings = []
    this.optional = []
    return this
}

/*function RDFQueryFormula() {
	this.statements = []
	this.constraints = []
	this.initBindings = []
	this.optional = []
	return this
}*/

function RDFFormula_toNT() {
    return "{\n" + this.statements.join('\n') + "}"
}

//RDFQueryFormula.prototype = new RDFFormula()
//RDFQueryFormula.termType = 'queryFormula'
RDFFormula.prototype.termType = 'formula'
RDFFormula.prototype.toNT = RDFFormula_toNT
RDFFormula.prototype.toString = RDFFormula_toNT   

RDFFormula.prototype.add = function(subj, pred, obj, why) {
    this.statements.push(new RDFStatement(subj, pred, obj, why))
}

// Convenience methods on a formula allow the creation of new RDF terms:

RDFFormula.prototype.sym = function(uri,name) {
    if (name != null) {
	uri = this.namespaces[uri] + name
    }
    return new RDFSymbol(uri)
}

RDFFormula.prototype.literal = function(val, lang, dt) {
    return new RDFLiteral(val, lang, dt)
}

RDFFormula.prototype.bnode = function(id) {
    return new RDFBlankNode(id)
}

RDFFormula.prototype.formula = function() {
    return new RDFFormula()
}

RDFFormula.prototype.collection = function () {
    return new RDFCollection()
}


/*RDFFormula.prototype.queryFormula = function() {
	return new RDFQueryFormula()
}*/

RDFVariableBase = "varid:"; // We deem variabe x to be the symbol varid:x 

//An RDFVariable is a type of s/p/o that's not literal. All it holds is it's URI.
//It has type 'variable', and a function toNT that turns it into NTriple form
function RDFVariable(rel) {
    this.uri = URIjoin(rel, RDFVariableBase);
    return this;
}

RDFVariable.prototype.termType = 'variable';
RDFVariable.prototype.toNT = function() {
    if (this.uri.slice(0, RDFVariableBase.length) == RDFVariableBase) {
	return '?'+ this.uri.slice(RDFVariableBase.length);} // @@ poor man's refTo
    return '?' + this.uri;
};

RDFVariable.prototype.toString = RDFVariable.prototype.toNT;
RDFVariable.prototype.classOrder = 7;

RDFFormula.prototype.variable = function(name) {
    return new RDFVariable(name);
};

RDFVariable.prototype.hashString = RDFVariable.prototype.toNT;


// The namespace function generator 

function Namespace(nsuri) {
    return function(ln) { return new RDFSymbol(nsuri+ln) }
}

// Parse a single token
//
// The bnode bit should not be used on program-external values; designed
// for internal work such as storing a bnode id in an HTML attribute.
// Not coded for literals.

RDFFormula.prototype.fromNT = function(str) {
    var len = str.length
    var ch = str.slice(0,1)
    if (ch == '<') return this.sym(str.slice(1,len-1))
    if (ch == '_') {
	var x = new RDFBlankNode()
	x.id = parseInt(str.slice(3))
	RDFNextId--
	return x
    }
    alert("Can't yet convert from NT: '"+str+"', "+str[0])
}

// ends
