$(document).ready(function () {
	$('.tx-result > ol > li').bind("click",function(){
		$(this).find("ol").toggle();
		if ($(this).children().is(":hidden"))
			$(this).removeClass("tx-open");
		else
			$(this).addClass("tx-open");
	});

	$('.tx-panel-open-close').bind("click",function(){

		var oPanel = $('.tx-panel').not(".tx-panel-middle").not(".tx-panel-right");

		if (!oPanel.hasClass("tx-close")) {
			$(this).removeClass("tx-icon-left-inverse");
			$(this).addClass("tx-icon-right-inverse");
			oPanel.addClass("tx-close");
			$(this).addClass("tx-close");
			$('.tx-list').addClass("tx-hide-panel").removeClass("tx-show-panel");
			$('.tx-edit').addClass("tx-hide-panel").removeClass("tx-show-panel");
			$('.tx-tree').addClass("tx-hide-panel").removeClass("tx-show-panel");
		}
		else {
			$(this).removeClass("tx-icon-right-inverse");
			$(this).addClass("tx-icon-left-inverse");
			oPanel.removeClass("tx-close");
			$(this).removeClass("tx-close");
			$('.tx-list').addClass("tx-show-panel").removeClass("tx-hide-panel");
			$('.tx-edit').addClass("tx-show-panel").removeClass("tx-hide-panel");
			$('.tx-tree').addClass("tx-show-panel").removeClass("tx-hide-panel");
		}
	});

});
