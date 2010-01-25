$(document).ready(function () {
	if($("#tx-panel-tabs").children().length == 0){
		$("#tx-panel").hide();
	} else {
		$("#tx-panel-tabs").children().each(function() {
			var titleElem =  $("#" + $(this).attr("id") + " > h3:first");
			var title = $(this).attr("id");
			if(titleElem.length > 0) {
				titleElem.hide();
				title = titleElem.text();
			}
			var li = $("<li/>").addClass($(this).attr("id")).attr("title", title);
			var aHref = $("<a/>").attr("href", "#").text("tab");
			aHref.bind("click", function () {
				if(!$(this).parent().hasClass("tx-active")) {
					$("#tx-panel-title > h3").text($(this).parent().attr("title"));
					$("#tx-panel-tabs").children().each(function() {
						$(this).hide();
					});
					$("#tx-panel-tab-buttons-ol").children().each(function() {
						$(this).removeClass("tx-active");
					});
					$("#" + $(this).parent().attr("class")).show();
					$(this).parent().addClass("tx-active");
				}
			})
			li.append(aHref);
			$("#tx-panel-tab-buttons-ol").append(li);
		});
		$("#tx-panel-tabs").children(":not(:first)").each(function() {
			$(this).hide();
		});
		$("#tx-panel-tab-buttons-ol").children(":first").each(function() {
			$("#tx-panel-title > h3").text($(this).attr("title"));
			$(this).addClass("tx-active");
		});	
	}
});