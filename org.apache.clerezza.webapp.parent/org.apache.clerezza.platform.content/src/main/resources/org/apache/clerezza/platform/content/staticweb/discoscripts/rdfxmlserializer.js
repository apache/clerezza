function RDFXMLSerializer() {
}


RDFXMLSerializer.serialize = function(rdfFormula, baseURL) {
	var statements = rdfFormula.statements;
	var result = document.implementation.createDocument("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF", null);
	var root = result.firstChild;
	var elementMap = new Object();
	for (var i = 0; i < statements.length; i++) {
		RDFXMLSerializer.addStatement(statements[i], result, root, baseURL, elementMap);
	}
	root.appendChild(result.createTextNode("\n"));
	for (var key in elementMap) {
		elementMap[key].appendChild(result.createTextNode("\n\t"));
	}
	//alert(new XMLSerializer().serializeToString(result));
	return result;
}

RDFXMLSerializer.addStatement = function(statement, result, root, baseURL, elementMap) {
	
	var elem = result.createElementNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:Description");
	var elemId;
	if (statement.subject.termType == "symbol") {
		elemId = statement.subject.uri;
		elem.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:about", RDFXMLSerializer.getRelativePath(statement.subject.uri, baseURL));
	} else {
		elemId = statement.subject.id;
		elem.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:nodeID", "a"+statement.subject.id);
	}
	if (elementMap[elemId]) {
		elem = elementMap[elemId];
	} else {
		elementMap[elemId] = elem;
		root.appendChild(result.createTextNode("\n\t"));
		root.appendChild(elem);
	}
	elem.appendChild(result.createTextNode("\n"));
	elem.appendChild(result.createTextNode("\t\t"));
	var splittedURI = RDFXMLSerializer.splitURI(statement.predicate.uri);
	var propertyElem = result.createElementNS(splittedURI.ns, splittedURI.name);
	elem.appendChild(propertyElem);
	if (statement.object.termType == "symbol") {
		propertyElem.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:resource", RDFXMLSerializer.getRelativePath(statement.object.uri, baseURL));
	} else {
		if (statement.object.termType == "literal") {
			//note supports hacked xml-literals, other types are not in the store
			if (statement.object.elementValue) {
				var nodes = statement.object.elementValue.childNodes;
				for (var i = 0; i < nodes.length; i ++) {
					propertyElem.appendChild(nodes[i].cloneNode(true));
				}
				propertyElem.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:parseType", "Literal");
			} else {
				var textNode = result.createTextNode(statement.object.value);
				propertyElem.appendChild(textNode);
				if (statement.object.lang) {
					propertyElem.setAttribute("xml:lang", statement.object.lang);
				}
			}
		} else {
			propertyElem.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf:nodeID", "a"+statement.object.id);
		}
	}
}

RDFXMLSerializer.splitURI = function(uri) {
	var poundPos = uri.indexOf('#');
	var splitPos; 
	if (poundPos > -1) {
		splitPos = poundPos;
	} else {
		splitPos = uri.lastIndexOf('/');
	}
	var result = new Object();
	result.ns = uri.substring(0, splitPos+1);
	result.name = uri.substring(splitPos+1);
	return result;
	
}

RDFXMLSerializer.getRelativePath = function(url, contextURL) {
	if (!contextURL) {
		return url;
	}
	var contextCollection = contextURL.substring(0, contextURL.lastIndexOf('/'));
	var contextualisation =  RDFXMLSerializer.getRelativePathToCollection(url, contextCollection);
	if (contextualisation) {
		return contextualisation;
	} else {
		return url;
	}
}

RDFXMLSerializer.getRelativePathToCollection = function(url, contextCollection) {
	if (url.indexOf(contextCollection) == 0) {
		return url.substring(contextCollection.length+1);
	} else {
		if (contextCollection.match(/.*\/\/.*\.*\//)) {
			var contextCollection = contextCollection.substring(0, contextCollection.lastIndexOf('/'));
			var superContextualisation = RDFXMLSerializer.getRelativePathToCollection(url, contextCollection);
			if (superContextualisation) {
				return "../"+superContextualisation;
			}
		} 
		return null;
	}
}