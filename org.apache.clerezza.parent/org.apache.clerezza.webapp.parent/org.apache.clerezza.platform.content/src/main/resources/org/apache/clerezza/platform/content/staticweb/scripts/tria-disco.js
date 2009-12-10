function TriaDisco() {
	var graphUri = null;
}


TriaDisco.putData = function(rdfSymbol, store, previousStore, noContainerCreation) {
    var xhr = Util.XMLHTTPFactory();
    var postUrl = "post?resource="+rdfSymbol.uri;
	if (TriaDisco.graphUri != null) {
		postUrl += "&graph="+TriaDisco.graphUri
	}
    xhr.open('POST', postUrl, false);
    xhr.setRequestHeader("Content-Type", "application/rdf+xml");
    var assertedRDF = new XMLSerializer().serializeToString(RDFXMLSerializer.serialize(store, ""));
    xhr.setRequestHeader("Content-length", assertedRDF.length);
    xhr.send(assertedRDF);
    if (xhr.status >= 300) {
        alert(xhr.status+" " +xhr.statusText);
        throw new Error(xhr.status+" " +xhr.statusText);
    }
            
}

WidgetFactory.createURIderefURL = function(uri) {
	var getUrl = "get?resource="+uri;
	if (TriaDisco.graphUri != null) {
		getUrl += "&graph="+TriaDisco.graphUri
	}
	return getUrl;
}