$(document).ready(function() {
	$("a#include").fancybox();
	$("#deleteButton").hide();
	$("#editButton").hide();
	$("#deleteButton").bind("click", function() {
		$(":checkbox:checked").each(function() {
			var name = $(this).val();
			$.ajax({
			type: "POST",
			url: "./delete-user",
			data: "userName="+name,
			success: function(msg) {
				$("#" + name).remove();
			}
		});

		});
	});
	$("#editButton").bind("click", function() {
		$(":checkbox:checked").each(function() {
			document.location = "update-user?userName=" + $(this).val();
		});

	});
	$(":checkbox").bind("click", function() {
		buttonVisibilty();
	});
});
function buttonVisibilty() {
	if ($(":checkbox:checked").length == 1) {
		$("#deleteButton").show();
		$("#editButton").show();
	}
	if ($(":checkbox:checked").length > 1) {
		$("#deleteButton").show();
		$("#editButton").hide();
	} else if ($(":checkbox:checked").length == 0) {
		$("#deleteButton").hide();
		$("#editButton").hide();
	}
}