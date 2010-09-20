$(document).ready(function () {
	$("#tx-login-button").click(function() {
		document.location.href = "/login?referer=" + encodeURIComponent(document.location.href);
	});	
});