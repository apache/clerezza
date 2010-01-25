$(document).ready(function () {
	if($("#tx-panel-detail").children().length == 0){
		$("#tx-panel").hide();
	} else {
		$("#tx-panel-detail").children().each(function() {
			var li = $("<li/>").addClass($(this).attr("id"))
			var aHref = $("<a/>").attr("href", "#").text("tab");
			aHref.bind("click", function () {
				if(!$(this).parent().hasClass("tx-active")) {
					$("#tx-panel-detail").children().each(function() {
						$(this).hide();
					});
					$("#tx-tab-panel-ol").children().each(function() {
						$(this).removeClass("tx-active");
					});
					$("#" + $(this).parent().attr("class")).show();
					$(this).parent().addClass("tx-active");
				}
			})
			li.append(aHref);
			$("#tx-tab-panel-ol").append(li);
		});
		$("#tx-panel-detail").children(":not(:first)").each(function() {
			$(this).hide();
		});
		$("#tx-tab-panel-ol").children(":first").each(function() {
			$(this).addClass("tx-active");
		});	
	}
});