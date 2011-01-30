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
 * The ModificationStatus is a global object with a field isModified 
 * indicating if some saveable information has been changed on the page.<br/>
 * 
 * The isModified field can be set by client code, or alternatively elements
 * that fire change events can be registered for being monitored using
 * <code>watchElements</code>. This method takes a jquery selector string to
 * specify the elements to be watched.<br/>
 * 
 * An onbeforeunload-listener is registered to the window informing the user 
 * about unsaved changes on an attempt to leave the page while isModified is true.
 *<br/>
 * Elements 
 * <br/><br/>
 * How to use it:
 * Add listeners to the elements, which should listen to on change events.
 * ModificationStatus.watchElements("input")
 * Use jquery selectors (http://docs.jquery.com/Selectors) to register the elements.
 * The above example add listeners to all input fields.
 *
 */
ModificationStatus = new Object();

/**
 * true if some saveable information on the page has been changed, alse otherwise
 */
ModificationStatus.isModified = false;

/**
 * Adds a listener to the elements resulting from a jquery
 * selector (http://docs.jquery.com/Selectors). If
 * this element fires a change event isModified is set to true
 *
 * @param jQuerySelector the jquery selector selecting the elements to be monitored
 * 
 */
ModificationStatus.watchElements = function(jQuerySelector) {
	$(jQuerySelector).live("change", function () {
		ModificationStatus.isModified = true;
	});
}

/**
 * Removes the listener from an element selected by a jquery selector
 * (http://docs.jquery.com/Selectors) 
 */
ModificationStatus.unwatchElements = function(jQuerySelector) {
	$(jQuerySelector).die("change");
}

window.onbeforeunload = function() { 
	if(ModificationStatus.isModified) {
		return "There are unsaved changes";
	}
	return undefined;
};






