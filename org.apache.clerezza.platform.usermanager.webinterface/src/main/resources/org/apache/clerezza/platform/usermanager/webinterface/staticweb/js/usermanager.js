function refreshCustomInformation(user){
	multipleValues = $("#userRoles").val() || []
	if(user != null){
		$("#custominfos").load("./custom-user?user="+user+"+&resource=" + document.location.href +"&roles="+multipleValues.join(",")+"&mode=naked");
	} else {
		$("#custominfos").load("./custom-user?resource=" + document.location.href +"&roles="+multipleValues.join(",")+"&mode=naked");
	}
}

function saveUser(){
	$("#userform").submit();
}