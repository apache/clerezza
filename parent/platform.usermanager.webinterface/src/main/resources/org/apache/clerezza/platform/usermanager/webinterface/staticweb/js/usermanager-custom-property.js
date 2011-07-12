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

$(document).ready(function() {
	CustomProperty.initButtons();
	buttonVisibilty();
});
CustomProperty = function(){};

CustomProperty.initButtons = function() {

	$("#deleteButton").bind("click", function() {
		if(!$(this).hasClass("tx-inactive")){
			var activatedCheckBoxes = $(".tx-tree input[type=checkbox]:checked");
			var prop = "property";
			if(activatedCheckBoxes.length > 1) {
				prop = "properties";
			}
			AlertMessage.show(CustomProperty.deleteCustomProperties, "Do you want to delete the selected "+ prop + "?", "Delete Properties");
		}
	});

	$("#addButton").bind("click", function() {
		document.location = "add-property?roleTitle=" + $("table").attr("id");
	});

	$("input[type=checkbox]").bind("click", function() {
		buttonVisibilty();
	});
}

CustomProperty.deleteCustomProperties = function() {
	var activatedCheckBoxes = $(".tx-tree input[type=checkbox]:checked");
	var counter = 1;
	activatedCheckBoxes.each(function() {
		var prop = $(this).val();
		var tr = $(this).parent().parent();
		var options = new AjaxOptions("delete-custom-" + counter, "deleting custom property ", function(data) {
			tr.remove();
		});
		options.type = "POST";
		options.url = "./delete-custom-field";
		options.data = {"role": $("table").attr("id"), "property": prop};
		$.ajax(options);
		counter++;

	});
}

function buttonVisibilty() {
	var activatedCheckBoxes = $(".tx-tree input[type=checkbox]:checked").length
	if (activatedCheckBoxes == 1) {
		$("#deleteButton").removeClass("tx-inactive");
	} else if (activatedCheckBoxes > 1) {
		$("#deleteButton").removeClass("tx-inactive");
	} else {
		$("#deleteButton").addClass("tx-inactive");
	}
}
