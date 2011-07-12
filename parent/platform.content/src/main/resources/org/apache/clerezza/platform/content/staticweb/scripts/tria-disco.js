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
