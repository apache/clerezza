/*
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
 */

/**
 * <code>Overlay</code> generates an overlay window on the page.
 * <code>Overlay</code> has a method show, which creates and renders an
 * overlay. The following css and javascript are required (provided by clerezza):
 * /yui/container/assets/container-core.css
 * /yui/yahoo-dom-event/yahoo-dom-event.js
 * /yui/build/dragdrop/dragdrop-min.js
 * /yui/container/container-min.js
 * /yui/resize/resize-min.js
 *
 */
function Overlay(){};

/**
 * Creates and renders an overlay.
 *
 * @param body html elements or text
 * @param header html elements or text
 * @param widthOverlay an optional parameter which specifies the width of the overlay
 * (default is 46em)
 * @param heightOverlay an optional parameter which specifies the height of the overlay
 * (default is 37em)
 * 
 *
 */
Overlay.show = function(body, header, widthOverlay, heightOverlay) {

	if(widthOverlay == undefined || widthOverlay == "") {
		widthOverlay = "46em";
	}
	if(heightOverlay == undefined || heightOverlay == "") {
		heightOverlay = "37em";
	}

	this.overlay = new YAHOO.widget.Panel("tx-overlay", {
		draggable: true,
		width: widthOverlay,
		height: heightOverlay,
		constraintoviewport: true,
		modal: true,
		zIndex: 100000,
		close: true,
		fixedcenter: true,
		autofillheight: "body",
		context: ["content"]
	});

	if (body.length) {
		if (!body.substring) {
			//as with results from jquery $-fucntion
			body = body[0]
		}
	}

	this.overlay.setBody(body);
	if (header.length) {
		this.overlay.setHeader(header);
	}	
	
	this.overlay.render(document.body);
	$("#tx-overlay").addClass("tx-window");
	
	// if overlay is closed the overlay is removed from dom (default is visibility: hidden)
	this.overlay.hideEvent.subscribe(function() {
		this.overlay.destroy();
		this.resize.destroy();
	});
	
	// Creates resize object, binding it to the 'tx-overlay' <div>
	
	this.resize = new YAHOO.util.Resize("tx-overlay", {
		handles: ["br"],
		autoRatio: false,
		status: false 
	});

	// Setup startResize handler, to limit the resize width/height
	// if the constraintoviewport configuration property is enabled.
	this.resize.on("startResize", function(args) {

		if (this.cfg.getProperty("constraintoviewport")) {
			var D = YAHOO.util.Dom;

			var clientRegion = D.getClientRegion();
			var elRegion = D.getRegion(this.element);

			this.resize.set("maxWidth", clientRegion.right - elRegion.left - YAHOO.widget.Overlay.VIEWPORT_OFFSET);
			this.resize.set("maxHeight", clientRegion.bottom - elRegion.top - YAHOO.widget.Overlay.VIEWPORT_OFFSET);
		} else {
			this.resize.set("maxWidth", null);
			this.resize.set("maxHeight", null);
		}

	}, this.overlay, true);

	// Setup resize handler to update the panel's 'height' configuration property
	// whenever the size of the 'resizablepanel' div changes.

	// Setting the height configuration property will result in the 
	// body of the Panel being resized to fill the new height (based on the
	// autofillheight property introduced in 2.6.0) and the iframe shim and 
	// shadow being resized if required (for IE6 and IE7 quirks mode).
	this.resize.on("resize", function(args) {
		var panelHeight = args.height;
		this.cfg.setProperty("height", panelHeight + "px");
	}, this.overlay, true);

	// set the tooltip string which is shown when the mouse pointer is over the
	// "close"-icon
	$(".container-close").attr("alt", "Close")
	$(".container-close").attr("title", "Close")
};

Overlay.close = function() {
	this.overlay.destroy();	
	this.resize.destroy();	
}







