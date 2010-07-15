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