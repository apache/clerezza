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

//  Identity management for RDF
//
// This file sees that things which are identical 
// according to owl:sameAs or an owl:InverseFunctionalProperty
// or an owl:FunctionalProperty
//
// Missing: Equating predicates will not propagate these actions
//

// String for hashing. The literal one maybe is slow?

/*jsl:option explicit*/ // Turn on JavaScriptLint variable declaration checking

owl_ns = "http://www.w3.org/2002/07/owl#";
link_ns = "http://www.w3.org/2006/link#";

/* hashString functions are used as array indeces. This
** is done to avoid conflict with existing properties of arrays such as length and map.
** See issue 139.
*/
RDFLiteral.prototype.hashString = RDFLiteral.prototype.toNT;
RDFSymbol.prototype.hashString = RDFSymbol.prototype.toNT;
RDFBlankNode.prototype.hashString = RDFBlankNode.prototype.toNT;
RDFCollection.prototype.hashString = RDFCollection.prototype.toNT;

RDFIndexedFormula.prototype = new RDFFormula();
RDFIndexedFormula.prototype.constructor = RDFIndexedFormula;
// RDFIndexedFormula.superclass = RDFFormula.prototype;

RDFArrayRemove = function(a, x) {  //removes all elements equal to x from a
    var i;
    for(i=0; i<a.length; i++) {
	if (a[i] == x) {
            a.splice(i,1);
            return;
	}
    }
    alert("RDFArrayRemove: Array did not contain "+x);
};

//Stores an associative array that maps URIs to functions
function RDFIndexedFormula() {
    this.statements = [];    // As in RDFFormula
    this.propertyAction = []; // What to do when getting statement with {s X o}
    //maps <uri> to f(F,s,p,o)
    this.classAction = [];   // What to do when adding { s type X }
    this.redirection = [];   // redirect to lexically smaller equivalent symbol
    this.subjectIndex = [];  // Array of statements with this X as subject
    this.predicateIndex = [];  // Array of statements with this X as subject
    this.objectIndex = [];  // Array of statements with this X as object
    this.namespaces = {} // Dictionary of namespace prefixes

    // Callbackify?
    
    function handleRDFType(formula, subj, pred, obj, why) {
        if (typeof formula.typeCallback != 'undefined')
            formula.typeCallback(formula, obj, why);

        var x = formula.classAction[obj.hashString()];
        if (x) return x(formula, subj, pred, obj);
        return false; // statement given is needed
    } //handleRDFType

    //If the predicate is #type, use handleRDFType to create a typeCallback on the object
    this.propertyAction[
	'<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>'] = handleRDFType;

    // Assumption: these terms are not redirected @@fixme
    this.propertyAction['<http://www.w3.org/2002/07/owl#sameAs>'] =
	function(formula, subj, pred, obj, why) {
            formula.equate(subj,obj);
            return false; // statement given is needed e.g. when dereferencing
	}; //sameAs -> equate & don't add to index
    
    this.propertyAction['<http://www.w3.org/2006/link#obsoletes>'] =
	function(formula, subj, pred, obj, why) {
            formula.replaceWith(obj, subj);
            return false; // statement given IS needed
	}; //sameAs -> equate & don't add to index
    
    function newPropertyAction(formula, pred, action) {
	fyi("newPropertyAction:  "+pred);
        formula.propertyAction[pred] = action;
	var fixEndA = formula.statementsMatching(undefined, pred, undefined);
	var i;
	for (i=0; i<fixEndA.length; i++) { // NOT optimized - sort fixEndA etc
//	    fyi("nePropertyAction: retrospective action for "+st)
	    if (action(formula, fixEndA[i].subject, pred, fixEndA[i].object)) {
//@@		kb.remove(st); /// messing up the list we are iterating over?? @@
		fyi("newPropertyAction: NOT removing "+fixEndA[i]);
	    }
	}
	return false;
    }

    this.classAction["<"+owl_ns+"InverseFunctionalProperty>"] =
	function(formula, subj, pred, obj, addFn) {
	    return newPropertyAction(formula, subj, handle_IFP); // yes subj not pred!
	}; //IFP -> handle_IFP, do add to index

    this.classAction["<"+owl_ns+"FunctionalProperty>"] =
	function(formula, subj, proj, obj, addFn) {
	    return newPropertyAction(formula, subj, handle_FP);
	}; //FP => handleFP, do add to index

    function handle_IFP(formula, subj, pred, obj)  {
        var s1 = formula.any(undefined, pred, obj);
        if (typeof s1 == 'undefined') return false; // First time with this value
        formula.equate(s1, subj);
        return true;
    } //handle_IFP

    function handle_FP(formula, subj, pred, obj)  {
        var o1 = formula.any(subj, pred, undefined);
        if (typeof o1 == 'undefined') return false; // First time with this value
        formula.equate(o1, obj);
        return true ;
    } //handle_FP
    
} /* end RDFIndexedFormula */


RDFIndexedFormula.prototype.register = function(prefix, nsuri) {
    this.namespaces[prefix] = nsuri
}


/** simplify graph in store when we realize two identifiers are equal

We replace the bigger with the smaller.

*/
RDFIndexedFormula.prototype.equate = function(u1, u2) {
    // fyi("Equating "+s+" and "+o)
    var d = u1.compareTerm(u2);
    if (!d) return true; // No information in {a = a}
    var big, small;
    if (d < 0)  {  // u1 less than u2
	return this.replaceWith(u2, u1);
    } else {
	return this.replaceWith(u1, u2);
    }
}

// Replace big with small, obsoleted with obsoleting.
//
RDFIndexedFormula.prototype.replaceWith = function(big, small) {

    var i, matches, fixEndA, hash;
    fixEndA = this.statementsMatching(big, undefined, undefined);
    matches = fixEndA.length;

    for (i=0; i<matches; i++) {
        fixEndA[i].subject = small;
	hash = small.hashString();
        if (typeof this.subjectIndex[hash] == 'undefined')
                this.subjectIndex[hash] = [];
        this.subjectIndex[hash].push(fixEndA[i]);
    }
    delete this.subjectIndex[big.hashString()];

    // If we allow equating predicates we must index them.
    fixEndA = this.statementsMatching(undefined, big, undefined);
    matches = fixEndA.length;
    
    for (i=0; i<matches; i++) {
	fixEndA[i].predicate = small;
	hash = small.hashString();
	if (typeof this.predicateIndex[hash] == 'undefined')
	    this.predicateIndex[hash] = [];
	this.predicateIndex[hash].push(fixEndA[i]);
    }
    delete this.predicateIndex[big.hashString()];
    
    fixEndA = this.statementsMatching(undefined, undefined, big);
    matches = fixEndA.length;

    for (i=0; i<matches; i++) {
        //  RDFArrayRemove(this.objectIndex[big], st)
        fixEndA[i].object = small;
	hash = small.hashString()
        if (typeof this.objectIndex[hash] == 'undefined')
                this.objectIndex[hash] = [];
        this.objectIndex[hash].push(fixEndA[i]);
    }
    delete this.objectIndex[big.hashString()];
    
    this.redirection[big.hashString()] = small;

    /* merge actions @@ assumes never > 1 action*/
    var action = this.classAction[big.hashString()];
    if ((typeof action != 'undefined') &&
	(typeof this.classAction[small.hashString()] == 'undefined')) {
	    this.classAction[small.hashString()] = action;
    }
    
    action = this.propertyAction[big.hashString()];
    if ((typeof action != 'undefined') &&
	(typeof this.propertyAction[small.hashString()] == 'undefined')) {
	    this.propertyAction[small.hashString()] = action;
	    alert("copying action on "+big+" to "+small)
    }
    
    fyi("Equate done. "+big+" to be known as "+small)    
    return false;
};


// On input parameters, do redirection and convert constants to terms
// We do not redirect literals
function RDFMakeTerm(formula,val) {
    if (typeof val != 'object') {   
	if (typeof val == 'string') {
	    return new RDFLiteral(val);
	} else if (typeof val == 'undefined') {
	    return undefined;
	} else {   // @@ add converting of dates and numbers
	    alert("Can't make term from " + val + " of type " + typeof val); 
	}
    }
    if (typeof formula.redirection == 'undefined') 
	alert('formula: '+ formula+', term: '+val);
    var y = formula.redirection[val.hashString()];
    if (typeof y == 'undefined') return val;
//    fyi(" redirecting "+val+" to "+y)
    return y;
}

// add a triple to the store
RDFIndexedFormula.prototype.add = function(subj, pred, obj, why) {
    var action, st, hashS, hashP, hashO;
    subj = RDFMakeTerm(this, subj);
    pred = RDFMakeTerm(this, pred);
    obj = RDFMakeTerm(this, obj);
    why = RDFMakeTerm(this, why);
    
    var hashS = subj.hashString();
    var hashP = pred.hashString();
    var hashO = obj.hashString();
    
    // Check we don't already know it -- esp when working with dbview
    st = this.anyStatementMatching(subj,pred,obj) // @@@@@@@ temp fix <====WATCH OUT!
    if (typeof st != 'undefined') return; // already in store
    //    fyi("\nActions for "+s+" "+p+" "+o+". size="+this.statements.length)
    if (typeof this.predicateCallback != 'undefined')
	this.predicateCallback(this, pred, why);
	
    // Action return true if the statement does not need to be added
    action = this.propertyAction[hashP];
    if (action && action(this, subj, pred, obj, why)) return;
    
    st = new RDFStatement(subj, pred, obj, why);
    if (typeof this.subjectIndex[hashS] =='undefined') this.subjectIndex[hashS] = [];
    this.subjectIndex[hashS].push(st); // Set of things with this as subject
    
    if (typeof this.predicateIndex[hashP] =='undefined') this.predicateIndex[hashP] = [];
    this.predicateIndex[hashP].push(st); // Set of things with this as subject
    
    if (typeof this.objectIndex[hashO] == 'undefined') this.objectIndex[hashO] = [];
    this.objectIndex[hashO].push(st); // Set of things with this as object
    
    this.statements.push(st);
}; //add


RDFIndexedFormula.prototype.anyStatementMatching = function(subj,pred,obj,why) {
    var x = this.statementsMatching(subj,pred,obj,why,true);
    if (!x || x == []) return undefined;
    return x[0];
};

// return statements matching a pattern
// ALL CONVENIENCE LOOKUP FUNCTIONS RELY ON THIS!
RDFIndexedFormula.prototype.statementsMatching = function(subj,pred,obj,why,justOne) {
    var results = [];
    var candidates;
    fyi("\nMatching {"+subj+" "+pred+" "+obj+"}");
    subj = RDFMakeTerm(this, subj);
    pred = RDFMakeTerm(this, pred);
    obj = RDFMakeTerm(this, obj);
    why = RDFMakeTerm(this, why);

    if (typeof(pred) != 'undefined' && this.redirection[pred.hashString()])
	pred = this.redirection[pred.hashString()];

    if (typeof(obj) != 'undefined' && this.redirection[obj.hashString()])
	obj = this.redirection[obj.hashString()];

	//looks for candidate statements matching a given s/p/o
    if (typeof(subj) =='undefined') {
        if (typeof(obj) =='undefined') {
	    if (typeof(pred) == 'undefined') { 
		candidates = this.statements; //all wildcards
	    } else {
		candidates = this.predicateIndex[pred.hashString()];
//		fyi("@@Trying predciate "+p+" length "+candidates.length)

		if (typeof candidates == 'undefined') return [];
		fyi("Trying predicate "+pred+" index of: "+candidates.length +
			    " for {"+subj+" "+pred+" "+obj+" @ "+why+"}");
	    }
    //      fyi("Trying all "+candidates.length+" statements")
        } else { // subj undefined, obj defined
            candidates = this.objectIndex[obj.hashString()];
            if (typeof candidates == 'undefined') return [];
            if ((typeof pred == 'undefined') &&
		(typeof why == 'undefined')) {
                // fyi("Returning all statements for object")
                return candidates ;
            }
            // fyi("Trying only "+candidates.length+" object statements")
        }
    } else {  // s defined
        if (this.redirection[subj.hashString()])
            subj = this.redirection[subj.hashString()];
        candidates = this.subjectIndex[subj.hashString()];
        if (typeof candidates == 'undefined') return [];
        if (typeof(obj) =='undefined') {
	    if ((typeof pred == 'undefined') && (typeof why == 'undefined')) {
		// fyi("Trying all "+candidates.length+" subject statements")
		return candidates;
	    }
	} else { // s and o defined ... unusual in practice?
            var oix = this.objectIndex[obj.hashString()];
            if (typeof oix == 'undefined') return [];
	    if (oix.length < candidates.length) {
		candidates = oix;
//		fyi("Wow!  actually used object index instead of subj");
	    }
	
        }
        // fyi("Trying only "+candidates.length+" subject statements")
    }
    
    if (typeof candidates == 'undefined') return [];
//    fyi("Matching {"+s+" "+p+" "+o+"} against "+n+" stmts")
    var i;
    var st;
    for(i=0; i<candidates.length; i++) {
        st = candidates[i]; //for each candidate, match terms of candidate with input, then return all
        // fyi("  Matching against st=" + st +" why="+st.why);
        if (RDFTermMatch(pred, st.predicate) &&  // first as simplest
            RDFTermMatch(subj, st.subject) &&
            RDFTermMatch(obj, st.object) &&
            RDFTermMatch(why, st.why)) {
            // fyi("   Found: "+st)
            if (justOne) return [st];
            results.push(st);
        }
    }
    return results;
}; // statementsMatching


/** remove a particular statement from the bank **/
RDFIndexedFormula.prototype.remove = function (st) {
  fyi("entering remove w/ st=" + st);
  var subj = st.subject, pred = st.predicate, obj = st.object;
  if (typeof this.subjectIndex[subj.hashString()] == 'undefined') twarn ("statement not in sbj index: "+st);
  if (typeof this.predicateIndex[pred.hashString()] == 'undefined') twarn ("statement not in pred index: "+st);
  if (typeof this.objectIndex[obj.hashString()] == 'undefined') twarn ("statement not in obj index: " +st);

  RDFArrayRemove(this.subjectIndex[subj.hashString()], st);
  RDFArrayRemove(this.predicateIndex[pred.hashString()], st);
  RDFArrayRemove(this.objectIndex[obj.hashString()], st);
  RDFArrayRemove(this.statements, st);
}; //remove

/** remove all statements matching args (within limit) **/
RDFIndexedFormula.prototype.removeMany = function (subj, pred, obj, why, limit) {
  fyi("entering removeMany w/ subj,pred,obj,why,limit = " + subj +", "+ pred+", " + obj+", " + why+", " + limit);
  var statements = this.statementsMatching (subj, pred, obj, why, false);
  if (limit) statements = statements.slice(0, limit);
  for (var st in statements) this.remove(statements[st]);
}; //removeMany

// ends

