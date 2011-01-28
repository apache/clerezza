$(document).ready(function() {
	UserManager.initButtons();
	buttonVisibilty();

});
UserManager = function(){};

UserManager.initButtons = function() {

	$("#deleteButton").bind("click", function() {
		if(!$(this).hasClass("tx-inactive")){
			var activatedCheckBoxes = $(".tx-tree input[type=checkbox]:checked");
			var user = "user";
			if(activatedCheckBoxes.length > 1) {
				user = "users";
			}
			AlertMessage.show(UserManager.deleteUsers, "Do you want to delete the selected " + user + "?", "Delete Users");
		}
	});
	$("#editButton").bind("click", function() {
		if(!$(this).hasClass("tx-inactive")){
			var activatedCheckBox = $(".tx-tree input[type=checkbox]:checked")
			document.location = "update-user?userName=" + activatedCheckBox.val();
		}

	});

	$("#showPermissionsButton").bind("click", function() {
		if(!$(this).hasClass("tx-inactive")){
			var activatedCheckBox = $(".tx-tree input[type=checkbox]:checked")
			document.location = "manage-user-permissions?userName=" + activatedCheckBox.val();
		}

	});

	$("input[type=checkbox]").bind("click", function() {
		buttonVisibilty();
	});
}

UserManager.deleteUsers = function() {
	var activatedCheckBoxes = $(".tx-tree input[type=checkbox]:checked")
	var counter = 1;
	activatedCheckBoxes.each(function() {
		var name = $(this).val();
		var options = new AjaxOptions("delete-user-" + counter, "deleting user ", function(data) {
			$("#" + name).remove();
		});
		options.type = "POST";
		options.url = "./delete-user";
		options.data = {"userName": name};
		$.ajax(options);
		counter++;
	});
}

function buttonVisibilty() {
	var activatedCheckBoxes = $(".tx-tree input[type=checkbox]:checked").length
	if (activatedCheckBoxes == 1) {
		$("#deleteButton").removeClass("tx-inactive");
		$("#editButton").removeClass("tx-inactive");
		$("#showPermissionsButton").removeClass("tx-inactive");
	} else if (activatedCheckBoxes > 1) {
		$("#deleteButton").removeClass("tx-inactive");
		$("#editButton").addClass("tx-inactive");
		$("#showPermissionsButton").addClass("tx-inactive");
	} else {
		$("#deleteButton").addClass("tx-inactive");
		$("#editButton").addClass("tx-inactive");
		$("#showPermissionsButton").addClass("tx-inactive");
	}
}