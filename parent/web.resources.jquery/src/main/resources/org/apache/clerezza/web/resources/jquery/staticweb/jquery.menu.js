/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/


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
		wrappedSet.addClass("active");
		settings.menuItem = wrappedSet.find("div").show();
		settings.menuItem.find("> ol > li").bind("mouseover",function(){
			$(this).addClass("active");
		});
		settings.menuItem.find("> ol > li").bind("mouseout",function(){
			$(this).removeClass("active");
		});		
	}
	
	var menuClose = function()
	{	
		if (settings.menuItem)
		{
			settings.linkElements.removeClass("active");
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
