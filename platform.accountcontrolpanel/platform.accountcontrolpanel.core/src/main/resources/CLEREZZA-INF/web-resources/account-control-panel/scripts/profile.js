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
	$('#newWebIdButton').click(function() {
		$("#newOrExistingSelection").css({display: "none"})
		$("#createNewWebId").css({display: "block"})
	});
	$("#existingWebIdButton").click(function() {
		$("#newOrExistingSelection").css({display: "none"})
		$("#setExistingWebId").css({display: "block"})
	});
	$("#keygenform").submit(function() {
		return spkacFix();
	});
});

var crmfObject;
function setCRMFRequest() {
	var hiddenField = $("<input type=\"hidden\" name=\"crmf\" id=\"crmf\" \/>");
	$("#keygenform").append(hiddenField)
	//var formContents = $("#keygenform").serialize();
	//var newContents = jQuery.extend({}, formContents);
	hiddenField.val(crmfObject.request)
	$("#keygenform").submit()
}

function spkacFix() {

	if ($("#spkac").val()) {
		return true;
	} else {
		if ($("#crmf").val()) {
			return true;
		}
		//alert("fix needed by firefox in xhtml mode")
		crmfObject = crypto.generateCRMFRequest(
		'CN=Ignored',
		"regToken", "authenticator", // not sure
		null, // base-64 cert for key
		// escrow. set this to null
		"setCRMFRequest();", // callback
		2048, null, "rsa-dual-use"); // key parameters
		return false;
	}
}
