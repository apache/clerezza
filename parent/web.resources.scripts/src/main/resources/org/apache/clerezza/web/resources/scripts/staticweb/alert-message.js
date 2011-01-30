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
 * <code>AlertMessage</code> shows a dialog box on top of a page.
 * <code>AlertMessage</code> has a method show, which creates and renders a
 * dialog box.
 * 
 */
function AlertMessage(){};

/**
 * Creates and renders a dialox box.
 *
 * @param yesHandler is a callback function which fired when the yes button is pressed
 * @param message
 * @param headerText
 * @param textYes
 * @param textNo
 * 
 *
 */
AlertMessage.show = function(yesHandler, message, headerText, textYes, textNo) {
	var handleYes = function() {
		if (yesHandler) {
			yesHandler();
		}
		this.destroy();
	};
	var handleNo = function() {
		this.destroy();
	};
    
	if(message == undefined) {
		message = "Are you sure you want to lose the unsaved changes?";
	}
	if(headerText == undefined) {
		headerText = "Alert";
	}
	var buttonConfig = new Array();
	if(textYes != undefined){
		buttonConfig.push({
			text:textYes,
			handler:handleYes,
			isDefault:true
		});
	} else if(textYes == undefined && textNo == undefined) {
		buttonConfig = [{
			text:"Yes",
			handler:handleYes,
			isDefault:true
		},
		{
			text:"No",
			handler:handleNo
		}]
	}
	if(textNo != undefined){
		buttonConfig.push({
			text:textNo,
			handler:handleNo
		});
	}
	var dialog = new YAHOO.widget.SimpleDialog("tx-dialog",
	{
		width: "25em",
		fixedcenter: true,
		visible: false,
		draggable: false,
		close: true,
		modal: true,
		zIndex: 100000,
		constraintoviewport: true,
		buttons: buttonConfig
	} );
	dialog.cancelEvent.subscribe(function() {
		this.destroy();
	});

	dialog.setHeader(headerText);
	dialog.setBody(message);
	dialog.render(document.body);
	$("#tx-dialog").addClass("tx-window");
	dialog.show();
}


