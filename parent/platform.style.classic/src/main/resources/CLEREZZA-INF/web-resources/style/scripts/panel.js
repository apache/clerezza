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

$(document).ready(function () {
	if($("#tx-panel-tabs").children().length == 0){
		$("#tx-panel").hide();
	} else {
		$("#tx-panel-tabs").children().each(function() {
			var titleElem =  $("#" + $(this).attr("id") + " > h3:first");
			var title = $(this).attr("id");
			if(titleElem.length > 0) {
				titleElem.hide();
				title = titleElem.text();
			}
			var li = $("<li/>").addClass($(this).attr("id")).attr("title", title);
			var aHref = $("<a/>").attr("href", "#").text("tab");
			aHref.bind("click", function () {
				if(!$(this).parent().hasClass("tx-active")) {
					$("#tx-panel-title > h3").text($(this).parent().attr("title"));
					$("#tx-panel-tabs").children().each(function() {
						$(this).hide();
					});
					$("#tx-panel-tab-buttons-ol").children().each(function() {
						$(this).removeClass("tx-active");
					});
					$("#" + $(this).parent().attr("class")).show();
					$(this).parent().addClass("tx-active");
				}
			})
			li.append(aHref);
			$("#tx-panel-tab-buttons-ol").append(li);
		});
		$("#tx-panel-tabs").children(":not(:first)").each(function() {
			$(this).hide();
		});
		$("#tx-panel-tab-buttons-ol").children(":first").each(function() {
			$("#tx-panel-title > h3").text($(this).attr("title"));
			$(this).addClass("tx-active");
		});	
	}
});
