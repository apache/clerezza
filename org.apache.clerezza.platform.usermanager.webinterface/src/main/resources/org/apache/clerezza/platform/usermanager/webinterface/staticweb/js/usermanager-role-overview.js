$(document).ready(function() {
	$("a#include").fancybox();
	$("#deleteButton").hide();
	$("#deleteButton").bind("click", function() {
		$(":checkbox:checked").each(function() {
			var title = $(this).val();
			$.ajax({
				type: "POST",
				url: "./delete-role",
				data: "roleTitle="+title,
				success: function(msg) {
					$("#" + title).remove();
				}
			});

		});
	});
	$(":checkbox").bind("click", function() {
		buttonVisibilty();
	});
});
function buttonVisibilty() {
	if ($(":checkbox:checked").length >= 1) {
		$("#deleteButton").show();

	} else if ($(":checkbox:checked").length == 0) {
		$("#deleteButton").hide();
	}
}