$(document).ready(function() {
	RoleManager.initButtons();
	buttonVisibilty();
});
RoleManager = function(){};

RoleManager.initButtons = function() {

	$("#deleteButton").bind("click", function() {
		if(!$(this).hasClass("tx-inactive")){
			var activatedCheckBoxes = $(".tx-tree input[type=checkbox]:checked")
			var counter = 1;
			activatedCheckBoxes.each(function() {
				var title = $(this).val();
				if(title != "BasePermissionsRole") {
					var options = new AjaxOptions("delete-role-" + counter, "deleting role ", function(data) {
						$("#" + title).remove();
					});
					options.type = "POST";
					options.url = "./delete-role";
					options.data = {"roleTitle": title};
					$.ajax(options);
					counter++;
				} else {
					AlertMessage.show(undefined, "Could not delete BasePermissionsRole", "Alert", "Ok");
				}
			});
		}
	});

	$("#showPermissionsButton").bind("click", function() {
		if(!$(this).hasClass("tx-inactive")){
			var activatedCheckBox = $(".tx-tree input[type=checkbox]:checked")
			document.location = "manage-role-permissions?roleTitle=" + activatedCheckBox.val();
		}

	});

	$("#manageCustomFieldsButton").bind("click", function() {
		if(!$(this).hasClass("tx-inactive")){
			var activatedCheckBox = $(".tx-tree input[type=checkbox]:checked")
			document.location = "manage-custom-properties?role=" + activatedCheckBox.val();

		}

	});
	$("#saveButton").bind("click", function() {
		$("#form1")[0].submit();
	});


	$("input[type=checkbox]").bind("click", function() {
		buttonVisibilty();
	});
}

function buttonVisibilty() {
	var activatedCheckBoxes = $(".tx-tree input[type=checkbox]:checked").length
	if (activatedCheckBoxes == 1) {
		$("#deleteButton").removeClass("tx-inactive");
		$("#showPermissionsButton").removeClass("tx-inactive");
		$("#manageCustomFieldsButton").removeClass("tx-inactive");
	} else if (activatedCheckBoxes > 1) {
		$("#deleteButton").removeClass("tx-inactive");
		$("#showPermissionsButton").addClass("tx-inactive");
		$("#manageCustomFieldsButton").addClass("tx-inactive");
	} else {
		$("#deleteButton").addClass("tx-inactive");
		$("#showPermissionsButton").addClass("tx-inactive");
		$("#manageCustomFieldsButton").addClass("tx-inactive");
	}
}