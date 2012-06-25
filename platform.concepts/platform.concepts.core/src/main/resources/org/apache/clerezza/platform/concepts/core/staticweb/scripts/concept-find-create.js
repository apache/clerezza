/*
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
 * @author hasan, andre
 */
function ConceptFinder(){
};

ConceptFinder.callback;

ConceptFinder.setAddConceptCallback = function(callback){
	this.callback = callback;
}

/**
 * Request the server concepts that match the search term in the input field "search-term"
 */
ConceptFinder.search = function(){
	$("#system-found-concepts").empty();
	var queryResult = null;
	var searchTerm = $(":text[name=\"search-term\"]").val();
	if (searchTerm.length > 0) {
		var options = new AjaxOptions("find-concepts", "finding concepts", function(data){
			var databank = $.rdf.databank();
			var store = databank.load(data, {});
			var query = $.rdf({
				databank: store
			});
			query.prefix('rdf', 'http://www.w3.org/1999/02/22-rdf-syntax-ns#');
			query.prefix('concepts', 'http://clerezza.org/2010/01/concepts#');
			query.prefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#');
			query.prefix('qr', 'http://clerezza.org/2010/04/queryresult#');
			var resultSet = query.where('?subject rdf:type qr:QueryResult');
			if (resultSet.size() != 1) {
				AlertMessage.show(function(){
				}, "The query results did not match what's expected: exactly 1 result");
			}
			var queryResult = query.node(resultSet.get(0).subject);
			var conceptProperty = $.rdf.resource("<http://clerezza.org/2010/04/queryresult#concept>");
			var conceptProperties = queryResult.get(conceptProperty);
			if (conceptProperties) {
				ConceptFinder.addFoundConcepts(conceptProperties);
			}
			$("#add-button-label").html("<div>Add '" + searchTerm + "' as new Free Concept</div>");
			$("#system-found-concepts-area").show();
			var newFreeConceptProperty = $.rdf.resource("<http://clerezza.org/2010/04/queryresult#creationOfNewFreeConceptSuggested>");
			var creationOfNewFreeConceptSuggested = queryResult.get(newFreeConceptProperty)[0].value.toString() == "true";
			if (creationOfNewFreeConceptSuggested) {
				$("#user-defined-concept-area").show();
			} else {
				$("#user-defined-concept-area").hide();
			}
		});
		options.url = "/concepts/find";
		options.data = "searchTerm=" + encodeURIComponent(searchTerm) + "&xPropObj=http://clerezza.org/2010/04/queryresult%23concept";
		options.dataType = "json";
		options.beforeSend = function(req){
			req.setRequestHeader("Accept", "application/rdf+json");
		};
		
		$.ajax(options);
	}
	return false;
}

/**
 * Adds the given (array of) concepts to the UI (that's currently being displayed). Displays them by a 
 * +-button and labels (for each of them)
 *
 * @param Json array of concepts
 */
ConceptFinder.addFoundConcepts = function(concepts){
	var selectedConceptsExists = false;
	if (typeof(SelectedConcepts) != "undefined") {
		selectedConceptsExists = true;
	}
	var added = false;
	for (var i = 0; i < concepts.length; i++) {
		var concept = concepts[i];
		if (!selectedConceptsExists || !SelectedConcepts.exists(concept.value)) {
			added = true;
			var prefLabelProperty = $.rdf.resource("<http://www.w3.org/2008/05/skos#prefLabel>"); 
			var prefLabel = concept.get(prefLabelProperty).value;
			var uri = concept.value;
			ConceptFinder.createSystemFoundConcept(prefLabel, uri);
		}
	}
	if (added) {
		$("#label-for-search-results").text("Concepts found:");
	}
	else {
		$("#label-for-search-results").text("No additional concepts found.");
	}
}

/**
 * Creates the widget that show the user a concept that was found on the backend. Shows prefLabel, 
 * uri and a button to add the concept to the backend
 * 
 * @param {Object} prefLabel
 * @param {Object} uri
 */
ConceptFinder.createSystemFoundConcept = function(prefLabel, uri) {
	var div = $("<div/>");
	ConceptFinder.createConceptWidgets(prefLabel, uri).appendTo(div)
	$("<a/>").addClass("tx-icon tx-icon-plus").attr({
		href: "#"
	}).text("Add").click(ConceptFinder.onAddClicked(prefLabel, uri)).appendTo(div);
	$("<br/>").appendTo(div);
	$("<br/>").appendTo(div);
	div.appendTo("#system-found-concepts")
}

ConceptFinder.onAddClicked = function(prefLabel, uri){
	return function(){
		if (typeof(SelectedConcepts) != "undefined") {
			SelectedConcepts.addConcept(prefLabel, uri);
		}
		if (typeof(ConceptFinder.callback) == "function") {
			ConceptFinder.callback(prefLabel, uri);
		}
		$(this).parent().remove();
	};
}

/**
 * Creates html elements that show the given pref label and uri. Appends them to the given div.
 * 
 * @param {Object} prefLabel
 * @param {Object} uri
 * @param {Object} div
 */
ConceptFinder.createConceptWidgets = function(prefLabel, uri) {
	var div = $("<div/>");
	if (prefLabel.substr(0,1) == "\"") {
		prefLabel = prefLabel.substring(1, prefLabel.length - 1);
	}
	$("<div/>").text("PrefLabel: " + prefLabel).appendTo(div);
	$("<div/>").text("Uri: " + uri).appendTo(div);
	$("<input/>").attr({
		"type": "hidden",
		"name": "concepts"
	}).val(uri).appendTo(div);
	return div;
}

/**
 * Checks if a concept with the given uri already exists
 * @param {Object} uri
 */
ConceptFinder.exists = function(uri){

};

function ConceptManipulator(){
};

ConceptManipulator.callback;

ConceptManipulator.setAddConceptCallback = function(callback){
	this.callback = callback;
}

/**
 * sends a new concept to the backend for addition. Uses the search term entered by the user (in the same form).
 */
ConceptManipulator.addConcept = function(){
	var searchTerm = $(":text[name='search-term']").val();
	if (searchTerm.length > 0) {
		var options = new AjaxOptions("add-concepts", "adding concepts", function(uri){
			$("#concept-description").val("");
			if (typeof(SelectedConcepts) != "undefined") {
				SelectedConcepts.addConcept(searchTerm, uri);
			}
			if (typeof(this.callback) == "function") {
				this.callback(searchTerm, uri);
			}
		});
		options.type = "POST";
		options.url = "/concepts/manipulator/add-concept";
		options.data = {
			"pref-label": searchTerm,
			lang: $(":input[name='lang']").val(),
			comment: $(":textarea[name='comment']").val()
		}
		
		$.ajax(options);
	}
	return false;
}

$(document).ready(function(){
	$("#go-button").click(function(){
		ConceptFinder.search();
	});
	$("#add-user-defined-concept").click(function(){
		ConceptManipulator.addConcept();
		$("#user-defined-concept-area").hide();
	});
	$("#system-found-concepts-area").hide();
	$("#user-defined-concept-area").hide();
});
