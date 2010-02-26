xhtmlNS  = "http://www.w3.org/1999/xhtml";

function RDF(localName) {
	return new RDFSymbol('http://www.w3.org/1999/02/22-rdf-syntax-ns#'+localName);
}

function RDFS(localName) {
	return new RDFSymbol('http://www.w3.org/2000/01/rdf-schema#'+localName);
}

function WidgetFactory() {
}

window.onbeforeunload = function () {
	var overview = "";
	var unsavedCount = 0;
	for (var i = 0; i < WidgetFactory.openWidgets.length; i++) {
		overview += WidgetFactory.openWidgets [i].rdfSymbol+"="+WidgetFactory.openWidgets[i].modified+", ";
		if (WidgetFactory.openWidgets[i].modified) {
			unsavedCount++;
		}
	}
	//alert("DEBUG. "+overview);
	if (unsavedCount > 1) {
		return "There are "+unsavedCount+" unsaved discobits.";
	}
	if (unsavedCount == 1) {
		return "There is an unsaved discobit.";
	}
	return undefined;
}

WidgetFactory.typeWidgets = new Array();

WidgetFactory.openWidgets = new Array();


/**
 * Creates a new widget
 * ...
 * @param {RDFIndexedFormula} lastSavedContent the last saved version of the store, if this is provided the save-link is active in the new widget
 * @type String
 */
WidgetFactory.createBackground = function(terminationListener, rdfSymbol, xhtmlContainer, providedFunctions, store, widgetHolder, lastSavedContent) {
			// use FireBug extension to inspect console.debug'd objects
		// Using TestStore you can access store.triples
		/*if (typeof(console) !=  'undefined') {
			console.debug('creating '+rdfSymbol+' with WidgetFactory.store',WidgetFactory.store);
		}*/
	if (!widgetHolder) {
		var widgetHolder = new Object();
		//temporary pseudo-widget, a hack to make rdfSymbol accesible already
		widgetHolder.widget = new Object();
		widgetHolder.widget.rdfSymbol = rdfSymbol;
	}
	window.setTimeout(function(){
		WidgetFactory.create(terminationListener, rdfSymbol, xhtmlContainer, providedFunctions, store, widgetHolder, lastSavedContent);
		
	}, 0)
	
	return widgetHolder;
}
WidgetFactory.create = function(terminationListener, rdfSymbol, xhtmlContainer, providedFunctions, store, widgetHolder, lastSavedContent) {
	//alert("creating widget");
	//private functions
	if (!widgetHolder) {
			var widgetHolder = new Object();
	}
	var fillController = function(functions, container) {
		while (container.firstChild) {
			container.removeChild(container.firstChild);
		}
		for (var i = 0; i < functions.length; i++) {
			var controlFunction = functions[i];
			var functionLinkElement = document.createElementNS("http://www.w3.org/1999/xhtml", "a");
			if (controlFunction.icon) {
				var functIcon = document.createElementNS("http://www.w3.org/1999/xhtml", "img");
				functionLinkElement.appendChild(functIcon);
				functIcon.src = controlFunction.icon;
				functIcon.alt = controlFunction.label;
			} else {
				functionLinkElement.appendChild(document.createTextNode(controlFunction.label));			
				
			}
			functionLinkElement.href = "#";
			container.appendChild(functionLinkElement);
			
			functionLinkElement.onclick = WidgetFactory.createOnClickFromPerform(controlFunction.perform);
			container.appendChild(document.createTextNode(" "));
		}
	}
	
	var getGenericControls = function() {
		var controlFunctions = new Array();
		var RDFControl = new Object();
	   	RDFControl.label = "RDF"
	   	RDFControl.perform = function() {
	   		mozile.edit.disable();
		   	var div = document.createElementNS(xhtmlNS, "div");
			var textarea = document.createElementNS(xhtmlNS, "textarea");
			div.appendChild(textarea);
			var useButton = document.createElementNS(xhtmlNS, "button");
			useButton.appendChild(document.createTextNode("use"));
			div.appendChild(useButton);
			var discardButton = document.createElementNS(xhtmlNS, "button");
			discardButton.appendChild(document.createTextNode("discard"));
			div.appendChild(discardButton);
			var body = document.getElementsByTagNameNS(xhtmlNS,"body")[0];
			div.className = "sourceEdit";
			textarea.appendChild(document.createTextNode(new XMLSerializer().serializeToString(RDFXMLSerializer.serialize(widgetHolder.widget.getStore(), ""))));
	   		body.appendChild(div);
	   		useButton.onclick = function() {
	   			var editedStore = new RDFIndexedFormula();
	   			var nodeTree = (new DOMParser()).parseFromString(textarea.value, 'text/xml');
	   			var docElem = nodeTree.documentElement;
	   			if (docElem.namespaceURI == 'http://www.mozilla.org/newlayout/xml/parsererror.xml') {
	   				alert(new XMLSerializer().serializeToString(docElem.firstChild));
	   				return;
	   			}
	   			var parser = new RDFParser(editedStore);
	   			parser.parse(nodeTree,rdfSymbol.uri);
				while (xhtmlContainer.firstChild) {
					xhtmlContainer.removeChild(xhtmlContainer.firstChild);
				}
				widgetHolder.widget.remove();
				WidgetFactory.create(function() {
					body.removeChild(div);
					mozile.edit.enable();
				}, rdfSymbol, xhtmlContainer, providedFunctions, editedStore, widgetHolder, widgetHolder.widget.lastSavedContent);
					   		}
	   		discardButton.onclick = function() {
				body.removeChild(div);
				mozile.edit.enable();
	   		}
   		}
   		controlFunctions[controlFunctions.length] = RDFControl;
   		
   		/* control to view RDF to be revoked
   		var revRDFControl = new Object();
	   	revRDFControl.label = "REVRDF"
	   	revRDFControl.perform = function() {
	   	
	   		alert(new XMLSerializer().serializeToString(RDFXMLSerializer.serialize(widget.lastSavedContent, "")));
   		}
   		controlFunctions[controlFunctions.length] = revRDFControl;
   		*/
   		var uriControl = new Object();
	   	uriControl.label = "URI"
	   	uriControl.perform = function() {
	   		alert(rdfSymbol.uri);
   		}
   		controlFunctions[controlFunctions.length] = uriControl;
   		
   		var reloadControl = new Object();
	   	reloadControl.label = "RELOAD"
	   	reloadControl.perform = function() {
			var reloadedStore = new RDFIndexedFormula();
			WidgetFactory.load(rdfSymbol, reloadedStore, function() {
				while (xhtmlContainer.firstChild) {
					xhtmlContainer.removeChild(xhtmlContainer.firstChild);
				}
				widgetHolder.widget.remove();
				var body = document.getElementsByTagNameNS(xhtmlNS,"body")[0];
				var origCursor = body.style.cursor;
				body.style.cursor = 'progress';
				WidgetFactory.create(function() {
					body.style.cursor = origCursor;
				},rdfSymbol, xhtmlContainer, providedFunctions, reloadedStore, widgetHolder);
			});

   		}
   		controlFunctions[controlFunctions.length] = reloadControl;
   		
	 	return controlFunctions;
	}
	
	
	xhtmlContainer.style.border = "dashed";
	xhtmlContainer.style.borderWidth = "1px 1px 0px 0px";
	
	var saveLink = document.createElementNS("http://www.w3.org/1999/xhtml", "a");
	saveLink.style.visibility ="hidden";
	//saveLink.appendChild(document.createTextNode("Save"));
	var saveIcon = document.createElementNS("http://www.w3.org/1999/xhtml", "img");
	saveLink.appendChild(saveIcon);
	saveIcon.src = WidgetFactory.root+"mozile/images/silk/page_save.png";
	;
	saveLink.onclick = function() {
		widgetHolder.widget.save();
	}
	var controlArea = document.createElementNS("http://www.w3.org/1999/xhtml", "div");
	controlArea.className = "controlArea";
	
	//order: genericControls, widgetControls, contextControls, save
	var genericFunctionContainer = document.createElementNS("http://www.w3.org/1999/xhtml", "span");
	controlArea.appendChild(genericFunctionContainer);
	var widgetFunctionContainer = document.createElementNS("http://www.w3.org/1999/xhtml", "span");
	controlArea.appendChild(widgetFunctionContainer);
	var contextFunctionContainer = document.createElementNS("http://www.w3.org/1999/xhtml", "span");
	controlArea.appendChild(contextFunctionContainer);
	xhtmlContainer.appendChild(controlArea);
	controlArea.appendChild(saveLink);
	
	fillController(getGenericControls(), genericFunctionContainer);
	
	var fillContextControler = function(contextFunctions) {
		fillController(contextFunctions, contextFunctionContainer);
	}

	if (providedFunctions) {
		fillContextControler(providedFunctions);
	}
		
	var typeWidget = document.createElementNS("http://www.w3.org/1999/xhtml", "div");
	typeWidget.className = "typeWidget";
	xhtmlContainer.appendChild(typeWidget);
	
	
	var afterLoading = function() {
		//	alert("hasType "+WidgetFactory.hasType(rdfSymbol, new RDFSymbol("http://discobits.org/ontology#XHTMLInfoDiscoBit")));
		
		widgetHolder.widget = null;
		
		var controller = new Object();
		controller.modifiedStateChanged = function(newState, widget) {
			if (newState) {
				saveLink.style.visibility ="";
				widget.modified = true;
			} else {
				saveLink.style.visibility ="hidden";
				widget.modified = false;
			}
		}
		
		for (var i = 0; i < WidgetFactory.typeWidgets.length; i++) {
			if (WidgetFactory.hasType(rdfSymbol, WidgetFactory.typeWidgets[i].type, store)) {
				widgetHolder.widget = new WidgetFactory.typeWidgets[i](store, rdfSymbol, typeWidget, controller, function(widget) {
					WidgetFactory.openWidgets.push(widget);
					if(terminationListener) {
						window.setTimeout(function(){
							terminationListener(widgetHolder);
						}, 0);
					}
				});
				break;
			}
		}
	
		if (widgetHolder.widget == null) {
			//throw new Error(rdfSymbol+" no good");
			widgetHolder.widget = new TypeSelectionWidget(rdfSymbol, typeWidget, xhtmlContainer, providedFunctions);
			if(terminationListener) {
				terminationListener(widgetHolder);
			}
		}
	
		widgetHolder.widget.remove = function() {
			var newOpenWidgets = new Array();
			for (var i = 0; i < WidgetFactory.openWidgets.length; i++) {
				if (WidgetFactory.openWidgets[i] != widgetHolder.widget) {
					newOpenWidgets.push(WidgetFactory.openWidgets[i]);
				}
			}
			WidgetFactory.openWidgets = newOpenWidgets;
			if (this.removeChildWidgets) {
				this.removeChildWidgets();
			}
		}
		 
	
		if (widgetHolder.widget.getWidgetControls) {
			fillController(widgetHolder.widget.getWidgetControls(), widgetFunctionContainer);
		}
		widgetHolder.widget.fillContextControler = fillContextControler;
		widgetHolder.widget.xhtmlContainer = xhtmlContainer
		widgetHolder.widget.rdfSymbol = rdfSymbol;
		widgetHolder.widget.controller = controller;
		if (lastSavedContent) {
			widgetHolder.widget.lastSavedContent = lastSavedContent;
			widgetHolder.widget.controller.modifiedStateChanged(true, widgetHolder.widget);
		} else {
			if (widgetHolder.widget.getStore) {
				widgetHolder.widget.lastSavedContent = widgetHolder.widget.getStore();
			}
		}
		if (widgetHolder.widget.getStore) {
			widgetHolder.widget.save = function() {
				var widgetStore = widgetHolder.widget.getStore();
				WidgetFactory.putData(widgetHolder.widget.rdfSymbol, widgetStore, widgetHolder.widget.lastSavedContent);
				widgetHolder.widget.lastSavedContent = widgetStore;
				widgetHolder.widget.controller.modifiedStateChanged(false, widgetHolder.widget);
			}
		}
		//alert(new XMLSerializer().serializeToString(RDFXMLSerializer.serialize(widget.getStore(), rdfSymbol.uri)));
		
		
	}
	
	if (!store) {
		var store = WidgetFactory.store;
		WidgetFactory.ensureDicoBitLoaded(rdfSymbol, function() {
			store = WidgetFactory.store;
			afterLoading();
		});
	} else {
		afterLoading();
	}

}

WidgetFactory.createOnClickFromPerform = function(perform) {
	return function() {
		perform();
		return false;
	}
}

TypeSelectionWidget = function(rdfSymbol, typeWidget, xhtmlContainer, providedFunctions) {
	var select = document.createElementNS("http://www.w3.org/1999/xhtml", "select");
	typeWidget.appendChild(select);
	var selectText = "--- select type ----";
	var option = document.createElementNS("http://www.w3.org/1999/xhtml", "option");
		select.appendChild(option);
		option.appendChild(document.createTextNode(selectText));
	for (var i = 0; i < WidgetFactory.typeWidgets.length; i++) {
		var option = document.createElementNS("http://www.w3.org/1999/xhtml", "option");
		select.appendChild(option);
		option.value = WidgetFactory.typeWidgets[i].type.uri;
		var label;
		if (WidgetFactory.typeWidgets[i].description) {
			label = WidgetFactory.typeWidgets[i].description;
		} else {
			label = WidgetFactory.typeWidgets[i].type.uri
		}
		option.appendChild(document.createTextNode(label));
	}
	var button = document.createElementNS("http://www.w3.org/1999/xhtml", "button");
	button.appendChild(document.createTextNode("set"));
	button.disabled = true;
	typeWidget.appendChild(button);
	select.onchange = function() {
		button.disabled = (this.value == selectText);
	};
	button.onclick = function() {
		WidgetFactory.store.add(rdfSymbol, 
			new RDFSymbol('http://www.w3.org/1999/02/22-rdf-syntax-ns#type'), 
			new RDFSymbol(select.value));
		while (xhtmlContainer.firstChild) {
			xhtmlContainer.removeChild(xhtmlContainer.firstChild);
		}
		WidgetFactory.create(function() {}, rdfSymbol, xhtmlContainer, providedFunctions, undefined, undefined, new RDFIndexedFormula());
		//alert(select.value);
	};
}


//mozile.debug.logLevel = "debug";

{
	var found = false;
	for(var i=0; i < mozile.edit.commands._commands.length; i++) {
		if(mozile.edit.commands._commands[i] == mozile.edit.save);
		//delete(mozile.edit.commands._commands[i]);
		found = true;
		if (found) {
			mozile.edit.commands._commands[i] = mozile.edit.commands._commands[i+1];
		}
	}
	mozile.edit.commands._commands.pop();
}

	
function XHTMLInfoDiscoBitWidget(store, rdfSymbol, xhtmlContainer, controller, terminationListener) {
	// Configure Mozile Basics
	if (!XHTMLInfoDiscoBitWidget.mozileInitialized) {
		mozile.root = WidgetFactory.root+"mozile/";
		mozile.useSchema("lib/xhtml.rng");
		XHTMLInfoDiscoBitWidget.mozileInitialized = true;
	}
	this.rdfSymbol = rdfSymbol;
	this.controller = controller;
	this.xhtmlContainer = xhtmlContainer;
	this.origStore = store;
	this.loadData(store);
	terminationListener(this);
}

WidgetFactory.typeWidgets.push(XHTMLInfoDiscoBitWidget);

XHTMLInfoDiscoBitWidget.type = new RDFSymbol("http://discobits.org/ontology#XHTMLInfoDiscoBit");


XHTMLInfoDiscoBitWidget.prototype.getWidgetControls = function() {
	var controlFunctions = new Array();
	var RDFControl = new Object();
   	RDFControl.label = "XHTML"
   	var widget = this;
   	RDFControl.perform = function() {
   		mozile.edit.disable();
	   	var div = document.createElementNS(xhtmlNS, "div");
		var textarea = document.createElementNS(xhtmlNS, "textarea");
		div.appendChild(textarea);
		var useButton = document.createElementNS(xhtmlNS, "button");
		useButton.appendChild(document.createTextNode("use"));
		div.appendChild(useButton);
		var discardButton = document.createElementNS(xhtmlNS, "button");
		discardButton.appendChild(document.createTextNode("discard"));
		div.appendChild(discardButton);
		var body = document.getElementsByTagNameNS(xhtmlNS,"body")[0];
		div.className = "sourceEdit";
		var serialized = "";
		for (var i = 0; i < widget.editableArea.childNodes.length; i++) {
			serialized += new XMLSerializer().serializeToString(widget.editableArea.childNodes[i]);
		}
		textarea.appendChild(document.createTextNode(serialized));
   		body.appendChild(div);
   		useButton.onclick = function() {
   			var editedStore = new RDFIndexedFormula();
   			var nodeTree = (new DOMParser()).parseFromString("<elem xmlns=\"http://www.w3.org/1999/xhtml\">"+textarea.value+"</elem>", 'text/xml');
   			var docElem = nodeTree.documentElement;
   			if (docElem.namespaceURI == 'http://www.mozilla.org/newlayout/xml/parsererror.xml') {
   				alert(new XMLSerializer().serializeToString(docElem.firstChild));
   				return;
   			}
   			widget.remove();
   			while (widget.editableArea.firstChild) {
	   			widget.editableArea.removeChild(widget.editableArea.firstChild);
   			}
   			for (var i = 0; i < nodeTree.documentElement.childNodes.length; i++) {
				widget.editableArea.appendChild(nodeTree.documentElement.childNodes[i].cloneNode(true));
			}
			widget.controller.modifiedStateChanged(true, widget);
   			body.removeChild(div);
			mozile.edit.enable();
   		}
   		discardButton.onclick = function() {
			body.removeChild(div);
			mozile.edit.enable();
   		}
  		}
  		controlFunctions[controlFunctions.length] = RDFControl;
  		return controlFunctions;
}   		

XHTMLInfoDiscoBitWidget.prototype.loadData = function(store) {
	var infobitProperty = store.anyStatementMatching(this.rdfSymbol, new RDFSymbol("http://discobits.org/ontology#infoBit"), undefined);
	if (infobitProperty) {
		var objectElement = infobitProperty.object.elementValue;
	} else {
		var objectElement = document.createElementNS("http://discobits.org/ontology#","infoBit");
		objectElement.appendChild(document.createTextNode("empty"));
	}
	//var editableParagraph = document.createElementNS("http://www.w3.org/1999/xhtml", "p");
	//xhtmlContainer.appendChild(editableParagraph);
	WidgetFactory.appendChildrenInDiv(objectElement, this.xhtmlContainer);
	this.editableArea = this.xhtmlContainer.childNodes[0];
	mozile.editElement(this.editableArea);
	var controller = this.controller;
	var widget = this;
	var modifiedTrue = function() {
		controller.modifiedStateChanged(true, widget);
	}
	this.editableArea.addEventListener("change", modifiedTrue, false);
}

XHTMLInfoDiscoBitWidget.prototype.getStore = function() {
	var store = new RDFIndexedFormula();
	store.add(this.rdfSymbol, new RDFSymbol("http://discobits.org/ontology#infoBit"), new RDFLiteral(this.editableArea));
	store.add(this.rdfSymbol, 
		new RDFSymbol('http://www.w3.org/1999/02/22-rdf-syntax-ns#type'), 
		new RDFSymbol("http://discobits.org/ontology#XHTMLInfoDiscoBit"));
	var origStatements = this.origStore.statements
	for (var i=0; i<origStatements.length; i++) {
        var statement = origStatements[i];
		if (statement.subject.uri == this.rdfSymbol.uri) {
			if (!(statement.predicate.uri == "http://discobits.org/ontology#infoBit")) {
				store.add(statement.subject, statement.predicate, statement.object)
			}
		}
    }
	return store;
}





function OrderedContentWidget(store, rdfSymbol, xhtmlContainer, controller, terminationListener) {
	this.controller = controller;
	this.rdfSymbol = rdfSymbol;
	this.origStore = store;
	this.load(store, rdfSymbol, xhtmlContainer, terminationListener);	
}



OrderedContentWidget.prototype.load = function(store, rdfSymbol, xhtmlContainer, terminationListener) {
   
    var containsStatements = store.statementsMatching(rdfSymbol, new RDFSymbol("http://discobits.org/ontology#contains"), undefined);
    this.childElemContainer = document.createElementNS("http://www.w3.org/1999/xhtml", "ol");
    this.childElemContainer.className = this.getChildElemContainerClassName();
    
    var children = new Array();//the rdfSymbolS of the children, will accessible by childWidgets[i].rdfSymbol
    var thisWidget = this;
    for(var i=0;i<containsStatements.length;i++) {
        var entry = containsStatements[i].object;
        var pos = store.statementsMatching(entry, new RDFSymbol("http://discobits.org/ontology#pos"), undefined);
        var holdsStatements = store.statementsMatching(entry, new RDFSymbol("http://discobits.org/ontology#holds"), undefined);
        children[pos[0].object] = holdsStatements[0].object; 
    }
	var elementsToAdd = children.length;
	if (elementsToAdd == 0) {
    	window.setTimeout(function() {terminationListener(thisWidget);}, 0);
    }
    this.childWidgets = new Array(children.length);
    for(var j=0;j<children.length;j++) {  
    	this.addChild(children[j], j, undefined, function() {
    		elementsToAdd--;
    		if (elementsToAdd == 0) {
    			terminationListener(thisWidget);
    		}
    	});    	
    }
    xhtmlContainer.appendChild(this.childElemContainer);
}

OrderedContentWidget.type = new RDFSymbol("http://discobits.org/ontology#OrderedContent");

WidgetFactory.typeWidgets.push(OrderedContentWidget);

OrderedContentWidget.prototype.addChild = function(child, pos, lastSavedStore, terminationListener) {
   	var li = document.createElementNS("http://www.w3.org/1999/xhtml", "li");
   	var div = document.createElementNS("http://www.w3.org/1999/xhtml", "div");
   	this.positionHandling(pos, div);
   	var controlFunctions = this.getControlFunctions(li, pos);
   	this.childElemContainer.appendChild(li);
   	li.appendChild(div);
   	this.childWidgets[pos] = WidgetFactory.createBackground(terminationListener, child, div, controlFunctions, undefined, undefined, lastSavedStore);
}

OrderedContentWidget.prototype.removeChildWidgets = function() {
	for (var i = 0; i < this.childWidgets.length; i++) {
		this.childWidgets[i].widget.remove();
	}
}

OrderedContentWidget.prototype.getWidgetControls = function() {
	var controlFunctions = new Array();
	var moveUpControl = new Object();
   	moveUpControl.label = "ADD";
   	var orderedContentWidget = this;
   	moveUpControl.perform = function() {
   		var baseURI = orderedContentWidget.rdfSymbol.uri;
   		if (!baseURI.match(/\/$/)) {
   			baseURI += "-el/";
   		}
   		var childRDFSymbol = new RDFSymbol(baseURI+orderedContentWidget.childWidgets.length);
   		orderedContentWidget.addChild(childRDFSymbol, orderedContentWidget.childWidgets.length); 
   		orderedContentWidget.controller.modifiedStateChanged(true, orderedContentWidget);
   	}
   	controlFunctions[controlFunctions.length] = moveUpControl;
	return controlFunctions;
}
OrderedContentWidget.prototype.getControlFunctions = function(li, pos) {
	var controlFunctions = new Array();
	var childWidgets = this.childWidgets;
	var containerWidget = this;
	
	if (pos > 0) {
	   	var moveUpControl = new Object();
	   	moveUpControl.label = "UP";
	   	moveUpControl.perform = function() {
	   		if (!li.previousSibling) {
	   			alert("no previous element");
	   			return;
	   		}
	   		var previousLiElem = li.previousSibling;
	   		var ulElem = li.parentNode;
	   		ulElem.removeChild(li);
	   		ulElem.insertBefore(li, previousLiElem);
	   		var previousWidget = childWidgets[pos -1];
	   		childWidgets[pos -1] = childWidgets[pos];
	   		childWidgets[pos] = previousWidget;
	   		for (var i = 0; i < childWidgets.length; i++) {
	   			childWidgets[i].widget.fillContextControler(containerWidget.getControlFunctions(ulElem.childNodes[i], i));
	   		}
	   		containerWidget.controller.modifiedStateChanged(true, containerWidget);
	   	}
	   	controlFunctions[controlFunctions.length] = moveUpControl;
	   	
   	}
   	if (pos < (this.childWidgets.length -1)) {
	   	var moveDownControl = new Object();
	   	moveDownControl.label = "DOWN";
	   	moveDownControl.perform = function() {
	   		if (!li.nextSibling) {
	   			alert("no next element");
	   			return;
	   		}
	   		var nextLiElem = li.nextSibling;
	   		var ulElem = li.parentNode;
	   		ulElem.removeChild(nextLiElem);
	   		ulElem.insertBefore(nextLiElem, li);
	   		var nextWidget = childWidgets[pos +1];
	   		childWidgets[pos +1] = childWidgets[pos];
	   		childWidgets[pos] = nextWidget;
	   		for (var i = 0; i < childWidgets.length; i++) {
	   			childWidgets[i].widget.fillContextControler(containerWidget.getControlFunctions(ulElem.childNodes[i], i));
	   		}
	   		containerWidget.controller.modifiedStateChanged(true, containerWidget);
	   	}
	   	controlFunctions[controlFunctions.length] = moveDownControl;
   	}
   	
   	
   	var removeControl = new Object();
   	removeControl.label = "REMOVE"
   	removeControl.perform = function() {
   		var ulElem = li.parentNode;
   		ulElem.removeChild(li);
   		containerWidget.controller.modifiedStateChanged(true, containerWidget);
   		var j = 0;
   		for (var i = 0; i < childWidgets.length; i++) {
   			if (i != pos) {
	   			childWidgets[j] = childWidgets[i];
	   			j++;
	   		} else {
	   			//alert("DEBUG: removing "+childWidgets[i].widget.rdfSymbol);
	   			childWidgets[i].widget.remove();
	   		}
	   	}
	   	childWidgets.length = childWidgets.length-1;
	   	for (var i = 0; i < childWidgets.length; i++) {
		   	childWidgets[i].widget.fillContextControler(containerWidget.getControlFunctions(ulElem.childNodes[i], i));
		}
   	}
   	controlFunctions[controlFunctions.length] = removeControl;
   	
   	return controlFunctions;
}

OrderedContentWidget.prototype.getChildElemContainerClassName = function() {
	return "orderedContent";
}

OrderedContentWidget.prototype.positionHandling = function(pos, div) {
	//div.appendChild(document.createTextNode("regular: "));
}
OrderedContentWidget.prototype.getStore = function() {
	var store = new RDFIndexedFormula();
	store.add(this.rdfSymbol, 
		new RDFSymbol('http://www.w3.org/1999/02/22-rdf-syntax-ns#type'), 
		new RDFSymbol('http://discobits.org/ontology#OrderedContent'));
	for (var i = 0; i < this.childWidgets.length; i++) {
		var entry = this.getEntryForChild(store, i);
		store.add(this.rdfSymbol,  
		new RDFSymbol('http://discobits.org/ontology#contains'),
		entry);
	}
	var origStatements = this.origStore.statements
	for (i=0; i<origStatements.length; i++) {
        var statement = origStatements[i];
		if (statement.subject.uri == this.rdfSymbol.uri) {
			if (!(statement.predicate.uri == "http://discobits.org/ontology#contains")) {
				store.add(statement.subject, statement.predicate, statement.object)
			}
		}
    }
	return store;
}

OrderedContentWidget.prototype.getEntryForChild = function(store, entryPos) {
	var result = new RDFBlankNode();
	store.add(result, 
		new RDFSymbol('http://www.w3.org/1999/02/22-rdf-syntax-ns#type'), 
		new RDFSymbol('http://discobits.org/ontology#Entry'));
	store.add(result,  
		new RDFSymbol('http://discobits.org/ontology#pos'),
		new RDFLiteral(entryPos));
	store.add(result,  
		new RDFSymbol('http://discobits.org/ontology#holds'),
		this.childWidgets[entryPos].widget.rdfSymbol);
	return result;
}


function TitledContentWidget(store, rdfSymbol, xhtmlContainer, controller, terminationListener) {
	var containsStatements = store.statementsMatching(rdfSymbol, new RDFSymbol("http://discobits.org/ontology#contains"), undefined);
    this.childElemContainer = document.createElementNS("http://www.w3.org/1999/xhtml", "ol");
    this.childElemContainer.className = this.getChildElemContainerClassName();
	this.origStore = store
    var titledContentWidget = this;
    var elementsToAdd = 2;
    var subTerminationListener = function() {
    	elementsToAdd--;
    	if (elementsToAdd == 0) {
    		terminationListener(titledContentWidget);
    	}
    }
    
    if (containsStatements.length != 2) {
    	this.childWidgets = new Array(2);
    	var baseURI = rdfSymbol.uri;
   		if (!baseURI.match(/\/$/)) {
   			baseURI += "-";
   		}
   		
   		var titleURI = baseURI + "title";
   		var titleRDFSymbol = new RDFSymbol(titleURI);
   		WidgetFactory.store.add(titleRDFSymbol,  RDF("type"), XHTMLInfoDiscoBitWidget.type);
		this.addChild(titleRDFSymbol, 0, new RDFIndexedFormula(), function(widgetHolder) {
			widgetHolder.widget.controller.modifiedStateChanged(true, widgetHolder.widget);
			//titledContentWidget.childWidgets[0].widget.controller.modifiedStateChanged(true);
			subTerminationListener();
		});    		
   		var contentURI = baseURI + "content";
   		var contentRDFSymbol = new RDFSymbol(contentURI);
		this.addChild(contentRDFSymbol, 1, undefined, subTerminationListener); 
   		controller.modifiedStateChanged(true, this);
   		
   		xhtmlContainer.appendChild(this.childElemContainer);
    } else {
		this.load(store, rdfSymbol, xhtmlContainer, terminationListener);
	}

	
}

TitledContentWidget.type = new RDFSymbol("http://discobits.org/ontology#TitledContent");

WidgetFactory.typeWidgets.push(TitledContentWidget);

TitledContentWidget.prototype.load = OrderedContentWidget.prototype.load;
TitledContentWidget.prototype.addChild = OrderedContentWidget.prototype.addChild;
TitledContentWidget.prototype.getEntryForChild = OrderedContentWidget.prototype.getEntryForChild;
TitledContentWidget.prototype.removeChildWidgets = OrderedContentWidget.prototype.removeChildWidgets;

TitledContentWidget.prototype.getControlFunctions = function(li, pos) {
	controlFunctions = new Array();
	return controlFunctions;
}

TitledContentWidget.prototype.getChildElemContainerClassName = function() {
	return "titledContent";
}

TitledContentWidget.prototype.positionHandling = function(pos, div) {
	if (pos == 0) {
		WidgetFactory.addClass(div, "title");
	}
	
	if (pos == 1) {
		WidgetFactory.addClass(div, "content");
	}
}

TitledContentWidget.prototype.getStore = function() {
	var store = new RDFIndexedFormula();
	store.add(this.rdfSymbol, 
		new RDFSymbol('http://www.w3.org/1999/02/22-rdf-syntax-ns#type'), 
		new RDFSymbol('http://discobits.org/ontology#TitledContent'));
	for (var i = 0; i < this.childWidgets.length; i++) {
		var entry = this.getEntryForChild(store, i);
		store.add(this.rdfSymbol,  
		new RDFSymbol('http://discobits.org/ontology#contains'),
		entry);
	}
	var origStatements = this.origStore.statements
	for (i=0; i<origStatements.length; i++) {
        var statement = origStatements[i];
		if (statement.subject.uri == this.rdfSymbol.uri) {
			if (!(statement.predicate.uri == "http://discobits.org/ontology#contains")) {
				store.add(statement.subject, statement.predicate, statement.object)
			}
		}
    }
	return store;
}

// helpers ////////////
WidgetFactory.addClass = function(elem, className) {
	//elem.className += "foo bar ";
	elem.className += " "+className;
	
}
WidgetFactory.hasType = function(rdfSymbol, type, store) {
 	//alert("anyStatementMatching for "+rdfSymbol+WidgetFactory.store.anyStatementMatching(rdfSymbol, undefined, undefined));
	//return (typeof(WidgetFactory.store.anyStatementMatching(rdfSymbol, undefined, type)) != 'undefined')
	return (typeof(store.anyStatementMatching(rdfSymbol, new RDFSymbol('http://www.w3.org/1999/02/22-rdf-syntax-ns#type'), type)) != 'undefined')
}

WidgetFactory.appendChildrenInDiv = function(objectElement, xhtmlContainer) {
		var div = document.createElementNS("http://www.w3.org/1999/xhtml", "p");
		/*if (typeof(console) !=  'undefined') {
			console.debug(objectElement);
		}*/
		for( var i=0; i< objectElement.childNodes.length; i++ ){
			/*if (typeof(console) !=  'undefined') {
				console.debug("adding node "+i+" "+objectElement.childNodes[i]);
			}*/
			div.appendChild(objectElement.childNodes[i].cloneNode(true));		
		}
		xhtmlContainer.appendChild(div);	
	}
WidgetFactory.putData = function(rdfSymbol, store, previousStore, noContainerCreation) {
	var url = rdfSymbol.uri;
	var xhr = Util.XMLHTTPFactory();
	
	xhr.open("PUT", url, false);
	xhr.setRequestHeader("Content-Type", "application/rdf+xml");
	xhr.send(new XMLSerializer().serializeToString(RDFXMLSerializer.serialize(store, rdfSymbol.uri)));
	/*RFC 2518 says: "A PUT that would result in the creation of a resource without an
   appropriately scoped parent collection MUST fail with a 409."
   
   Apache however sends a 403*/
	if ((xhr.status == 409) || (xhr.status == 403)) {
  		//alert("collection does not exist"+xhr.responseText);
  		if (!noContainerCreation) {
  			WidgetFactory.createContainingCollection(url);
  			WidgetFactory.putData(rdfSymbol, store, previousStore, true);
  		} else {
  			throw new Error(xhr.responseText);
  		}
  	} else {
	  	if (xhr.status >= 300) {
	  		alert("server returned failure: "+xhr.responseText);
	  	}
	 }
}

WidgetFactory.createContainingCollection = function(url, noContainerCreation) {
	if (url == "/") {
		return;
	} 
	var containerURL = url.substring(0, url.lastIndexOf('/',url.length -2)+1);
	var xhr = Util.XMLHTTPFactory();
	xhr.open("MKCOL", containerURL, false);
	xhr.send();
	if ((xhr.status == 409) || (xhr.status == 403)) {
  		//alert("collection does not exist"+xhr.responseText);
  		if (!noContainerCreation) {
  			WidgetFactory.createContainingCollection(containerURL);
  			WidgetFactory.createContainingCollection(url, true);
  		} else {
  			throw new Error(xhr.responseText);
  		}
  	} else {
	  	if (xhr.status >= 300) {
	  		alert("server returned failure: "+xhr.responseText);
	  	}
  	}
}

WidgetFactory.ensureDicoBitLoaded = function(rdfSymbol, terminationListener) {
	if(WidgetFactory.isLoading) {
		window.setTimeout(function() {
			WidgetFactory.ensureDicoBitLoaded(rdfSymbol, terminationListener);
		}, 10);
	} else {
		if (typeof(WidgetFactory.store.anyStatementMatching(rdfSymbol)) == 'undefined') {
			WidgetFactory.isLoading = true;
			WidgetFactory.store = WidgetFactory.removeContext(rdfSymbol, WidgetFactory.store);
			WidgetFactory.load(rdfSymbol, WidgetFactory.store, function(store) {
				WidgetFactory.isLoading = false;
				terminationListener(store);
			});
		} else {
			terminationListener();
		}
	}
}

WidgetFactory.createURIderefURL = function(uri) {
	return uri;
}
/**
* returns the context of a resource as an aaray of statements
*/
WidgetFactory.getContext = function(rdfSymbol, store, pResult) {
	if (pResult) {
		var result = pResult;
	} else {
		var result = [ ];
	}
	var directedExpander = function(directioner) {
		for (var i = 0; i < statements.length; i++) {
			if (result.contains(statements[i])) {
				continue;
			}
			result.push(statements[i]);
			var other = directioner(statements[i]);
			if (other.termType == 'bnode') {
				WidgetFactory.getContext(other, store, result);
			}
		}
	}
	var statements = store.statementsMatching(rdfSymbol, undefined, undefined);
	directedExpander(function(statement) {
		return statement.object;
	});
	statements = store.statementsMatching(undefined, undefined, rdfSymbol);
	directedExpander(function(statement) {
		return statement.subject;
	});
	return result;
}

WidgetFactory.removeContext = function(rdfSymbol, store) {
	var result = new RDFIndexedFormula();
	var blockStatements = WidgetFactory.getContext(rdfSymbol, store);
	var statements = store.statementsMatching(undefined, undefined, undefined);
	for (var i = 0; i < statements.length; i++) {
		if (!blockStatements.contains(statements[i])) {
			result.statements.push(statements[i]);
		}
	}
	return result;
}


WidgetFactory.load = function(rdfSymbol, pStore, terminationListener) {
	var store;
	if (pStore) {
		store = pStore;
	} else {
		store = new RDFIndexedFormula();
	}
	var parser = new RDFParser(store);
	parser.reify = parser.forceRDF = true;
	// forceRDF isn't used??
	
	
	// var url = 'http://something.or/other';
	
	// get the XML
	var xhr = Util.XMLHTTPFactory(); // returns a new XMLHttpRequest, or ActiveX XMLHTTP object
	if (xhr.overrideMimeType) {
	    xhr.overrideMimeType("text/xml");
	}
	// the "data/" path and encoding is just how I store files locally
	xhr.onload = function() {
		var nodeTree = xhr.responseXML;
		if (nodeTree === null && xhr.responseText !== null) {
		    // Only if the server fails to set Content-Type: text/xml AND xmlhttprequest doesn't have the overrideMimeType method
		    nodeTree = (new DOMParser()).parseFromString(xhr.responseText, 'text/xml');
		}
		// must be an XML document node tree
		parser.parse(nodeTree,rdfSymbol.uri);
		terminationListener(store);	
	};
	xhr.open("GET", WidgetFactory.createURIderefURL(rdfSymbol.uri), true);
        xhr.setRequestHeader("Accept", "application/rdf+xml");
	// xhr.open("GET", "data/" + encodeURIComponent(encodeURIComponent(url)), false);
	xhr.send("");
	
	
}
WidgetFactory.store = new RDFIndexedFormula();//WidgetFactory.load(document.location.toString().substring(0, document.location.toString().lastIndexOf('/')+1)+'sample1b.rdf');

Array.prototype.contains = function(element) {
  for(var i = 0; i < this.length; i++) {
    if (this[i] == element) {
      return true;
    }
  }
  return false;
}


//////////////////




