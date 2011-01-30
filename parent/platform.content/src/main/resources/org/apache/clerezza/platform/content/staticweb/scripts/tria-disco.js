function TriaDisco() {
	var graphUri = null;
}


TriaDisco.putData = function(rdfSymbol, store, previousStore, noContainerCreation) {
    var xhr = Util.XMLHTTPFactory();
    var postUrl = "post";
	if (TriaDisco.graphUri != null) {
		postUrl += "?graph="+TriaDisco.graphUri
	}
    xhr.open('POST', postUrl, false);

	var assertedRDF = new XMLSerializer().serializeToString(RDFXMLSerializer.serialize(store, ""));
	var revokedRDF = new XMLSerializer().serializeToString(RDFXMLSerializer.serialize(previousStore, ""));
	var parameters = "assert="+encodeURIComponent(assertedRDF);
	parameters += "&revoke="+encodeURIComponent(revokedRDF);
	xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xhr.setRequestHeader("Content-length", parameters.length);
	//xhr.setRequestHeader("Connection", "close");
	xhr.send(parameters);
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