function refreshCustomInformation(user){
	multipleValues = $("#userRoles").val() || []
	var options = new AjaxOptions("get-customfields", "retrieving custom fields", function(data) {
			$("#custominfos").html(data);
		});
	options.type = "GET";
	options.url = "./custom-user";
	options.dataType = "html";
	if(user != null){
		options.data = "user="+user+"+&resource=" + document.location.href +"&roles="+multipleValues.join(",")+"&mode=naked"
	} else {
		options.data = "resource=" + document.location.href +"&roles="+multipleValues.join(",")+"&mode=naked"
	}
	$.ajax(options);
}

function saveUser(){
	$("#userform").submit();
}