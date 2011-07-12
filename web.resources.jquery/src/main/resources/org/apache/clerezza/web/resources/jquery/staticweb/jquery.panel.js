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
