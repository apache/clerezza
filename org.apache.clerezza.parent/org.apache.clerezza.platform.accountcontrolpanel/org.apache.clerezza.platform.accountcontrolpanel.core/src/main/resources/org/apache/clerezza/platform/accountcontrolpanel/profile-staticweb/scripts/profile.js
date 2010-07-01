$(document).ready(function() {
	$('#newWebIdButton').click(function() {
		$("#newOrExistingSelection").css({display: "none"})
		$("#createNewWebId").css({display: "block"})
	});
	$("#existingWebIdButton").click(function() {
		$("#newOrExistingSelection").css({display: "none"})
		$("#setExistingWebId").css({display: "block"})
	});
	//$('form').submit(function () { return false; })
});
