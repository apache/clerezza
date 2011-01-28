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
function SelectedConcepts(){};

SelectedConcepts.exists = function (concept) {
	var exists = false;
	$("input[name='concepts']").each( function () {
		if ($(this).val() == concept) {
			exists = true;
		}
	});
	return exists;
}

SelectedConcepts.addConcept = function (prefLabel, uri) {
	var div = $("<div/>");
	ConceptFinder.createConceptWidgets(prefLabel, uri).appendTo(div);
	SelectedConcepts.createDeleteButton(prefLabel, uri).appendTo(div);
	$("<br />").appendTo(div);
	$("<br />").appendTo(div);
	$("#selected-concepts").append(div);
}

/**
 * Creates a button that allows to delete a concept.
 */
SelectedConcepts.createDeleteButton = function(prefLabel, uri) {
	var deleteLink = $("<a/>").attr("href", "#");
	deleteLink.addClass("tx-icon tx-icon-delete").text("Delete");
	deleteLink.click(function() {
		deleteLink.parent().remove();
		ConceptFinder.createSystemFoundConcept(prefLabel, uri)
	});
	return deleteLink;
}


$(document).ready(function () {
	$(".tx-icon-delete").click(function() {
		$(this).parent().remove();
	});
});
