$(document).ready(function () {
	$('.link').live("click",function() {
		showInformation($(this).attr('id'));
	});

});
