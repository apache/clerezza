
(function($){
$.fn.menu = function(settings)
{
	var defaults = 
	{
		timer: 500
	};
	
	
  	var settings  = $.extend(defaults, settings);
	settings.linkElements = this.find("> ol > li");
	settings.menuItem = 0;
	settings.menuTimer = 0;
	
	var menuOpen = function(wrappedSet)
	{
		menuClearTimer();
		menuClose();
		settings.menuItem = wrappedSet.find("ol").show();
	}
	
	var menuClose = function()
	{
		if (settings.menuItem)
		{
			settings.menuItem.hide();
		}	
	}

	var menuClearTimer = function()
	{
		if (settings.menuTimer)
		{
			window.clearTimeout(settings.menuTimer);
			settings.menuTimer = null;
		}			
	}

	var menuSetTimer = function()
	{
		settings.menuTimer = window.setTimeout(menuClose, settings.timer);
	}
	
	$(settings.linkElements).bind("mouseover",function(){
		menuOpen($(this));
		
	});
	$(settings.linkElements).bind("mouseout",menuSetTimer);
	
};
})(jQuery);


$(function(){
	$("#tx-menu").menu();
});
