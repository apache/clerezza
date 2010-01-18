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
 */
function ConceptFinder(){};

ConceptFinder.callback;

ConceptFinder.setAddConceptCallback = function (callback) {
	this.callback = callback;
}

ConceptFinder.search = function () {
	$("#system-suggested-concepts").empty();
	var searchTerm = $(":text[name=\"search-term\"]").val();
	if (searchTerm.length > 0) {
		var options = new AjaxOptions("find-concepts", "finding concepts", function(data) {
			var concepts = data.concepts;
			ConceptFinder.addSuggestions(concepts);
			$("#add-button-label")
				.html("<div>or</div><div>Add '"+searchTerm
					+"' as new Free Concept</div>");
			$("#system-suggested-concepts-area").show();
			$("#user-defined-concept-area").show();
		});
		options.url = "/concepts/find";
		options.data = "searchTerm=" + encodeURIComponent(searchTerm);
		options.dataType = "json";
		$.ajax(options);
	}
	return false;
}

ConceptFinder.addSuggestions = function (concepts) {
	var selectedConceptsExists = false;
	if (typeof(SelectedConcepts) != "undefined") {
		selectedConceptsExists = true;
	}
	for (var i = 0; i < concepts.length; i++) {
		if (!selectedConceptsExists || !SelectedConcepts.exists(concepts[i].uri)) {
			var div = $("<div/>").appendTo("#system-suggested-concepts");
			$("<div/>").text(concepts[i].prefLabel)
			.appendTo(div);
			$("<div/>").text(concepts[i].uri)
			.appendTo(div);
			$("<a/>").addClass("tx-icon tx-icon-plus add-suggested-concept")
			.attr({
				href: "#"
			})
			.text("Add")
			.click(function () {
				var searchTerm = $(this).prev().prev().text();
				var uri = $(this).prev().text();
				if (typeof(SelectedConcepts) != "undefined") {
					SelectedConcepts.addConcept(searchTerm, uri);
				}
				if (typeof(ConceptFinder.callback) == "function") {
					ConceptFinder.callback(searchTerm, uri);
				}
				$(this).parent().remove();
			})
			.appendTo(div);
			$("<br/>").appendTo(div);
			$("<br/>").appendTo(div);
		}
	}
}

function ConceptManipulator(){};

ConceptManipulator.callback;

ConceptManipulator.setAddConceptCallback = function (callback) {
	this.callback = callback;
}

ConceptManipulator.addConcept = function () {
	var searchTerm = $(":text[name='search-term']").val();
	if (searchTerm.length > 0) {
		var options = new AjaxOptions("add-concepts", "adding concepts", function(uri) {
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
		options.data = $("#suggestions-form").serialize();
		options.data = {"pref-label":searchTerm,
			lang:$(":input[name='lang']").val(),
			comment:$(":textarea[name='comment']").val()}

		$.ajax(options);
	}
	return false;
}

$(document).ready(function () {
	$("#go-button").click(function() {
		ConceptFinder.search();
	});
	$("#add-user-defined-concept").click(function() {
		ConceptManipulator.addConcept();
	});
	$("#system-suggested-concepts-area").hide();
	$("#user-defined-concept-area").hide();
});
