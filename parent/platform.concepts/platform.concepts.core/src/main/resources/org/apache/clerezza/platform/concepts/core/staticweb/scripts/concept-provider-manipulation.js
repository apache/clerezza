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
function ConceptProviderGui(){};

ConceptProviderGui.isModified = false;

ConceptProviderGui.query = "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n" +
	"PREFIX skos08: <http://www.w3.org/2008/05/skos#> \n" +
	"PREFIX skos04: <http://www.w3.org/2004/02/skos/core#> \n" +
	"CONSTRUCT { ?concept a skos08:Concept; skos08:prefLabel ?prefLabel; " +
		"rdfs:comment ?comment; owl:sameAs ?sameConcept . } \n" +
	"WHERE { ?concept a skos04:Concept; skos04:prefLabel ?prefLabel . \n" +
	"OPTIONAL { ?concept rdfs:comment ?comment . } \n" +
	"OPTIONAL { ?concept owl:sameAs ?sameConcept . } \n" +
	"FILTER (REGEX(STR(?prefLabel), '${searchTerm}', 'i'))}";

ConceptProviderGui.initButtons = function() {

	ConceptProviderGui.updateButtonState();

	$("#deleteButton").click(function(event) {
		$("input:checked").each(function() {
			$(this).parent().parent().remove();
		});
		$(this).addClass("tx-inactive");
		ConceptProviderGui.isModified = true;
	});

	$("#addProvider").click(function(event) {
		ConceptProviderGui.addProvider($("#type").val(),
					$("#conceptScheme").val(),
					$("#sparqlEndPoint").val(),
					$("#defaultGraph").val(),
					$("#query").val());
		$("#conceptScheme").val("");
		$("#sparqlEndPoint").val("");
		$("#defaultGraph").val("");
		$("#query").val("");
	});

	$("#addButton").click(function(event) {
		if($(this).text() == "Save") {
			var options = new AjaxOptions("update-providers", "updating providers", function(obj) {
			});
			$("textarea[id!=query]").each(function() {
				$(this).removeAttr("disabled");
			});
			options.type = "POST";
			options.url = "update-concept-provider-list";
			options.data = $("#providers").serialize();
			$.ajax(options);
		}
	});
}

ConceptProviderGui.addProvider = function(rdfType, conceptScheme, sparqlEndPoint, defaultGraph, query) {
	var tr = $("<tr/>").attr("id", "");
	var td1 = $("<td/>");
	var div = $("<div/>").text("Provider:");
	var td2 = $("<td/>");

	var selection = $("<select/>").attr("name","types");

	$("#type > option:not(:selected)").each(function() {
		selection.append($(this).clone());
	});
	$("#type > option:selected").each(function() {
		var option = $(this).clone();
		option.attr("selected", "selected");
		selection.append(option);
	});

	td2.append(div);
	td2.append(selection);
	
	var inputText = $("<input/>").attr({
				"type":"text",
				"name":"conceptSchemes",
				"value": conceptScheme
				});
	
	div = $("<div/>").text("Concept Scheme:");
	td2.append(div);
	td2.append(inputText);

	inputText = $("<input/>").attr({
				"type":"text",
				"name":"sparqlEndPoints",
				"value": sparqlEndPoint
				});

	div = $("<div/>").text("SPARQL End Point");
	td2.append(div);
	td2.append(inputText);

	inputText = $("<input/>").attr({
				"type":"text",
				"name":"defaultGraphs",
				"value": defaultGraph
				});

	div = $("<div/>").text("Default Graph:");
	td2.append(div);
	td2.append(inputText)
	var inputCheckbox = $("<input/>").attr({
				"type":"checkbox",
				"value": rdfType
				});

	inputCheckbox.appendTo(td1);

	td1.appendTo(tr);
	
	var textArea = $("<textarea/>").attr({
				"rows":"8",
				"name":"queryTemplates"
				}).val(query);
	div = $("<div/>").text("Query Template:");
	td2.append(div);
	td2.append(textArea);
	td2.append($("<br/>"));
	td2.append($("<br/>"));
	td2.appendTo(tr);
	tr.insertBefore("#last");
	ConceptProviderGui.isModified = true;

	$("select[name=types] > option:selected").each(function() {
		ConceptProviderGui.updateDropDown($(this), "conceptSchemes");
	});
}

ConceptProviderGui.updateDropDown = function (obj, name) {
	if(obj.val().indexOf("Local") != -1) {
		obj.parent().parent().find("input[name!=" + name + "]").each(function() {
			$(this).prev().hide();
			$(this).hide();
		});
		obj.parent().parent().find("input[name=" + name + "]").each(function() {
			$(this).prev().show();
			$(this).show();
		});
		obj.parent().parent().find("textarea").each(function() {
			$(this).prev().hide();
			$(this).hide();
		});
	} else {
		obj.parent().parent().find("input[name!=" + name + "]").each(function() {
			$(this).prev().show();
			$(this).show();
		});
		obj.parent().parent().find("input[name=" + name + "]").each(function() {
			$(this).prev().hide();
			$(this).hide();
		});
		obj.parent().parent().find("textarea").each(function() {
			$(this).prev().show();
			$(this).show();
		});
	}
}

ConceptProviderGui.updateButtonState = function() {
	var counter = $("input:checked").length;
	if(counter == 0) {
		$("#deleteButton").addClass("tx-inactive");
	} else {
		$("#deleteButton").removeClass("tx-inactive");
	}
}

$(document).ready(function () {

	$("select[name=types]").live("change", function() {
		$("select[name=types] > option:selected").each(function(){
			ConceptProviderGui.updateDropDown($(this), "conceptSchemes");
		});
	});
	$("select[name=types] > option:selected").each(function() {
		ConceptProviderGui.updateDropDown($(this), "conceptSchemes");
	});
	$("#type").live("change", function() {
		$("#type > option:selected").each(function(){
			ConceptProviderGui.updateDropDown($(this), "conceptScheme");
			$("#query").val(ConceptProviderGui.query);
		});
	});
	$("#type > option:selected").each(function() {
		ConceptProviderGui.updateDropDown($(this), "conceptScheme");
		$("#query").val(ConceptProviderGui.query);
	});
	$("input:checkbox").live("change",function() {
		ConceptProviderGui.updateButtonState();
	});

	ConceptProviderGui.initButtons();
});
