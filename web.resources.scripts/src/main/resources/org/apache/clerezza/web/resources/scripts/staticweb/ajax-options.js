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
 * The AjaxOptions is a object defining the options of an jquery ajax object.
 * @see <a href="http://docs.jquery.com/Ajax/jQuery.ajax#options">
 * http://docs.jquery.com/Ajax/jQuery.ajax#options</a>. AjaxOptions has the
 * following fields: type (type of the url request), actionName (name/id of the request),
 * actionText (text which will be displayed for the user),
 * beforeSendAction (function called before the ajax request),
 * beforeSend (function, which calls beforeSendAction), successAction
 * function called if requested was successfully), success (function, which calls
 * successAction), errorAction (analogous to successAction), error (analogous to success)
 * 
 */

AjaxOptions = function(actionName, actionText, successAction) {
    if (actionName) {
        this.actionName = actionName
    }
	if (actionText) {
        this.actionText = actionText
    }
    if (successAction) {
        this.successAction = successAction
    }
}
AjaxOptions.prototype.type = "GET";
AjaxOptions.prototype.actionName = "Unnamed";
AjaxOptions.prototype.actionText = "retrieving data";
AjaxOptions.prototype.beforeSendAction = function() {};
AjaxOptions.prototype.beforeSend = function() {
	this.beforeSendAction();
	statusMessage.add(this.actionName, this.actionText);
}
AjaxOptions.prototype.successAction = function(obj) {};
AjaxOptions.prototype.success = function(obj) {
	this.successAction(obj);
	statusMessage.remove(this.actionName);
}
AjaxOptions.prototype.errorAction = function() {};
AjaxOptions.prototype.error = function(XMLHttpRequest, textStatus, errorThrown) {
    this.errorAction();
	var errorMessage = XMLHttpRequest.responseText;
	if(errorMessage == undefined || errorMessage == "") {
		errorMessage = "Unspecified Error (" + textStatus + ")";
	}
	try {
		var errorXml = XMLHttpRequest.responseXML
	} catch (err) {
	}

	if (!errorXml) {
		try {
			errorXml = $(errorMessage)[0]
		} catch (err) {}
	}
	if (errorXml) {
		try {
			var errorMessageFromXml = $('body',errorXml).text()
		} catch (err) {
		}
	}
	if (errorMessageFromXml) {
		errorMessage = errorMessageFromXml
	} else {
		errorMessage = errorMessage.replace(/</g, "&lt;").replace(/>/g, "&gt;");
	}
	AlertMessage.show(function(){}, "The following error occured: " + errorMessage, "Error", "Ok");
	statusMessage.remove(this.actionName);
};
