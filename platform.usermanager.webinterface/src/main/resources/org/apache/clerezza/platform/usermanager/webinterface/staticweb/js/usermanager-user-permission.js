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
	UserManagerUserPermission.initButtons();
});
UserManagerUserPermission = function(){};

UserManagerUserPermission.initButtons = function() {

	$("#saveButton").bind("click", function() {
		if(!$(this).hasClass("tx-inactive")){
			var options = new AjaxOptions("update-permissions", "updating permissions", function(data) {
					
			});
			options.type = "POST";
			options.url = "./update-user-permissions";
			options.data = $("#form1").serialize();
			$.ajax(options);
		}

	});
	$("input[type=checkbox]").bind("click", function() {
		$("#saveButton").removeClass("tx-inactive");
	});

	$("#addButton").bind("click", function() {
		$("#form2")[0].action="add-user-permissions";
		$("#form2")[0].submit();
	});
}
