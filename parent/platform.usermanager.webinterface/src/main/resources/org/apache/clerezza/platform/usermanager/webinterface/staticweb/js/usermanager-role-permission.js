$(document).ready(function() {
	UserManagerRolePermission.initButtons();
});
UserManagerRolePermission = function(){};

UserManagerRolePermission.initButtons = function() {

	$("#saveButton").bind("click", function() {
		if(!$(this).hasClass("tx-inactive")){
			var options = new AjaxOptions("update-permissions", "updating permissions", function(data) {

			});
			options.type = "POST";
			options.url = "./update-role-permissions";
			options.data = $("#form1").serialize();
			$.ajax(options);
		}

	});
	$("input[type=checkbox]").bind("click", function() {
		$("#saveButton").removeClass("tx-inactive");
	});

	$("#addButton").bind("click", function() {
		$("#form2")[0].action="add-role-permissions";
		$("#form2")[0].submit();
	});
}
