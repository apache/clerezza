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
