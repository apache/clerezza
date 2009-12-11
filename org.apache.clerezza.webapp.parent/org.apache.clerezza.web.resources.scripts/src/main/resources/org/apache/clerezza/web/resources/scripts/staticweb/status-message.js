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
 * <code>statusMessage</code> allows to add/remove status messages (e.g. saving) 
 * to an overlay (absolut positioned <code>div</code>) which follows the mouse pointer. 
 * During a ajax request a status message can be added. After the response the message can be removed
 * Each message is added to an array.
 *
 * @constructor
 */

function StatusMessage() {

	var messages = new Array();

	var speed = 3000;

	var offSet = 10;
	
	var mousePosX = 10;
	var mousePosY = 10;

	/**
     * Stores the current mouse position
     *
     * @param posX
	 * @param posY
	 *
     */
	this.setMousePos = function(posX, posY) {
		mousePosX = posX;
		mousePosY = posY;
	}

	/**
     * Sets the fade out speed
     *
     * @param milliseconds speed in milliseconds
     */
	this.setFadeOutSpeed = function(milliseconds) {
		speed = milliseconds;
	}
    
	/**
     * Adds a message to the overlay
     *
     * @param id which identifies the message
     *
     * @param text which should be displayed
     */
	this.add = function(id, text) {

		if (messages.length == 0) {
			$("#tx-loader").remove();
			var loader = $("<div/>").attr("id", "tx-loader");
			loader.appendTo("body");
			$().bind("mousemove.eventStatus", function(e){
				loader.css({
					top: e.pageY + offSet,
					left: e.pageX + offSet
				});
			});
			loader.css({
					top: (mousePosY + offSet),
					left: (mousePosX + offSet)
				});
			loader.show();
		} 
		if($("#" + id).size() == 0) {
			var div = $("<div/>").attr({
				"id": id
			})
			.text(text);
			messages.push(id);
			$("#tx-loader").append(div);
		}
		
	}
    
	/**
     * Removes the message form the the overlay
     * @param id which identifies the message
     *
     */
	this.remove = function(id) {

		if (messages.length) {
			for (var i = 0; i < messages.length; i++) {
				if (messages[i] == id) {
					$("#" + id).fadeOut(speed, function() {
						$(this).remove();
					});
					messages.splice(i,1);
					break;
				}
			}
		}
		if (messages.length == 0) {
			$("#tx-loader").fadeOut(speed, function() {
				$(this).remove();
				$().unbind("mousemove.eventStatus");
			});
		}
	}
}

//use statusMessage
var statusMessage = new StatusMessage();

$().bind("mousemove", function(e){
	statusMessage.setMousePos(e.pageX, e.pageY);
});


