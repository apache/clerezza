$(document).ready(function () {
	getQueryParam("script");

	$('.link').live("click",function() {
		showInformation($(this).attr('id'));

	});

	$('#deleteButton').bind("click",function() {
		if(ACTUALSCRIPT != null) {
			deleteScript();
		}
	});
	$('#editButton').bind("click",function() {
		if(ACTUALSCRIPT != null) {
			updateScript();
		}
	});
	$('#addButton').bind("click",function() {
		if(ACTUALSCRIPT != null) {
			executeScript();
		}
	});
});
