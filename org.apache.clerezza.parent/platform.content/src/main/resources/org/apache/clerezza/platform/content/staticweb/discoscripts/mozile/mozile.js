/* ***** BEGIN LICENSE BLOCK *****
 * Licensed under Version: MPL 1.1/GPL 2.0/LGPL 2.1
 * Full Terms at http://mozile.mozdev.org/0.8/LICENSE
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is James A. Overton's code (james@overton.ca).
 *
 * The Initial Developer of the Original Code is James A. Overton.
 * Portions created by the Initial Developer are Copyright (C) 2005-2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *	James A. Overton <james@overton.ca>
 *
 * ***** END LICENSE BLOCK ***** */

/*
 * Comments and white space have been removed from this file to reduce its size.
 * For full source code see http://mozile.mozdev.org
 */

/*
 * This file includes the following Mozile modules: 
 * core.js, dom.js, xml.js, xpath.js, util.js, dom/TreeWalker.js, dom/InternetExplorerRange.js, dom/InternetExplorerSelection.js, rng.js, edit.js, edit/InsertionPoint.js, edit/rich.js, event.js, save.js, save/tidy.js, save/extract.js, gui.js, gui/htmlToolbar.js
 */

var mozile={window:window,document:document,filesep:"/",linesep:"\n",root:""}
mozile.version={major:"0",minor:"8",micro:"0",flag:"a1",toString:function(){return this.major+"."+this.minor+"."+this.micro+this.flag;}}
mozile.about="Mozile is a WYSIWYG inline edtior for XHTML and XML+CSS files.";mozile.copyright="Copyright 2006, The Mozile Team";mozile.license="MPL 1.1/GPL 2.0/LGPL 2.1, http://mozile.mozdev.org/0.8/LICENSE";mozile.homepage="http://mozile.mozdev.org";mozile.credits="James A. Overton, Conor Dowling, Max d'Ayala, Christian Stocker, Paul Everitt, David Palm, Richard Prescott, Lars Kiilerich, Kingsley Kerse, Tobias Minich, Andreas Schamberger, and others...";mozile.acknowledgements="Icons created by Mark James <http://www.famfamfam.com/lab/icons/silk/>";mozile.precompiled=true;mozile.deferRequirements=true;mozile.help=["doc","html","index.html"].join(mozile.filesep);mozile.useDesignMode=false;mozile.defaultNS=null;mozile.emptyToken="\u00A0";mozile.alternateSpace=null;mozile.updateInterval=500;mozile.load=function(uri){var XHR;try{if(window.XMLHttpRequest){XHR=new XMLHttpRequest();XHR.open("GET",uri,false);XHR.send(null);}
else if(window.ActiveXObject){XHR=new ActiveXObject('Microsoft.XMLHTTP');XHR.open("GET",uri,false);XHR.send();}}catch(e){if(mozile.debug)mozile.debug.inform("mozile.load","File load failed loading '"+uri+"' with error message:\n"+e);return null;}
if(XHR){if(XHR.status==0||XHR.status==200)return XHR.responseText;else{if(mozile.debug)mozile.debug.inform("mozile.load","File load failed with status '"+XHR.status+"' and message:\n"+XHR.responseText);return null;}}
if(mozile.debug)mozile.debug.inform("mozile.load","No XMLHttpRequest available when trying to load '"+uri+"'.");return null;}
mozile.findRoot=function(){var scripts=document.getElementsByTagName("script");var matches=["mozile.js","mozile-src.js"];var src,index,i,j;for(i=0;i<scripts.length;i++){src=scripts[i].getAttribute("src");if(!src)continue;for(j=0;j<matches.length;j++){index=src.indexOf(matches[j]);if(index>-1)return src.substring(0,index);}}
return"";}
mozile.root=mozile.findRoot();mozile.findModule=function(moduleName){var levels=moduleName.split(".");var current=mozile.window;for(var i=0;i<levels.length;i++){if(!current[levels[i]])return null;current=current[levels[i]];}
return current;}
mozile.loadModule=function(moduleName){if(!moduleName)return false;var filename=mozile.root+mozile.filesep;var levels=moduleName.split(".");for(var i=0;i<levels.length;i++){if(levels[i]=="*")levels=levels.splice(i,1);}
if(levels[0]&&levels[0]=="mozile"){levels.shift();if(levels[0]&&levels[0]=="test"){levels.shift();filename+="tests"+mozile.filesep;}
else filename+="src"+mozile.filesep;}
else return false;if(levels.length==0)return false;while(levels.length>1){filename+=levels.shift()+mozile.filesep;}
filename+=levels.pop()+".js";var file=mozile.load(filename);if(!file&&mozile.test&&mozile.test.root){filename=filename.replace(mozile.root,mozile.test.root);file=mozile.load(filename);}
if(!file){var parent=moduleName.substring(0,moduleName.lastIndexOf("."));return mozile.loadModule(parent);}
try{eval(file);return true;}catch(e){mozile.debug.inform("mozile.loadModule","Error evaluating module '"+moduleName+"' in file '"+filename+"'.\n"+mozile.dumpError(e));return false;}}
mozile.deferrals=new Array();mozile.require=function(moduleName){if(!mozile.findModule(moduleName)){if(mozile.deferRequirements){mozile.deferrals.push(moduleName);return false;}
return mozile.loadModule(moduleName);}
else return false;}
mozile.loadDeferred=function(){while(mozile.deferrals.length){mozile.require(mozile.deferrals.shift());}}
mozile.provide=function(){}
mozile.Module=function(){};mozile.isPathAbsolute=function(path){if(!path)return false;path=path.toString();if(path.indexOf(":")==-1)return false;var protocol=path.substring(0,path.indexOf(":"));switch(protocol){case"http":case"https":case"file":return true;}
return false;}
mozile.getPath=function(url){if(!url)return"";url=url.toString();if(url.indexOf("?")>-1)url=url.substring(0,url.indexOf("?"));return url;}
mozile.getDirectory=function(path){if(!path)return"";path=path.toString();path=mozile.getPath(path);var partial;if(path.indexOf(mozile.filesep)>-1){if(path.indexOf(":///")>-1){partial=path.substring(path.indexOf(":///")+4);if(partial.indexOf(mozile.filesep)==-1)return path;}
else if(path.indexOf("://")>-1){partial=path.substring(path.indexOf("://")+3);if(partial.indexOf(mozile.filesep)==-1)return path;}
return path.substring(0,path.lastIndexOf(mozile.filesep)+1);}
return path;}
mozile.getAbsolutePath=function(path,root){if(!path)return"";path=path.toString();if(mozile.isPathAbsolute(path))return path;if(!root)root=location;root=mozile.getDirectory(root);root=mozile.getAbsolutePath(root);return mozile.joinPaths(root,path);}
mozile.joinPaths=function(){var path="";var part;for(var i=0;i<arguments.length;i++){part=arguments[i];if(typeof(part)!="string")continue;if(path){if(path.charAt(path.length-1)==mozile.filesep){if(part.charAt(0)==mozile.filesep)
part=part.substring(1);}
else{if(part.charAt(0)!=mozile.filesep)
part=mozile.filesep+part;}}
path+=part;}
return path;}
mozile.debug=new Object();mozile.debug.prototype=new mozile.Module;mozile.debug.alertLevel="suppress";mozile.debug.logLevel="inform";mozile.debug.messages=new Array();mozile.debug.window=null;mozile.debug.isSelected=function(type,level){if(typeof(type)!="string")return false;type=type.toLowerCase();var checkLevel;if(type=="alert")checkLevel=mozile.debug.alertLevel;else if(type=="log")checkLevel=mozile.debug.logLevel;else return false;checkLevel=checkLevel.toLowerCase();if(typeof(level)!="string")return false;level=level.toLowerCase();if(checkLevel=="suppress")return false;if(checkLevel=="warn"){if(level=="warn")return true;else return false;}
if(checkLevel=="inform"){if(level=="warn"||level=="inform")return true;else return false;}
if(checkLevel=="debug")return true;return false;}
mozile.debug.warn=function(caller,message){var level="warn";var msg="Mozile Warning ["+caller+"] "+message;if(mozile.debug.isSelected("alert",level)){if(window.warn)warn(msg);else mozile.alert(msg);}
if(mozile.debug.isSelected("log",level)){mozile.debug.log(caller,level,message);}}
mozile.debug.inform=function(caller,message){var level="inform";var msg="Mozile Information ["+caller+"] "+message;if(mozile.debug.isSelected("alert",level)){if(window.inform)inform(msg);else mozile.alert(msg);}
if(mozile.debug.isSelected("log",level)){mozile.debug.log(caller,level,message);}}
mozile.debug.debug=function(caller,message){var level="debug";var msg="Mozile Debugging ["+caller+"] "+message;if(mozile.debug.isSelected("alert",level)){if(window.debug)debug(msg);else mozile.alert(msg);}
if(mozile.debug.isSelected("log",level)){mozile.debug.log(caller,level,message);}}
mozile.debug.log=function(caller,level,message){var date=new Date();var msg={caller:caller,level:level,message:message,date:date.toLocaleString(),toString:function(){return this.level.toUpperCase()+" ("+this.date+") ["+this.caller+"] "+this.message;}};mozile.debug.messages.push(msg);if(mozile.debug.window&&mozile.debug.window.document){mozile.debug.window.document.write(msg+"<br/>\n");mozile.debug.window.scroll(0,document.body.clientHeight);}}
mozile.debug.show=function(){if(!mozile.debug.window||!mozile.debug.window.document){mozile.debug.window=window.open("","MozileDebugging","");mozile.debug.window.document.write("<h3>Mozile Debugging Messages</h3>");mozile.debug.window.document.write(mozile.debug.messages.join("<br/>\n")+"<br/>\n");}
else mozile.debug.window=window.open("","MozileDebugging","");}
mozile.alert=function(message){alert(message);}
mozile.dumpError=function(object){if(typeof(object)=="string")return object;if(!mozile.browser.isIE)return object.toString();var fields;if(object&&object.description){fields=["Name: "+object.name,"Number: "+object.number,"Message: "+object.message,"Description: "+object.description,];return fields.join("\n");}
if(!object)object=document;if(!object.parseError)object=document;if(!object.parseError)object=window;if(!object.parseError)return"[No error to parse]";fields=["Error Code: "+object.parseError.errorCode,"File Position: "+object.parseError.filepos,"Line: "+object.parseError.line,"Line Position: "+object.parseError.linepos,"Reason: "+object.parseError.reason,"Source Text: "+object.parseError.srcText,"Url: "+object.parseError.url];return fields.join("\n");}
mozile.editElement=function(elementOrId){var element;if(typeof(elementOrId)=="string"){if(document.documentElement.nodeName.toLowerCase()=="html"){element=document.getElementById(elementOrId);}
else{mozile.require("mozile.dom");var results=mozile.dom.getElements("id",elementOrId,null,true);if(results.length)element=results[0];else return false;}}
else if(elementOrId.nodeType&&elementOrId.nodeType==mozile.dom.ELEMENT_NODE){element=elementOrId;}
if(element)return mozile._markEditable(element,true);return false;}
mozile.editElements=function(listOrValue,name){var list;if(typeof(listOrValue)=="string"){mozile.require("mozile.dom");list=mozile.dom.getElements(name,listOrValue);}
else if(listOrValue.length){list=listOrValue;}
if(list.length){for(var i=0;i<list.length;i++){if(list[i]&&list[i].nodeType&&list[i].nodeType==mozile.dom.ELEMENT_NODE){mozile.editElement(list[i]);}}}
return undefined;}
mozile.editDocument=function(doc){if(!doc)doc=document;mozile.editElement(doc.documentElement);}
mozile.protectElement=function(elementOrId){var element;if(typeof(elementOrId)=="string"){if(document.documentElement.nodeName.toLowerCase()=="html"){element=document.getElementById(elementOrId);}
else{mozile.require("mozile.dom");var results=mozile.dom.getElements("id",elementOrId,null,true);if(results.length)element=results[0];else return false;}}
else if(elementOrId.nodeType&&elementOrId.nodeType==mozile.dom.ELEMENT_NODE){element=elementOrId;}
if(element)return mozile._markEditable(element,false);return false;}
mozile.protectElements=function(listOrValue,name){var list;if(typeof(listOrValue)=="string"){mozile.require("mozile.dom");list=mozile.dom.getElements(name,listOrValue);}
else if(listOrValue.length){list=listOrValue;}
if(list.length){for(var i=0;i<list.length;i++){if(list[i]&&list[i].nodeType&&list[i].nodeType==mozile.dom.ELEMENT_NODE){mozile.protectElement(list[i]);}}}
return undefined;}
mozile._markEditable=function(element,value){if(!element)return null;mozile.require("mozile.edit");if(value===true&&mozile.edit.setMark(element,"editable")===false&&mozile.edit.getContainer(element.parentNode)){mozile.edit.setMark(element,"editable",undefined);if(mozile.edit.getMark(element,"contentEditable")==undefined){element.removeAttribute("contentEditable");}
return value;}
mozile.edit.setMark(element,"editable",value);if(mozile.edit.getMark(element,"contentEditable")==undefined){switch(element.getAttribute("contentEditable")){case"true":mozile.edit.setMark(element,"contentEditable",true);break;case"false":mozile.edit.setMark(element,"contentEditable",false);break;}}
element.setAttribute("contentEditable",String(value));return value;}
mozile.enableEditing=function(rich){mozile.require("mozile.edit");mozile.edit.defaults.addCommand(mozile.edit.navigateLeftRight);mozile.edit.defaults.addCommand(mozile.edit.insertText);mozile.edit.defaults.addCommand(mozile.edit.removeText);if(rich){mozile.require("mozile.edit.rich");mozile.edit.defaults.addCommand(mozile.edit.remove);var splitBlocks=new mozile.edit.Split("splitBlocks");splitBlocks.accel="Return Enter";mozile.edit.defaults.addCommand(splitBlocks);}}
mozile.useSchema=function(target){try{mozile.require("mozile.rng");mozile.require("mozile.edit");mozile.require("mozile.edit.rich");mozile.edit.extendRNG();mozile.schema=new mozile.rng.Schema();var validation=mozile.schema.parse(target);if(validation.isValid){mozile.edit.generateCommands(mozile.schema);return true;}
else{mozile.debug.inform("mozile.useSchema","Schema validation failed.\n"+validation.report(true));return false;}}catch(e){mozile.debug.inform("mozile.useSchema","Could not create schema for target '"+target+"' because of an error:\n"+mozile.dumpError(e));return false;}}
mozile.os=new Object();mozile.os.prototype=new mozile.Module;mozile.os.isMac=false;if(navigator.userAgent.match(/Macintosh/))mozile.os.isMac=true;mozile.browser=new Object();mozile.browser.prototype=new mozile.Module;mozile.browser.isMozilla=false;mozile.browser.mozillaVersion=0;mozile.browser.mozillaVersion=navigator.userAgent.match(/rv\:(\d+\.\d+)/);if(mozile.browser.mozillaVersion&&Number(mozile.browser.mozillaVersion[1])){mozile.browser.isMozilla=true;mozile.browser.mozillaVersion=Number(mozile.browser.mozillaVersion[1]);}
mozile.browser.isIE=false;if(navigator.userAgent.match(/MSIE/))mozile.browser.isIE=true;mozile.browser.isSafari=false;if(navigator.userAgent.match(/Safari/))mozile.browser.isSafari=true;mozile.browser.safariVersion=0;mozile.browser.safariVersion=navigator.userAgent.match(/AppleWebKit\/(\d+)/);if(mozile.browser.safariVersion&&Number(mozile.browser.safariVersion[1])){mozile.browser.safariVersion=Number(mozile.browser.safariVersion[1]);}
mozile.browser.isOpera=false;if(navigator.userAgent.match(/Opera/))mozile.browser.isOpera=true;if(mozile.browser.isMozilla){if(document.contentType&&document.contentType=="text/html"){mozile.defaultNS=null;}
else{mozile.defaultNS="http://www.w3.org/1999/xhtml";}}
mozile.dom=new Object();mozile.dom.prototype=new mozile.Module;mozile.dom.ELEMENT_NODE=1;mozile.dom.ATTRIBUTE_NODE=2;mozile.dom.TEXT_NODE=3;mozile.dom.CDATA_SECTION_NODE=4;mozile.dom.ENTITY_REFERENCE_NODE=5;mozile.dom.ENTITY_NODE=6;mozile.dom.PROCESSING_INSTRUCTION_NODE=7;mozile.dom.COMMENT_NODE=8;mozile.dom.DOCUMENT_NODE=9;mozile.dom.DOCUMENT_TYPE_NODE=10;mozile.dom.DOCUMENT_FRAGMENT_NODE=11;mozile.dom.NOTATION_NODE=12;mozile.dom.links=new Array();mozile.dom.getBody=function(node){var doc=document;if(node)doc=node.ownerDocument;var elements=doc.getElementsByTagName("body");if(elements&&elements[0])return elements[0];else return doc.documentElement;}
mozile.dom.getHead=function(node){var doc=document;if(node)doc=node.ownerDocument;var elements=doc.getElementsByTagName("head");if(elements&&elements[0])return elements[0];else return doc.documentElement;}
mozile.dom.getFirstChildElement=function(parent){for(var i=0;i<parent.childNodes.length;i++){if(parent.childNodes[i].nodeType==mozile.dom.ELEMENT_NODE)
return parent.childNodes[i];}
return null;}
mozile.dom.getChildElements=function(parent){var children=new Array();for(var i=0;i<parent.childNodes.length;i++){if(parent.childNodes[i].nodeType==mozile.dom.ELEMENT_NODE)
children.push(parent.childNodes[i]);}
return children;}
mozile.dom.getNextSiblingElement=function(node){var sibling=node.nextSibling;while(sibling){if(sibling.nodeType==mozile.dom.ELEMENT_NODE)return sibling;sibling=sibling.nextSibling;}
return null;}
mozile.dom.getElements=function(attr,value,root,single){var list=new Array();if(!attr)attr="class";if(!root)root=document.documentElement;if(document.createTreeWalker){var treeWalker=document.createTreeWalker(root,mozile.dom.NodeFilter.SHOW_ELEMENT,null,false);var node=treeWalker.currentNode;while(node){if(attr=="class"&&mozile.dom.hasClass(node,value))
list.push(node);else if(attr!="local name"&&node.getAttribute(attr)){if(!value)list.push(node);else if(node.getAttribute(attr)==value)list.push(node);}
else if(attr=="local name"&&mozile.dom&&mozile.dom.getLocalName(node).toLowerCase()==value){list.push(node);}
if(single&&list.length>0)break;node=treeWalker.nextNode();}}
return list;}
mozile.dom.getText=function(node){if(!node)return"";if(node.nodeType==mozile.dom.TEXT_NODE&&!mozile.dom.isWhitespace(node)){return node.data;}
else if(node.nodeType==mozile.dom.ATTRIBUTE_NODE){return node.nodeValue;}
else if(node.nodeType==mozile.dom.ELEMENT_NODE){var text="";for(var i=0;i<node.childNodes.length;i++){text+=mozile.dom.getText(node.childNodes[i]);}
return text;}
return"";}
mozile.dom.insertAfter=function(newNode,refNode){if(!newNode)throw("Error [mozile.dom.insertAfter]: No newNode. newNode: "+newNode+" refNode: "+refNode);if(!refNode)throw("Error [mozile.dom.insertAfter]: No refNode. newNode: "+newNode+" refNode: "+refNode);var parentNode=refNode.parentNode;if(!parentNode)return null;if(refNode.nextSibling)return parentNode.insertBefore(newNode,refNode.nextSibling);else return parentNode.appendChild(newNode);}
mozile.dom.prependChild=function(newNode,parentNode){if(parentNode.firstChild)return parentNode.insertBefore(newNode,parentNode.firstChild);else return parentNode.appendChild(newNode);}
mozile.dom.isAncestorOf=function(ancestorNode,descendantNode,limitNode){var checkNode=descendantNode;while(checkNode){if(checkNode==ancestorNode)return true;else if(checkNode==limitNode)return false;else checkNode=checkNode.parentNode;}
return false;}
mozile.dom.getCommonAncestor=function(firstNode,secondNode){var ancestor=firstNode;while(ancestor){if(mozile.dom.isAncestorOf(ancestor,secondNode))return ancestor;else ancestor=ancestor.parentNode;}
return null;}
mozile.dom._matchNonWhitespace=/\S/m;mozile.dom.isWhitespace=function(node){if(!node||!node.nodeValue)return false;if(node.nodeValue.match(mozile.dom._matchNonWhitespace))return false;return true;}
mozile.dom.isVisible=function(node){var visibility;while(node){if(node==document)return true;if(node.nodeType==mozile.dom.ELEMENT_NODE){if(mozile.dom.getStyle(node,"display")=="none")return false;visibility=mozile.dom.getStyle(node,"visibility");if(visibility=="hidden"||visibility=="collapse")return false;}
node=node.parentNode;}
return false;}
mozile.dom.isHTML=function(node){if(!node)node=document;var doc=node;if(node.ownerDocument)doc=node.ownerDocument;if(!doc.documentElement)return false;var name=doc.documentElement.nodeName;if(doc.documentElement.nodeName)name=doc.documentElement.nodeName;if(name.toLowerCase()=="html")return true;else return false;}
mozile.dom.isIgnored=function(node){if(node.nodeType==mozile.dom.ATTRIBUTE_NODE){if(node.nodeName.indexOf("xmlns")==0)return true;if(mozile.browser.isOpera&&node.nodeName.toLowerCase()=="shape")return true;}
return false;}
mozile.dom.getLocalName=function(node){if(!node)return null;if(node.localName)return node.localName;else if(node.nodeName&&node.nodeName.indexOf(":")>-1)
return node.nodeName.substring(node.nodeName.indexOf(":")+1);else return node.nodeName;}
mozile.dom.getPrefix=function(node){if(!node)return null;if(node.prefix)return node.prefix;else if(node.nodeName.indexOf(":")>-1)
return node.nodeName.substring(0,node.nodeName.indexOf(":"));else return null;}
mozile.dom.getIndex=function(node){if(!node.parentNode)return null;for(var c=0;c<node.parentNode.childNodes.length;c++){if(node.parentNode.childNodes[c]==node)return c;}
return c;}
mozile.dom.getPosition=function(node){if(!node.parentNode)return null;var s=1;for(var c=0;c<node.parentNode.childNodes.length;c++){if(node.parentNode.childNodes[c]==node)break;else if(node.nodeType==mozile.dom.ELEMENT_NODE){if(node.parentNode.childNodes[c].nodeName==node.nodeName)s++;}
else if(node.parentNode.childNodes[c].nodeType==node.nodeType)s++;}
return s;}
mozile.dom.removeChildNodes=function(node){if(node.childNodes.length==0)return;while(node.firstChild){node.removeChild(node.firstChild);}}
mozile.dom.getNamespaceURI=function(node){if(!node)return null;if(node.namespaceURI)return node.namespaceURI;else if(node.nodeName.indexOf(":")>-1){var prefix=node.nodeName.substring(0,node.nodeName.indexOf(":"));return mozile.dom.lookupNamespaceURI(node,prefix);}
return mozile.dom.getDefaultNamespaceURI(node);}
mozile.dom.getDefaultNamespaceURI=function(node){var namespaceURI=null;while(node){if(node.nodeType==mozile.dom.ELEMENT_NODE&&node.getAttribute("xmlns"))
return node.getAttribute("xmlns");node=node.parentNode;}
return namespaceURI;}
mozile.dom.lookupNamespaceURI=function(node,prefix){if(!prefix)prefix=mozile.dom.getPrefix(node);var attr="xmlns";if(prefix&&node.lookupNamespaceURI){while(node){var ns=node.lookupNamespaceURI(prefix);if(ns)return ns;else node=node.parentNode;}
return null;}
if(prefix&&mozile.browser.isSafari){while(node&&node.getAttributeNS){if(node.getAttributeNS(attr,prefix))return node.getAttributeNS(attr,prefix);else node=node.parentNode;}
return null;}
if(prefix)attr="xmlns:"+prefix;while(node){if(node.getAttribute(attr))return node.getAttribute(attr);else node=node.parentNode;}
return null;}
mozile.dom.lookupPrefix=function(node,namespaceURI){if(!namespaceURI)namespaceURI=node.namespaceURI;if(!namespaceURI)return null;while(node&&node.attributes){for(var i=0;i<node.attributes.length;i++){if(node.attributes[i].nodeName.indexOf("xmlns:")==0&&node.attributes[i].nodeValue==namespaceURI){return node.attributes[i].nodeName.substring(6);}}
node=node.parentNode;}
return null;}
mozile.dom.convertStyleName=function(styleName){if(!styleName||typeof(styleName)!="string")return null;return styleName.replace(/\-(\w)/g,function(strMatch,p1){return p1.toUpperCase();});}
mozile.dom.getStyle=function(node,cssRule){var value="";if(!node)return value;if(node.nodeType!=mozile.dom.ELEMENT_NODE)node=node.parentNode;if(!node||node.nodeType!=mozile.dom.ELEMENT_NODE)return value;if(document.defaultView&&document.defaultView.getComputedStyle){value=document.defaultView.getComputedStyle(node,"").getPropertyValue(cssRule);}
else if(node.currentStyle){cssRule=mozile.dom.convertStyleName(cssRule);value=node.currentStyle[cssRule];}
return value;}
mozile.dom.setStyle=function(element,rule,value){if(!element)return;if(!rule||typeof(rule)!="string")return;rule=mozile.dom.convertStyleName(rule);if(element.style)element.style[rule]=value;else mozile.debug.debug("mozile.dom.setStyle","Element does not have a 'style' attribute.");}
mozile.dom.addStyleSheet=function(href,type){var link;if(mozile.defaultNS!=null){mozile.require("mozile.xml");link=mozile.dom.createElementNS(mozile.xml.ns.xhtml,"link");}
else link=mozile.dom.createElement("link");link.setAttribute("class","mozileLink");link.setAttribute("href",href);link.setAttribute("rel","stylesheet");if(!type)type="text/css";link.setAttribute("type",type);mozile.dom.getHead().appendChild(link);mozile.dom.links.push(link);return link;}
mozile.dom.getStyleSheet=function(element){if(!element)return null;if(element.styleSheet!=undefined)return element.styleSheet;else if(element.sheet!=undefined)return element.sheet;return null;}
mozile.dom.getClass=function(element){if(!element||element.nodeType!=mozile.dom.ELEMENT_NODE)return"";var value;if(element.className!=undefined)value=element.className;else value=element.getAttribute("class");if(value)return value;else return"";}
mozile.dom.setClass=function(element,value){if(!element||element.nodeType!=mozile.dom.ELEMENT_NODE)return null;if(!value||typeof(value)!="string")return null;if(element.className!=undefined)element.className=value;else element.setAttribute("class",value);return value;}
mozile.dom.hasClass=function(element,className){if(!element)return false;var attr=mozile.dom.getClass(element);if(!attr)return false;if(!className)return true;attr=attr.split(/\s+/);for(var i=0;i<attr.length;i++){if(attr[i]==className)return true;}
return false;}
mozile.dom.getX=function(node,container){if(!node)return 0;if(node.nodeType!=mozile.dom.ELEMENT_NODE)node=node.parentNode;if(!node)return 0;var x=0;if(node.offsetParent){while(node.offsetParent){x+=node.offsetLeft;node=node.offsetParent;if(node==container)break;}}
else if(node.x)x+=node.x;return x;}
mozile.dom.getY=function(node,container){if(!node)return 0;if(node.nodeType!=mozile.dom.ELEMENT_NODE)node=node.parentNode;if(!node)return 0;var y=0;if(node.offsetParent){while(node.offsetParent){y+=node.offsetTop;node=node.offsetParent;if(node==container)break;}}
else if(node.y)y+=node.y;return y;}
mozile.dom.getAttributeNS=function(element,namespaceURI,name){if(!element)return null;if(element.getAttributeNS)return element.getAttributeNS(namespaceURI,name);else{prefix=mozile.dom.lookupPrefix(element,namespaceURI);if(prefix)return element.getAttribute(prefix+":"+name);}
return null;}
mozile.dom.createElement=function(name){if(mozile.defaultNS){return mozile.dom.createElementNS(mozile.defaultNS,name);}else{return document.createElement(name);}}
mozile.dom.createElementNS=function(namespaceURI,name){if(document.createElementNS&&!mozile.browser.isSafari)return document.createElementNS(namespaceURI,name);else{mozile.require("mozile.xml");return mozile.xml.parseElement('<'+name+' xmlns="'+namespaceURI+'"/>');}
return null;}
mozile.dom.importNode=function(node,deep){if(!node||!node.nodeType)return null;var clone;var i=0;switch(node.nodeType){case mozile.dom.ELEMENT_NODE:clone=mozile.dom.createElement(node.nodeName);for(i=node.attributes.length-1;i>=0;i--){clone.setAttribute(node.attributes[i].nodeName,node.attributes[i].nodeValue);}
break;case mozile.dom.TEXT_NODE:clone=document.createTextNode(node.data);break;case mozile.dom.COMMENT_NODE:clone=document.createComment(node.data);break;default:return null;}
if(deep){var child;for(i=0;i<node.childNodes.length;i++){child=mozile.dom.importNode(node.childNodes[i],true);if(child)clone.appendChild(child);}}
return clone;}
mozile.dom.Range=function(selectRange){var range;if(document.createRange){if(selectRange)range=selectRange.cloneRange();else range=document.createRange();for(var field in mozile.dom.Range.prototype)
range[field]=mozile.dom.Range.prototype[field];return range;}else{if(selectRange&&selectRange._range){range=new mozile.dom.InternetExplorerRange(selectRange._range.duplicate());range._init();return range;}
else return new mozile.dom.InternetExplorerRange();}}
mozile.dom.Range.prototype.store=function(){var state=new Object();if(false&&this._range){state.format="IE";state.bookmark=this._range.getBookmark();}
else{mozile.require("mozile.xpath");state.type="Range";state.format="W3C";state.startContainer=mozile.xpath.getXPath(this.startContainer);state.startOffset=this.startOffset;if(this.startContainer==this.endContainer)
state.endContainer=state.startContainer;else state.endContainer=mozile.xpath.getXPath(this.endContainer);state.endContainer=mozile.xpath.getXPath(this.endContainer);state.endOffset=this.endOffset;}
return state;}
mozile.dom.Range.prototype.restore=function(state){try{if(false&&this._range){this._range.moveToBookmark(state.bookmark);this._init();}
else{mozile.require("mozile.xpath");var startContainer,endContainer;startContainer=mozile.xpath.getNode(state.startContainer);this.setStart(startContainer,state.startOffset);if(state.endContainer==state.startContainer)
endContainer=startContainer;else endContainer=mozile.xpath.getNode(state.endContainer);this.setEnd(endContainer,state.endOffset);}}catch(e){alert("Error [mozile.dom.Range.restore]:\n"+mozile.dumpError(e))}}
mozile.dom.selection=new Object();mozile.dom.selection.prototype=new mozile.Module;mozile.dom.selection.xPadding=50;mozile.dom.selection.yPadding=100;mozile.dom.selection.get=function(){if(!mozile.dom._selection)mozile.dom._selection=new mozile.dom.Selection();else if(mozile.browser.isIE)mozile.dom._selection._init();return mozile.dom._selection;}
mozile.dom.Selection=function(){if(window.getSelection){var selection=window.getSelection();for(var field in mozile.dom.Selection.prototype)
selection[field]=mozile.dom.Selection.prototype[field];return selection;}else{return new mozile.dom.InternetExplorerSelection();}}
mozile.dom.Selection.prototype.store=function(oldState,newOffset){var state=new Object();if(oldState){for(var i in oldState)state[i]=oldState[i];state.anchorOffset=newOffset;state.focusOffset=newOffset;return state;}
else if(this.rangeCount>0){mozile.require("mozile.xpath");state.type="Selection";state.format="W3C";state.anchorNode=mozile.xpath.getXPath(this.anchorNode);state.anchorOffset=this.anchorOffset;if(this.focusNode==this.anchorNode)state.focusNode=state.anchorNode;else state.focusNode=mozile.xpath.getXPath(this.focusNode);state.focusOffset=this.focusOffset;state.isCollapsed=this.isCollapsed;return state;}
return null;}
mozile.dom.Selection.prototype.restore=function(state){if(state){if(state.type=="Selection"){mozile.require("mozile.xpath");var anchorNode,focusNode;anchorNode=mozile.xpath.getNode(state.anchorNode);this.collapse(anchorNode,state.anchorOffset);if(state.focusNode!=state.anchorNode||state.focusOffset!=state.anchorOffset){if(state.focusNode==state.anchorNode)focusNode=anchorNode;else focusNode=mozile.xpath.getNode(state.focusNode);try{this.extend(focusNode,state.focusOffset);}catch(e){mozile.debug.debug("mozile.dom.Selection.restore","Error extending selection to '"+state.focusNode+" "+state.focusOffset+"'.\n"+mozile.dumpError(e));}}}
else if(state.type=="Range"){var range=new mozile.dom.Range();range.restore(state);this.removeAllRanges();this.addRange(range);}}
else if(mozile.dom.selection.last){this.collapse(mozile.dom.selection.last.anchorNode,mozile.dom.selection.last.anchorOffset);if(!mozile.dom.selection.last.isCollapsed){this.extend(mozile.dom.selection.last.focusNode,mozile.dom.selection.last.focusOffset);}}}
mozile.dom.Selection.prototype.scroll=function(){if(!this.focusNode);var x=mozile.dom.getX(this.focusNode);var y=mozile.dom.getY(this.focusNode);var pX=window.pageXOffset;var pY=window.pageYOffset;if(x<pX||x>(pX+window.innerWidth)||y<pY||y>(pY+window.innerHeight)){window.scroll(x-mozile.dom.selection.xPadding,y-mozile.dom.selection.yPadding);}}
mozile.dom.Selection.getSelection=function(){if(window.getSelection){return window.getSelection();}else if(document.getSelection){return document.getSelection();}else if(document.selection){return new mozile.dom.Selection();}
return null;}
if(mozile.browser.isIE){mozile.require("mozile.dom.TreeWalker");mozile.require("mozile.dom.InternetExplorerRange");mozile.require("mozile.dom.InternetExplorerSelection");}
else{mozile.dom.NodeFilter=NodeFilter;mozile.dom.TreeWalker=TreeWalker;}
mozile.xml=new Object();mozile.xml.prototype=new mozile.Module;mozile.xml.ns={AdobeExtensions:"http://ns.adobe.com/AdobeSVGViewerExtensions/3.0/",atom:"http://www.w3.org/2005/Atom",cml:"http://www.xml-cml.org",dc:"http://purl.org/dc/elements/1.1/",dcq:"http://purl.org/dc/qualifiers/1.0",dt:"http://www.w3.org/2001/XMLSchema-datatypes",fo:"http://www.w3.org/1999/XSL/Format",mes:"http://mozile.mozdev.org/ns/mes/1.0",mml:"http://www.w3.org/1998/Math/MathML",rdf:"http://www.w3.org/1999/02/22-rdf-syntax-ns#",rdfs:"http://www.w3.org/2000/01/rdf-schema#",rng:"http://relaxng.org/ns/structure/1.0",saxon:"http://icl.com/saxon","soap-env":"http://schemas.xmlsoap.org/soap/envelope/",smil:"http://www.w3.org/2001/SMIL20/",svg:"http://www.w3.org/2000/svg",wsdl:"http://schemas.xmlsoap.org/wsdl/",xalan:"http://xml.apache.org/xslt",xbl:"http://www.mozilla.org/xbl",xforms:"http://www.w3.org/2002/01/xforms",xhtml:"http://www.w3.org/1999/xhtml",xi:"http://www.w3.org/2001/XInclude",xlink:"http://www.w3.org/1999/xlink",xsd:"http://www.w3.org/2001/XMLSchema",xsi:"http://www.w3.org/2001/XMLSchema-instance",xsl:"http://www.w3.org/1999/XSL/Transform",xslt:"http://www.w3.org/1999/XSL/Transform",xul:"http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul"};mozile.xml._IE_DOM=null;mozile.xml._IE_THREADEDDOM=null;mozile.xml._IE_XSLTEMPLATE=null;mozile.xml._IE_XMLWRITER=null;mozile.xml.lookupPrefix=function(namespaceURI){for(var prefix in mozile.xml.ns){if(mozile.xml.ns[prefix]==namespaceURI)return prefix;}
return null;}
mozile.xml.load=function(filepath){if(typeof(filepath)!="string")return null;var uri;if(filepath.indexOf(":")>0)uri=filepath;else{var loc=location.toString();loc=loc.substring(0,loc.lastIndexOf("?"));loc=loc.substring(0,loc.lastIndexOf("/")+1);uri=loc+filepath;}
if(mozile.browser.isSafari)uri=uri.replace("file://","file:///");try{var XHR=new XMLHttpRequest();XHR.overrideMimeType("text/xml");XHR.open("GET",uri,false);try{XHR.send(null);return XHR.responseXML;}catch(e){mozile.debug.inform("mozile.xml.load","Error loading document: "+e);return null;}}
catch(e){try{mozile.xml._getDomProgID();var xmlDoc=new ActiveXObject(mozile.xml._IE_DOM);xmlDoc.async=false;try{var loaded=xmlDoc.load(uri);if(loaded)return xmlDoc;else{mozile.debug.inform("mozile.xml.load","Failed to load document.");}}catch(e){mozile.debug.inform("mozile.xml.load","Error loading document: "+mozile.dumpError(e));}}
catch(e){mozile.debug.inform("mozile.xml.load","No XML loading technique avaliable in this browser.");}}
return null;}
mozile.xml.parse=function(string){if(window.ActiveXObject){mozile.xml._getDomProgID();var xmlDoc=new ActiveXObject(mozile.xml._IE_DOM);xmlDoc.async="false";xmlDoc.loadXML(string);return xmlDoc;}
else if(window.DOMParser){var parser=new DOMParser();return parser.parseFromString(string,"text/xml");}
else{mozile.debug.inform("mozile.xml.serialize","No XML parsing technique avaliable in this browser.");return null;}}
mozile.xml.parseElement=function(string){var doc=mozile.xml.parse(string);if(doc&&doc.documentElement)return doc.documentElement;else return null;}
mozile.xml.serialize=function(node){if(!node)return null;if(node.xml)return node.xml;else if(window.XMLSerializer){var serializer=new XMLSerializer()
return serializer.serializeToString(node);}
else if(node.outerHTML)return node.outerHTML;else if(node.innerHTML){var container=document.createElement("container");container.appendChild(node.cloneNode(true));return container.innerHTML;}
else{mozile.debug.inform("mozile.xml.serialize","No XML serialization technique avaliable in this browser.");return null;}}
mozile.xml.transformToDocument=function(target,stylesheet,parameters){if(!target)return null;if(!stylesheet||stylesheet.nodeType!=9)return null;if(window.ActiveXObject){var method=mozile.xml._getMethod(stylesheet);var result=mozile.xml._IETransform(target,stylesheet,parameters);if(method=="text")result="<result>"+result+"</result>";return mozile.xml.parse(result);}
if(window.XSLTProcessor!=undefined){var processor=new XSLTProcessor();processor.importStylesheet(stylesheet);mozile.xml._setParameters(processor,parameters);return processor.transformToDocument(target);}
else{mozile.debug.inform("mozile.xml.transformToDocument","No XSLT technique avaliable in this browser.");return null;}}
mozile.xml.transformToFragment=function(target,stylesheet,parameters,ownerDoc){if(!target)return null;if(!stylesheet||stylesheet.nodeType!=9)return null;if(!ownerDoc)ownerDoc=document;if(window.ActiveXObject){var method=mozile.xml._getMethod(stylesheet);var result=mozile.xml._IETransform(target,stylesheet,parameters);var fragment=ownerDoc.createDocumentFragment();if(method=="text")fragment.appendChild(ownerDoc.createTextNode(result));else if(method=="html"&&ownerDoc.body&&ownerDoc.body.innerHTML){var container=ownerDoc.createElement("div");container.innerHTML=result;while(container.childNodes.length)
fragment.appendChild(container.firstChild);}
else{fragment.appendChild(mozile.xml.parseElement(result));}
return fragment;}
if(window.XSLTProcessor!=undefined){var processor=new XSLTProcessor();processor.importStylesheet(stylesheet);mozile.xml._setParameters(processor,parameters);return processor.transformToFragment(target,ownerDoc);}
else{mozile.debug.inform("mozile.xml.transformToFragment","No XSLT technique avaliable in this browser.");return null;}}
mozile.xml.transformToString=function(target,stylesheet,parameters){if(!target)return null;if(!stylesheet||stylesheet.nodeType!=9)return null;if(window.ActiveXObject){return mozile.xml._IETransform(target,stylesheet,parameters);}
if(window.XSLTProcessor!=undefined){var processor=new XSLTProcessor();processor.importStylesheet(stylesheet);mozile.xml._setParameters(processor,parameters);var result=processor.transformToDocument(target);var source=mozile.xml.serialize(result.documentElement);if(result.documentElement.nodeName=="transformiix:result")
source=source.substring(78,source.length-22);return source;}
else{mozile.debug.inform("mozile.xml.transformToString","No XSLT technique avaliable in this browser.");return null;}}
mozile.xml._getMethod=function(stylesheet){var method="html";if(!stylesheet)return method;var result;if(window.ActiveXObject){stylesheet.setProperty("SelectionNamespaces","xmlns:xsl='http://www.w3.org/1999/XSL/Transform'");result=stylesheet.selectSingleNode("//xsl:output");}
else if(window.XSLTProcessor!=undefined){mozile.require("mozile.xpath");var results=mozile.xpath.evaluate("//xsl:output",stylesheet);if(results[0])result=results[0];}
else return method;if(result)method=result.getAttribute("method");return method;}
mozile.xml._setParameters=function(processor,parameters){if(!processor)return;if(!parameters)return;var param;for(var i=0;i<parameters.length;i++){param=parameters[i];if(!param.name||!param.value)continue;if(processor.setParameter){processor.setParameter(param.namespace,param.name,param.value);}
else{if(param.namespace)
processor.addParameter(param.name,param.value,param.namespace);else processor.addParameter(param.name,param.value);}}}
mozile.xml._nodeToDocument=function(node){if(target.nodeType==mozile.dom.DOCUMENT_NODE)return node;var doc;if(window.ActiveXObject){var source=mozile.xml.serialize(node);return mozile.xml.parse(source);}
else if(document.implementation){doc=document.implementation.createDocument("","",null);var clone=doc.importNode(target,true);doc.appendChild(clone);return doc;}
else return null;}
mozile.xml._IETransform=function(target,stylesheet,parameters){if(!target)return null;if(!stylesheet||stylesheet.nodeType!=9)return null;var method,processor,result;try{mozile.xml._getDomProgID();mozile.xml._getXMLProgIDs();var newStyle=new ActiveXObject(mozile.xml._IE_THREADEDDOM);newStyle.loadXML(stylesheet.xml);var xslt=new ActiveXObject(mozile.xml._IE_XSLTEMPLATE);xslt.stylesheet=newStyle;processor=xslt.createProcessor();processor.input=target;mozile.xml._setParameters(processor,parameters);processor.transform();return processor.output;}catch(e){return"XSLT ERROR "+mozile.dumpError(e);}}
mozile.xml._pickRecentProgID=function(idList){var bFound=false;for(var i=0;i<idList.length&&!bFound;i++){try{var oDoc=new ActiveXObject(idList[i]);o2Store=idList[i];bFound=true;}catch(objException){};};if(!bFound){throw"Could not retreive a valid progID of Class: "+idList[idList.length-1]+". (original exception: "+e+")";};idList=null;return o2Store;}
mozile.xml._getDomProgID=function(){if(!mozile.xml._IE_DOM){mozile.xml._IE_DOM=mozile.xml._pickRecentProgID(["Msxml2.DOMDocument.5.0","Msxml2.DOMDocument.4.0","Msxml2.DOMDocument.3.0","MSXML2.DOMDocument","MSXML.DOMDocument","Microsoft.XMLDOM"]);}}
mozile.xml._getXMLProgIDs=function(){if(!mozile.xml._IE_XSLTEMPLATE){mozile.xml._IE_XSLTEMPLATE=mozile.xml._pickRecentProgID(["Msxml2.XSLTemplate.5.0","Msxml2.XSLTemplate.4.0","MSXML2.XSLTemplate.3.0"]);mozile.xml._IE_THREADEDDOM=mozile.xml._pickRecentProgID(["Msxml2.FreeThreadedDOMDocument.5.0","MSXML2.FreeThreadedDOMDocument.4.0","MSXML2.FreeThreadedDOMDocument.3.0"]);mozile.xml._IE_XMLWRITER=mozile.xml._pickRecentProgID(["Msxml2.MXXMLWriter.5.0","Msxml2.MXXMLWriter.4.0","Msxml2.MXXMLWriter.3.0","MSXML2.MXXMLWriter","MSXML.MXXMLWriter","Microsoft.XMLDOM"]);}}
mozile.xpath=new Object();mozile.xpath.prototype=new mozile.Module;mozile.xpath.getXPath=function(node,root){if(!node||node.nodeType==undefined||!node.nodeType)return"";if(node==root)return"";var parent=node.parentNode;switch(node.nodeType){case mozile.dom.ELEMENT_NODE:var nodeName;if(node.prefix)nodeName=node.nodeName;else nodeName=node.nodeName;if(!nodeName)nodeName=node.nodeName;if(mozile.dom.isHTML(node))nodeName=nodeName.toLowerCase();if(node.ownerDocument&&node.ownerDocument.documentElement==node)
return"/"+nodeName+"[1]";else return mozile.xpath.getXPath(parent,root)
+"/"+nodeName+"["+mozile.dom.getPosition(node)+"]";case mozile.dom.ATTRIBUTE_NODE:if(!parent)parent=node.ownerElement;return mozile.xpath.getXPath(parent,root)+"/@"+node.nodeName;case mozile.dom.TEXT_NODE:if(node==this._lastNode&&root==this._lastRoot)
return this._lastXPath;var xpath=mozile.xpath.getXPath(parent,root)
+"/text()["+mozile.dom.getPosition(node)+"]";this._lastNode=node;this._lastRoot=root;this._lastXPath=xpath;return xpath;case mozile.dom.COMMENT_NODE:return mozile.xpath.getXPath(parent,root)
+"/comment()["+mozile.dom.getPosition(node)+"]";default:if(parent)return mozile.xpath.getXPath(parent,root);else return"";}}
mozile.xpath.getComponents=function(expression){if(typeof(expression)!="string")return[];var components=expression.split("/");for(var c=0;c<components.length;c++){if(!components[c])components.splice(c,1);}
return components;}
mozile.xpath.getComponent=function(expression){var result=new Object();var components=mozile.xpath.getComponents(expression);if(components.length==0)return result;var component=components[components.length-1];var match=component.match(/(\S+:)?(\S+)\[(\d+)\]|(\S+:)?(\S+)/);if(match){if(match[1]&&match[2]){result.name=match[1]+match[2];result.localName=match[2];}
else if(match[2]){result.name=match[2];result.localName=match[2];}
else if(match[4]&&match[5]){result.name=match[4]+match[5];result.localName=match[5];}
else if(match[5]){result.name=match[5];result.localName=match[5];}
if(match[3])result.position=match[3];else result.position=null;}
return result;}
mozile.xpath.getNode=function(expression,root){if(!root)root=document;if(!expression)return root;if(expression==this._lastXPath&&root==this._lastRoot)return this._lastNode;var node;if(!node){var components=mozile.xpath.getComponents(expression);node=root;for(var i=0;i<components.length;i++){var component=mozile.xpath.getComponent(components[i]);node=mozile.xpath._getNthChild(node,component.name,component.position);if(!node)return null;}}
if(node){this._lastNode=node;this._lastRoot=root;this._lastXPath=expression;return node;}
else return null;}
mozile.xpath._getNthChild=function(parent,name,position){var c=0;var p=1;if(parent.nodeType==mozile.dom.DOCUMENT_NODE)
return parent.documentElement;if(name=="text()"){for(c=0;c<parent.childNodes.length;c++){if(parent.childNodes[c].nodeType==mozile.dom.TEXT_NODE){if(p==position)return parent.childNodes[c];p++;}}}
else if(name=="comment()"){for(c=0;c<parent.childNodes.length;c++){if(parent.childNodes[c].nodeType==mozile.dom.COMMENT_NODE){if(p==position)return parent.childNodes[c];p++;}}}
if(name.indexOf("@")==0){name=name.substring(1);return parent.getAttributeNode(name);}
else{if(name.indexOf("xmlns:")>-1)name=name.substring(6);for(c=0;c<parent.childNodes.length;c++){var childName=parent.childNodes[c].nodeName;if(childName==name||childName==name.toUpperCase()){if(p==position)return parent.childNodes[c];p++;}}}
return null;}
mozile.xpath.evaluate=function(expression,root){if(!root)root=document;var doc;if(root.ownerDocument)doc=root.ownerDocument;else{doc=root;root=root.documentElement;}
var nodes=new Array();var results,result;if(window.XPathEvaluator){var XPE=new XPathEvaluator;var NSR=function(prefix){var namespaceURI=mozile.dom.lookupNamespaceURI(root,prefix);if(namespaceURI)return namespaceURI;else if(mozile.xml.ns[prefix])return mozile.xml.ns[prefix];return mozile.defaultNS;}
try{results=XPE.evaluate(expression,root,NSR,0,null);if(results){result=results.iterateNext();while(result){nodes.push(result);result=results.iterateNext();}}}catch(e){alert(doc.documentElement.getAttribute("xmlns")+"\n"+e);}}
else if(root.selectNodes){results=root.selectNodes(expression);result=results.nextNode();while(result){nodes.push(result);result=results.nextNode();}}
return nodes;}
mozile.util=new Object();mozile.util.prototype=new mozile.Module;mozile.util.dumpKeys=function(obj){var keys=new Array();for(var key in obj)keys.push(key);keys.sort();return keys.join(", ");}
mozile.util.dumpValues=function(obj){var result=new Array();for(var key in obj){result.push(key+" = "+obj[key]);}
result.sort();return result.join("\n");}
mozile.util.capitalize=function(string){if(!string)return string;return string.replace(/\w+/g,function(a){return a.charAt(0).toUpperCase()+a.substr(1).toLowerCase();});}
mozile.util.pad=function(string,length,left){if(!string)string=" ";else string=String(string);if(!this._memory)this._memory=new Object();var id=string+":"+length+":"+left;if(this._memory[id])return this._memory[id];var space="";if(string.length<length){for(var s=0;s<length-string.length;s++)space+=" ";}
var result;if(left)result=space+string;else result=string+space;this._memory[id]=result;return result;}
mozile.util.debug=function(details,level,message){if(level>=mozile.getDebugLevel()||details["Status Message"]){var date=new Date();mozile.util.messages.push([date.toLocaleString(),details,level,message]);}
return true;}
mozile.util.messages=new Array();mozile.dom.NodeFilter={FILTER_ACCEPT:1,FILTER_REJECT:2,FILTER_SKIP:3,SHOW_ALL:-1,SHOW_ELEMENT:1,SHOW_ATTRIBUTE:2,SHOW_TEXT:4,SHOW_CDATA_SECTION:8,SHOW_ENTITY_REFERENCE:16,SHOW_ENTITY:32,SHOW_PROCESSING_INSTRUCTIONS:64,SHOW_COMMENT:128,SHOW_DOCUMENT:256,SHOW_DOCUMENT_TYPE:512,SHOW_DOCUMENT_FRAGMENT:1024,SHOW_NOTATION:2048}
mozile.dom.TreeWalker=function(root,whatToShow,filter,expandEntityReferences){this.root=root;this.whatToShow=whatToShow;this.filter=filter;this.expandEntityReferences=expandEntityReferences;this.currentNode=root;}
mozile.dom.TreeWalker.prototype.parentNode=function(){var testNode=this.currentNode;do{if(testNode!=this.root&&testNode.parentNode!=this.root&&testNode.parentNode!=null){testNode=testNode.parentNode;}
else return null;}while(this._getFilteredStatus(testNode)!=mozile.dom.NodeFilter.FILTER_ACCEPT);if(testNode!=null)this.currentNode=testNode;return testNode;}
mozile.dom.TreeWalker.prototype.firstChild=function(){var testNode=this.currentNode.firstChild;while(testNode!=null){if(this._getFilteredStatus(testNode)==mozile.dom.NodeFilter.FILTER_ACCEPT){break;}
testNode=testNode.nextSibling;}
if(testNode!=null)this.currentNode=testNode;return testNode;}
mozile.dom.TreeWalker.prototype.lastChild=function(){var testNode=this.currentNode.lastChild;while(testNode!=null){if(this._getFilteredStatus(testNode)==mozile.dom.NodeFilter.FILTER_ACCEPT){break;}
testNode=testNode.previousSibling;}
if(testNode!=null)this.currentNode=testNode;return testNode;}
mozile.dom.TreeWalker.prototype.nextNode=function(){var testNode=this.currentNode;while(testNode!=null){if(testNode.childNodes.length!=0)testNode=testNode.firstChild;else if(testNode.nextSibling!=null)testNode=testNode.nextSibling;else{while(testNode!=null){if(testNode.parentNode!=this.root&&testNode.parentNode!=null){if(testNode.parentNode.nextSibling!=null){testNode=testNode.parentNode.nextSibling;break;}
else testNode=testNode.parentNode;}
else return null;}}
if(testNode!=null&&this._getFilteredStatus(testNode)==mozile.dom.NodeFilter.FILTER_ACCEPT){break;}}
if(testNode!=null)this.currentNode=testNode;return testNode;}
mozile.dom.TreeWalker.prototype.previousNode=function(){var testNode=this.currentNode;while(testNode!=null){if(testNode.previousSibling!=null){testNode=testNode.previousSibling;while(testNode.lastChild!=null){testNode=testNode.lastChild;}}
else{if(testNode.parentNode!=this.root&&testNode.parentNode!=null){testNode=testNode.parentNode;}
else testNode=null;}
if(testNode!=null&&this._getFilteredStatus(testNode)==mozile.dom.NodeFilter.FILTER_ACCEPT){break;}}
if(testNode!=null)this.currentNode=testNode;return testNode;}
mozile.dom.TreeWalker.prototype.nextSibling=function(){var testNode=this.currentNode;while(testNode!=null){if(testNode.nextSibling!=null)testNode=testNode.nextSibling;if(this._getFilteredStatus(testNode)==mozile.dom.NodeFilter.FILTER_ACCEPT){break;}}
if(testNode!=null)this.currentNode=testNode;return testNode;}
mozile.dom.TreeWalker.prototype.previousSibling=function(){var testNode=this.currentNode;while(testNode!=null){if(testNode.previousSibling!=null)testNode=testNode.previousSibling;if(this._getFilteredStatus(testNode)==mozile.dom.NodeFilter.FILTER_ACCEPT){break;}}
if(testNode!=null)this.currentNode=testNode;return testNode;}
mozile.dom.TreeWalker.prototype._getFilteredStatus=function(node){var mask=null;switch(node.nodeType){case mozile.dom.ELEMENT_NODE:mask=mozile.dom.NodeFilter.SHOW_ELEMENT;break;case mozile.dom.ATTRIBUTE_NODE:mask=mozile.dom.NodeFilter.SHOW_ATTRIBUTE;break;case mozile.dom.TEXT_NODE:mask=mozile.dom.NodeFilter.SHOW_TEXT;break;case mozile.dom.CDATA_SECTION_NODE:mask=mozile.dom.NodeFilter.SHOW_CDATA_SECTION;break;case mozile.dom.ENTITY_REFERENCE_NODE:mask=mozile.dom.NodeFilter.SHOW_ENTITY_REFERENCE;break;case mozile.dom.ENTITY_NODE:mask=mozile.dom.NodeFilter.SHOW_PROCESSING_INSTRUCTION;break;case mozile.dom.PROCESSING_INSTRUCTION_NODE:mask=mozile.dom.NodeFilter.SHOW_PROCESSING_INSTRUCTION;break;case mozile.dom.COMMENT_NODE:mask=mozile.dom.NodeFilter.SHOW_COMMENT;break;case mozile.dom.DOCUMENT_NODE:mask=mozile.dom.NodeFilter.SHOW_DOCUMENT;break;case mozile.dom.DOCUMENT_TYPE_NODE:mask=mozile.dom.NodeFilter.SHOW_DOCUMENT_TYPE;break;case mozile.dom.DOCUMENT_FRAGMENT_NODE:mask=mozile.dom.NodeFilter.SHOW_DOCUMENT_FRAGMENT;break;case mozile.dom.NOTATION_NODE:mask=mozile.dom.NodeFilter.SHOW_NOTATION;break;}
if(mask!=null&&(this.whatToShow&mask)==0){return mozile.dom.NodeFilter.FILTER_REJECT;}
if(this.filter!=null&&this.filter.acceptNode!=null){return this.filter.acceptNode(node);}
return mozile.dom.NodeFilter.FILTER_ACCEPT;}
if(mozile.browser.isIE){document.createTreeWalker=function(root,whatToShow,filter,expandEntityReferences){return new mozile.dom.TreeWalker(root,whatToShow,filter,expandEntityReferences);};mozile.dom.NodeFilter=mozile.dom.NodeFilter;mozile.dom.TreeWalker=mozile.dom.TreeWalker;}
else{mozile.dom.NodeFilter=NodeFilter;mozile.dom.TreeWalker=TreeWalker;}
mozile.dom.InternetExplorerRange=function(range){this._range=null;if(range)
this._range=range;else{this._range=document.body.createTextRange();}
this.collapsed=null;this.commonAncestorContainer=null;this.startContainer=null;this.startOffset=null;this.endContainer=null;this.endOffset=null;}
mozile.dom.InternetExplorerRange.prototype._init=function(){var beginRange=this._range.duplicate();beginRange.collapse(true);var position=this._getPosition(beginRange);this.startContainer=position.node;this.startOffset=position.offset;var endRange=this._range.duplicate();endRange.collapse(false);position=this._getPosition(endRange);this.endContainer=position.node;this.endOffset=position.offset;this._commonAncestorContainer();this._collapsed();}
mozile.dom.InternetExplorerRange.prototype._getPosition=function(textRange){var element=textRange.parentElement();var range=document.body.createTextRange();range.moveToElementText(element);range.setEndPoint("EndToStart",textRange);var rangeLength=range.text.length;if(rangeLength<element.innerText.length/2){var direction=1;var node=element.firstChild;}
else{direction=-1;node=element.lastChild;range.moveToElementText(element);range.setEndPoint("StartToStart",textRange);rangeLength=range.text.length;}
while(node){switch(node.nodeType){case mozile.dom.TEXT_NODE:nodeLength=node.data.length;if(nodeLength<rangeLength){var difference=rangeLength-nodeLength;if(direction==1)range.moveStart("character",difference);else range.moveEnd("character",-difference);rangeLength=difference;}
else{if(direction==1)return{node:node,offset:rangeLength};else return{node:node,offset:nodeLength-rangeLength};}
break;case mozile.dom.ELEMENT_NODE:nodeLength=node.innerText.length;if(direction==1)range.moveStart("character",nodeLength);else range.moveEnd("character",-nodeLength);rangeLength=rangeLength-nodeLength;break;}
if(direction==1)node=node.nextSibling;else node=node.previousSibling;}
return{node:element,offset:0};}
mozile.dom.InternetExplorerRange.prototype._getOffset=function(startNode,startOffset){var node,moveCharacters;if(startNode.nodeType==mozile.dom.TEXT_NODE){moveCharacters=startOffset;node=startNode.previousSibling;}
else if(startNode.nodeType==mozile.dom.ELEMENT_NODE){moveCharacters=0;if(startOffset>0)node=startNode.childNodes[startOffset-1];else return 0;}
else{mozile.debug.inform("mozile.dom.InternetExplorerRange.prototype._getOffset","Bad node given: "+mozile.xpath.getXPath(startNode));return 0;}
while(node){var nodeLength=0;if(node.nodeType==mozile.dom.ELEMENT_NODE){nodeLength=node.innerText.length;if(this._isChildless(node))nodeLength=1;if(this._isBlock(node))nodeLength++;}
else if(node.nodeType==mozile.dom.TEXT_NODE){nodeLength=node.data.length;}
moveCharacters+=nodeLength;node=node.previousSibling;}
return moveCharacters;}
mozile.dom.InternetExplorerRange.prototype._isBlock=function(node){switch(node.nodeName.toLowerCase()){case'p':case'div':case'h1':case'h2':case'h3':case'h4':case'h5':case'h6':case'pre':return true;}
return false;}
mozile.dom.InternetExplorerRange.prototype._isChildless=function(node){switch(node.nodeName.toLowerCase()){case'img':case'br':case'hr':return true;}
return false;}
mozile.dom.InternetExplorerRange.prototype.setStart=function(startNode,startOffset){var container=startNode;if(startNode.nodeType==mozile.dom.TEXT_NODE||startNode.nodeType==mozile.dom.COMMENT_NODE||startNode.nodeType==mozile.dom.CDATA_SECTION_NODE){container=container.parentNode;}
var copyRange=this._range.duplicate();copyRange.moveToElementText(container);copyRange.collapse(true);copyRange.move('Character',this._getOffset(startNode,startOffset));this._range.setEndPoint('StartToStart',copyRange);this.startContainer=startNode;this.startOffset=startOffset;if(this.endContainer==null&&this.endOffset==null){this.endContainer=startNode;this.endOffset=startOffset;}
this._commonAncestorContainer();this._collapsed();}
mozile.dom.InternetExplorerRange.prototype.setEnd=function(endNode,endOffset){var copyRange=this._range.duplicate();copyRange.collapse(true);var container=endNode;if(endNode.nodeType==mozile.dom.TEXT_NODE||endNode.nodeType==mozile.dom.COMMENT_NODE||endNode.nodeType==mozile.dom.CDATA_SECTION_NODE){container=container.parentNode;}
copyRange=this._range.duplicate();copyRange.moveToElementText(container);copyRange.collapse(true);copyRange.move('Character',this._getOffset(endNode,endOffset));this._range.setEndPoint('EndToEnd',copyRange);this.endContainer=endNode;this.endOffset=endOffset;if(this.startContainer==null&&this.startOffset==null){this.startContainer=endNode;this.startOffset=endOffset;}
this._commonAncestorContainer();this._collapsed();}
mozile.dom.InternetExplorerRange.prototype.setStartBefore=function(referenceNode){this.setStart(referenceNode.parentNode,mozile.dom.getIndex(referenceNode));};mozile.dom.InternetExplorerRange.prototype.setStartAfter=function(referenceNode){this.setStart(referenceNode.parentNode,mozile.dom.getIndex(referenceNode)+1);};mozile.dom.InternetExplorerRange.prototype.setEndBefore=function(referenceNode){this.setEnd(referenceNode.parentNode,mozile.dom.getIndex(referenceNode));};mozile.dom.InternetExplorerRange.prototype.setEndAfter=function(referenceNode){this.setEnd(referenceNode.parentNode,mozile.dom.getIndex(referenceNode)+1);};mozile.dom.InternetExplorerRange.prototype.selectNode=function(referenceNode){this.setStartBefore(referenceNode);this.setEndAfter(referenceNode);};mozile.dom.InternetExplorerRange.prototype.selectNodeContents=function(referenceNode){this.setStart(referenceNode,0);if(referenceNode.nodeType==mozile.dom.TEXT_NODE)
this.setEnd(referenceNode,referenceNode.data.length);else
this.setEnd(referenceNode,referenceNode.childNodes.length);};mozile.dom.InternetExplorerRange.prototype.collapse=function(toStart){this._range.collapse(toStart);if(toStart){this.endContainer=this.startContainer;this.endOffset=this.startOffset;}else{this.startContainer=this.endContainer;this.startOffset=this.endOffset;}
this._commonAncestorContainer();this._collapsed();};mozile.dom.InternetExplorerRange.prototype.cloneContents=function(){var df=document.createDocumentFragment();var container=this.commonAncestorContainer;if(container.nodeType==mozile.dom.TEXT_NODE){df.appendChild(document.createTextNode(this._range.text));return df;}
var startNode=this.startContainer;if(this.startContainer.nodeType!=mozile.dom.TEXT_NODE)
startNode=this.startContainer.childNodes[this.startOffset];var endNode=this.endContainer;if(this.endContainer.nodeType!=mozile.dom.TEXT_NODE)
endNode=this.endContainer.childNodes[this.endOffset-1];if(startNode==endNode){df.appendChild(startNode.cloneNode(true));return df;}
var current=container.firstChild;var parent=null;var clone;while(current){if(!parent){if(mozile.dom.isAncestorOf(current,startNode,container)){parent=df;}
else{current=current.nextSibling;continue;}}
if(current==startNode&&this.startContainer.nodeType==mozile.dom.TEXT_NODE){content=this.startContainer.data.substring(this.startOffset);parent.appendChild(document.createTextNode(content));}
else if(current==endNode){if(this.endContainer.nodeType==mozile.dom.TEXT_NODE){content=this.endContainer.data.substring(0,this.endOffset);parent.appendChild(document.createTextNode(content));}
else parent.appendChild(endNode.cloneNode(false));break;}
else{clone=current.cloneNode(false);parent.appendChild(clone);}
if(current.firstChild){parent=clone;current=current.firstChild;}
else if(current.nextSibling){current=current.nextSibling;}
else while(current){if(current.parentNode){parent=parent.parentNode;current=current.parentNode;if(current.nextSibling){current=current.nextSibling;break;}}
else current=null;}}
return df;};mozile.dom.InternetExplorerRange.prototype.deleteContents=function(){this._range.pasteHTML('');this.endContainer=this.startContainer;this.endOffset=this.startOffset;this._commonAncestorContainer();this._collapsed();};mozile.dom.InternetExplorerRange.prototype.extractContents=function(){var fragment=this.cloneContents();this.deleteContents();return fragment;};mozile.dom.InternetExplorerRange.prototype.insertNode=function(newNode){if(this.startContainer.nodeType==mozile.dom.TEXT_NODE){this.startContainer.splitText(this.startOffset);this.startContainer.parentNode.insertBefore(newNode,this.startContainer.nextSibling);this.setStart(this.startContainer,this.startOffset);return;}else{var parentNode=this.startContainer.parentNode;if(this.startContainer.childNodes.length==this.startOffset){parentNode.appendChild(newNode);}else{this.startContainer.insertBefore(newNode,this.startContainer.childNodes.item(this.startOffset));this.setStart(this.startContainer,this.startOffset+1);return;}}};mozile.dom.InternetExplorerRange.prototype.surroundContents=function(newNode){newNode.appendChild(this.extractContents());this.insertNode(newNode);};mozile.dom.InternetExplorerRange.prototype.compareBoundaryPoints=function(how,sourceRange){alert('mozile.dom.InternetExplorerRange.compareBoundaryPoints() is not implemented yet');};mozile.dom.InternetExplorerRange.prototype.cloneRange=function(){var r=new mozile.dom.InternetExplorerRange(this._range.duplicate());var properties=["startContainer","startOffset","endContainer","endOffset","commonAncestorContainer","collapsed"];for(var i=0;i<properties.length;i++){r[properties[i]]=this[properties[i]];}
return r;};mozile.dom.InternetExplorerRange.prototype.detach=function(){};mozile.dom.InternetExplorerRange.prototype.toString=function(){return this._range.text;};mozile.dom.InternetExplorerRange.prototype._commonAncestorContainer=function(){if(this.startContainer==null||this.endContainer==null){this.commonAncestorContainer=null;return;}
if(this.startContainer==this.endContainer){this.commonAncestorContainer=this.startContainer;}
else{this.commonAncestorContainer=mozile.dom.getCommonAncestor(this.startContainer,this.endContainer);}}
mozile.dom.InternetExplorerRange.prototype._collapsed=function(){this.collapsed=(this.startContainer==this.endContainer&&this.startOffset==this.endOffset);}
mozile.dom.InternetExplorerRange.prototype.store=mozile.dom.Range.prototype.store;mozile.dom.InternetExplorerRange.prototype.restore=mozile.dom.Range.prototype.restore;mozile.dom.InternetExplorerSelection=function(){this._selection=document.selection;this.anchorNode=null;this.anchorOffset=null;this.focusNode=null;this.focusOffset=null;this.isCollapsed=null;this.rangeCount=0;this._direction=this._LTR;this._init();}
mozile.dom.InternetExplorerSelection.prototype._LTR=1;mozile.dom.InternetExplorerSelection.prototype._RTL=-1;mozile.dom.InternetExplorerSelection.prototype._init=function(range){if(!range)range=this.getRangeAt(0);if(!this._direction)this._direction=this._LTR;if(range&&range.startContainer){if(this._direction==this._LTR){this.anchorNode=range.startContainer;this.anchorOffset=range.startOffset;this.focusNode=range.endContainer;this.focusOffset=range.endOffset;}
else{this.anchorNode=range.endContainer;this.anchorOffset=range.endOffset;this.focusNode=range.startContainer;this.focusOffset=range.startOffset;}
this.isCollapsed=range.collapsed;}
else{this.anchorNode=null;this.anchorOffset=null;this.focusNode=null;this.focusOffset=null;this.isCollapsed=true;}
this.rangeCount=this._selection.createRangeCollection().length;}
mozile.dom.InternetExplorerSelection.prototype.getRangeAt=function(index){var textRange=this._selection.createRangeCollection().item(index).duplicate();var range;if(this._lastTextRange&&this._lastTextRange.isEqual(textRange)){range=this._lastRange;}
else{range=new mozile.dom.InternetExplorerRange(textRange);range._init();this._lastTextRange=textRange.duplicate();this._lastRange=range;this._direction=this._LTR;}
return range.cloneRange();}
mozile.dom.InternetExplorerSelection.prototype.collapse=function(parentNode,offset){try{var range=this.getRangeAt(0);range.setStart(parentNode,offset);range.setEnd(parentNode,offset);range.collapse(false);this._direction=this._LTR;this._init(range);this._lastTextRange=range._range.duplicate();this._lastRange=range;range._range.select();}catch(e){mozile.debug.debug("mozile.dom.InternetExplorerSelection.collapse","Error: "+mozile.dumpError(e));}}
mozile.dom.InternetExplorerSelection.prototype.extend=function(parentNode,offset){var range=this.getRangeAt(0);var direction;if(parentNode==range.startContainer&&range.startOffset<=offset){direction=this._LTR;}
else if(parentNode==range.endContainer&&range.endOffset>=offset){direction=this._RTL;}
else if(parentNode==range.startContainer.parentNode&&range.startContainer.nodeType==mozile.dom.TEXT_NODE&&mozile.dom.getIndex(range.startContainer)<=offset){direction=this._LTR;}
else if(parentNode==range.endContainer.parentNode&&range.endContainer.nodeType==mozile.dom.TEXT_NODE&&mozile.dom.getIndex(range.endContainer)>=offset){direction=this._RTL;}
else{var ancestor=mozile.dom.getCommonAncestor(parentNode,range.commonAncestorContainer)
var treeWalker=document.createTreeWalker(ancestor,mozile.dom.NodeFilter.SHOW_ALL,null,false);treeWalker.currentNode=this.anchorNode;while(treeWalker.previousNode()){if(treeWalker.currentNode==parentNode){direction=this._RTL;break;}}
if(!direction)direction=this._LTR;}
if(direction==this._LTR)range.setEnd(parentNode,offset);else if(direction==this._RTL)range.setStart(parentNode,offset);else return;this._direction=direction;this._init(range);this._lastTextRange=range._range.duplicate();this._lastRange=range;range._range.select();}
mozile.dom.InternetExplorerSelection.prototype.collapseToStart=function(){var range=this.getRangeAt(0);range.collapse(true);this._init(range);range._range.select();}
mozile.dom.InternetExplorerSelection.prototype.collapseToEnd=function(){var range=this.getRangeAt(0);range.collapse();this._init(range);range._range.select();}
mozile.dom.InternetExplorerSelection.prototype.selectAllChildren=function(parentNode){var range=this.getRangeAt(0);range.selectNodeContents(parentNode);this._init(range);range._range.select();}
mozile.dom.InternetExplorerSelection.prototype.addRange=function(range){this._direction=this._LTR;this._init(range);range._range.select();}
mozile.dom.InternetExplorerSelection.prototype.removeRange=function(range){range.collapse();this._init(range);}
mozile.dom.InternetExplorerSelection.prototype.removeAllRanges=function(){this._selection.empty();this._init();}
mozile.dom.InternetExplorerSelection.prototype.deleteFromDocument=function(){this._selection.clear();this._init();}
mozile.dom.InternetExplorerSelection.prototype.selectionLanguageChange=function(){}
mozile.dom.InternetExplorerSelection.prototype.toString=function(){var range=this.getRangeAt(0);return range.toString();}
mozile.dom.InternetExplorerSelection.prototype.containsNode=function(aNode,aPartlyContained){alert('mozile.dom.InternetExplorerSelection.containsNode() is not implemented yet');}
mozile.dom.InternetExplorerSelection.prototype.store=mozile.dom.Selection.prototype.store;mozile.dom.InternetExplorerSelection.prototype.restore=mozile.dom.Selection.prototype.restore;mozile.dom.InternetExplorerSelection.prototype.scroll=mozile.dom.Selection.prototype.scroll;mozile.dom.Selection.restoreSelection=function(r){var range=new mozile.Range();try{range.setStart(r.startContainer,r.startOffset);range.setEnd(r.endContainer,r.endOffset);}catch(e){}
var s=new mozile.dom.Selection();s.removeAllRanges();s.addRange(range);}
mozile.rng=new Object();mozile.rng.prototype=new mozile.Module;mozile.rng.debug=false;mozile.rng.getName=function(){if(this._name)return this._name;if(this._element.getAttribute("name")){var match=this._element.getAttribute("name").match(/^\s*(.+?)\s*$/);if(match&&match.length==2){this._name=match[1];return this._name;}}
throw Error("This RNG '"+this.getType()+"' element must have a 'name' attribute.");}
mozile.rng.getLocalName=function(){if(this._localName)return this._localName;var name=this.getName();if(name.indexOf(":"))this._localName=name.substring(name.indexOf(":")+1,name.length);else this._localName=name;return this._localName;}
mozile.rng.getPrefix=function(){if(this._prefix)return this._prefix;var name=this.getName();if(name.indexOf(":"))this._prefix=name.substring(0,name.indexOf(":"));else this._prefix=null;return this._prefix;}
mozile.rng.checkType=function(types,type){if(typeof(types)=="string"&&types==type)return true;if(types.length){for(var i=0;i<types.length;i++){if(types[i]==type)return true;}}
return false;}
mozile.rng.validateSequence=function(node,validation){if(!node)throw Error("mozile.rng.validateSequence() requires a node.");if(!validation)throw Error("mozile.rng.validateSequence() requires an mozile.rng.Validation object.");var RNGChildren=this.getChildNodes();if(RNGChildren.length==0)return validation;for(var r=0;r<RNGChildren.length;r++){if(!validation.isValid)break;validation=RNGChildren[r].validate(node,validation);if(node.nodeType==mozile.dom.ELEMENT_NODE&&validation.isEmpty)
validation.logError(this,node,"The parent element should be empty, but it contains child elements.");if(validation.getCurrentElement()){node=validation.getCurrentElement();if(mozile.dom.getNextSiblingElement(node))node=mozile.dom.getNextSiblingElement(node);}}
return validation;}
mozile.rng.validateMany=function(node,validation){if(!node)throw Error("mozile.rng.validateMany() requires a node.");if(!validation)throw Error("mozile.rng.validateMany() requires an mozile.rng.Validation object.");var result;validation.count=0;while(true){result=validation.branch();result=this._validateSequence(node,result);if(result.isValid){validation.count++;validation.merge(result);if(result.getCurrentElement()&&mozile.dom.getNextSiblingElement(result.getCurrentElement())){node=mozile.dom.getNextSiblingElement(result.getCurrentElement());continue;}}
if(mozile.rng.debug)validation.append(result,true);break;}
return validation;}
mozile.rng.validateInterleave=function(node,validation){if(!node)throw Error("mozile.rng.validateInterleave() requires a node.");if(!validation)throw Error("mozile.rng.validateInterleave() requires an mozile.rng.Validation object.");validation.logMessage(this,node,"Validating interleave...");var RNGChildren=new Array();for(var c=0;c<this.getChildNodes().length;c++){RNGChildren.push(this.getChildNodes()[c]);}
var type;var length=RNGChildren.length;for(var i=0;i<length;i++){for(var r=0;r<RNGChildren.length;r++){var result=validation.branch();result=RNGChildren[r].validate(node,result);if(result.isValid){type=RNGChildren[r].getType();if((RNGChildren[r].getType()=="optional"||RNGChildren[r].getType()=="zeroOrMore")&&!result.count)
continue;validation.merge(result);if(result.getCurrentElement()&&mozile.dom.getNextSiblingElement(result.getCurrentElement())){node=mozile.dom.getNextSiblingElement(result.getCurrentElement());}
RNGChildren.splice(r,1);break;}}}
if(RNGChildren.length>0){for(r=0;r<RNGChildren.length;r++){if(!validation.isValid)break;type=RNGChildren[r].getType();if(type!="optional"&&type!="zeroOrMore")
validation.logError(this,node,"There were non-optional RNG children which did not match any elements.");}}
if(validation.isValid)validation.logMessage(this,node,"Interleave is valid.");return validation;}
mozile.rng.combine=function(target){var combineType;var combineA=this._element.getAttribute("combine");var combineB=target._element.getAttribute("combine");if(combineA&&combineB){if(combineA==combineB)combineType=combineA;else throw Error("In order to combine RNG elements, their 'combine' attributes muct match. '"+combineA+"' != '"+combineB+"'");}
else{if(combineA)combineType=combineA;else if(combineB)combineType=combineB;else throw Error("In order to combine RNG elements, at least one must have a 'combine' attribute.");}
if(combineType!="choice"&&combineType!="interleave")
throw Error("RNG 'define' or 'start' elements can only have 'combine' attribute values of 'choice' or 'interleave', not '"+combineType+"'.");this._element.setAttribute("combine",combineType);var combineObject;if(this.getChildNodes().length==1&&this.getChildNode(0).getType()==combineType){combineObject=this.getChildNode(0);}
else{var validation=new mozile.rng.Validation();combineObject=this.getSchema().parseElement(mozile.dom.createElementNS(mozile.xml.ns.rng,combineType),validation,true);this.appendChild(combineObject);while(this.getChildNodes().length>1){combineObject.appendChild(this.removeChild(this.getChildNode(0)));}}
var targetObject=target;if(target.getChildNodes().length==1&&target.getChildNode(0).getType()==combineType){targetObject=target.getChildNode(0);}
while(targetObject.getChildNodes().length){combineObject.appendChild(targetObject.removeChild(targetObject.getChildNode(0)));}
return this;}
mozile.rng.Schema=function(target){this._documents=new Object();this._types=new Object();this._names=new Object();this._root=null;if(target)this.parse(target);}
mozile.rng.Schema.prototype.constructor=mozile.rng.Schema;mozile.rng.Schema.prototype.toString=function(){return"[object mozile.rng.Schema]"};mozile.rng.Schema.prototype.getType=function(){return"schema";}
mozile.rng.Schema.prototype.parse=function(target){var element;var filepath;if(typeof(target)=="string"){if(target.indexOf("<")>-1){element=mozile.xml.parse(target).documentElement;filepath=location.toString();}
else{var doc=this.load(target);if(!doc)throw Error("Could not load schema '"+target+"'.");element=doc.documentElement;filepath=target;}}
else if(target.nodeType){element=target;filepath=location.toString();}
else throw Error("Unknown target given in mozile.rng.Schema.parse().");if(!this.filepath)this.filepath=filepath;var validation=new mozile.rng.Validation();validation.logMessage(this,null,"Starting to parse schema");var result=this.parseElement(element,validation);if(!result){validation.logError(this,null,"An invalid result was returned from the parsing operation: "+result);return validation;}
var root=result;if(root.getType()=="element"||root.getType()=="grammar"){this._root=root;}
else{validation.logError(this,null,"Schema has no root element.");return validation;}
var includes=this.getNodes("include");if(!includes)return validation;for(var i=0;i<includes.length;i++){validation=includes[i].include(validation);}
return validation;}
mozile.rng.Schema.prototype.parseElement=function(element,validation,omitValidation){var rngNode=this._createRNGNode(element,validation);if(!rngNode){return null;}
else validation.logMessage(rngNode,element,"Adding RNG '"+rngNode.getType()+"' node.");if(omitValidation!=true){validation=rngNode.selfValidate(validation);if(!validation.isValid)return{node:null,validation:validation};}
if(element.hasChildNodes()){var child;for(var c=0;c<element.childNodes.length;c++){child=this.parseElement(element.childNodes[c],validation);if(child)rngNode.appendChild(child);}}
return rngNode;}
mozile.rng.Schema.prototype._createRNGNode=function(element,validation){if(!element.nodeType||element.nodeType!=mozile.dom.ELEMENT_NODE)return null;if(element.namespaceURI!=mozile.xml.ns.rng)return null;var result;var type=mozile.dom.getLocalName(element);switch(type){case"grammar":result=new mozile.rng.Grammar(element,this);break;case"start":result=new mozile.rng.Start(element,this);break;case"element":result=new mozile.rng.Element(element,this);break;case"attribute":result=new mozile.rng.Attribute(element,this);break;case"empty":result=new mozile.rng.Empty(element,this);break;case"text":result=new mozile.rng.Text(element,this);break;case"group":result=new mozile.rng.Group(element,this);break;case"optional":result=new mozile.rng.Optional(element,this);break;case"oneOrMore":result=new mozile.rng.OneOrMore(element,this);break;case"zeroOrMore":result=new mozile.rng.ZeroOrMore(element,this);break;case"choice":result=new mozile.rng.Choice(element,this);break;case"define":result=new mozile.rng.Define(element,this);break;case"ref":result=new mozile.rng.Ref(element,this);break;case"include":result=new mozile.rng.Include(element,this);break;case"data":result=new mozile.rng.Data(element,this);break;case"param":result=new mozile.rng.Param(element,this);break;case"value":result=new mozile.rng.Value(element,this);break;case"interleave":result=new mozile.rng.Interleave(element,this);break;case"div":result=new mozile.rng.Div(element,this);break;default:validation.logError(this,null,"Method mozile.rng.Schema._createRNGNode() found an unknown element '"+element.nodeName+"' with the RNG namespace.");return null;}
if(result){this._indexNode(result);return result;}
else return null;}
mozile.rng.Schema.prototype._indexNode=function(node){var type=node.getType();if(!this._types[type])this._types[type]=new Array();this._types[type].push(node);if(node.getName){var name=node.getName();if(!this._names[type])this._names[type]=new Object();if(!this._names[type][name])this._names[type][name]=new Array();this._names[type][name].push(node);}}
mozile.rng.Schema.prototype.getNodes=function(type,name){if(name){if(this._names[type]&&this._names[type][name])
return this._names[type][name];else return new Array();}
else{if(this._types[type])return this._types[type];else return new Array();}}
mozile.rng.Schema.prototype.load=function(filepath,forceLoad){if(!forceLoad&&this._documents[filepath])return this._documents[filepath];var uri=mozile.getPath(filepath);uri=mozile.getAbsolutePath(uri,mozile.root);var xmlDoc=mozile.xml.load(uri);if(xmlDoc)this._documents[filepath]=xmlDoc;return xmlDoc;}
mozile.rng.Schema.prototype.validate=function(element){if(!element)element=document.documentElement;if(!this._root)return false;var validation=new mozile.rng.Validation();this._root.resetAll();this._root.validate(element,validation);return validation;}
mozile.rng.Validation=function(){this.isValid=true;this.isEmpty=false;this.allowText=false;this._currentElement=null;this._currentParent=null;this._lastValidElement=null;this._validAttributes=new Array();this._messages=new Array();}
mozile.rng.Validation.prototype.toString=function(){return"[object mozile.rng.Validation]"};mozile.rng.Validation.prototype.getType=function(){return"validation";}
mozile.rng.Validation.prototype.getCurrentElement=function(){return this._currentElement;}
mozile.rng.Validation.prototype.setCurrentElement=function(element){if(element.nodeType==mozile.dom.ELEMENT_NODE){if(element==this._lastValidElement){this.logError(this,element,"An element has been marked as valid twice, which indicated an error");}
else{this._lastValidElement=this._currentElement;this._currentElement=element;}}
return element;}
mozile.rng.Validation.prototype.getCurrentParent=function(){return this._currentParent;}
mozile.rng.Validation.prototype.setCurrentParent=function(element){this._currentParent=element;return element;}
mozile.rng.Validation.prototype.addAttribute=function(attr){this._validAttributes.push(attr);return attr;}
mozile.rng.Validation.prototype.isAttributeValid=function(attr){for(var a=0;a<this._validAttributes.length;a++){if(mozile.browser.isMozilla&&mozile.browser.mozillaVersion<1.8){if(this._validAttributes[a].nodeName==attr.nodeName)return true;}
if(this._validAttributes[a]==attr)return true;}
return false;}
mozile.rng.Validation.prototype.logMessage=function(rngNode,node,message,type){if(!type)type="Message";this._messages.push({"type":type,"rngNode":rngNode,"node":node,"message":message});}
mozile.rng.Validation.prototype.logError=function(rngNode,node,message){this.logMessage(rngNode,node,message,"Error");this.isValid=false;}
mozile.rng.Validation.prototype.getFirstError=function(){for(var m=0;m<this._messages.length;m++){if(this._messages[m].type!="Message")return this._messages[m];}
return null;}
mozile.rng.Validation.prototype.branch=function(){var validation=new mozile.rng.Validation();validation._currentParent=this._currentParent;return validation;}
mozile.rng.Validation.prototype.append=function(validation,status){this._messages=this._messages.concat(validation._messages);if(status)this.isValid=status;else if(!validation.isValid)this.isValid=false;}
mozile.rng.Validation.prototype.merge=function(validation){this.append(validation);this._validAttributes=this._validAttributes.concat(validation._validAttributes);if(validation.isEmpty)this.isEmpty=true;if(validation.allowText)this.allowText=true;if(validation._currentElement)this._currentElement=validation._currentElement;if(validation._lastValidElement)this._lastValidElement=validation._lastValidElement;}
mozile.rng.Validation.prototype.report=function(errorsOnly){mozile.require("mozile.util");mozile.require("mozile.xpath");var messages=new Array();var maxType=8;var maxName=0;var maxLocation=0;var msg;var location;var rngName
var lastNode;var lastLocation;for(var m=0;m<this._messages.length;m++){msg=this._messages[m];if(errorsOnly&&msg.type=="Message")continue;rngName="validation";if(msg.rngNode){if(msg.rngNode.getType())rngName=mozile.util.pad(msg.rngNode.getType(),11,true);if(msg.rngNode.getName)rngName=rngName+" "+msg.rngNode.getName();}
location="";if(msg.node){if(msg.node==lastNode)location=lastLocation;else{location=mozile.xpath.getXPath(msg.node);location=location.replace(/xmlns:/g,"");lastNode=msg.node;lastLocation=location;if(location.length>maxLocation)maxLocation=location.length;}}
if(rngName.length>maxName)maxName=rngName.length;messages.push([msg.type,rngName,location,msg.message]);}
var output=new Array();var maxNum=Math.ceil(Math.log(messages.length+1)/Math.log(10))+1;for(m=0;m<messages.length;m++){msg=messages[m];output.push(mozile.util.pad((m+1)+".",maxNum)+" "+mozile.util.pad(msg[0],maxType)+" "+mozile.util.pad(msg[1],maxName)+"  "+mozile.util.pad(msg[2],maxLocation)+"  "+msg[3]);}
return output.join(mozile.linesep);}
mozile.rng.Node=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Node.prototype._cached=[this._mustHaveCache,this._mayHaveCache,this._descendantElementsCache,this._type];mozile.rng.Node.prototype.toString=function(){var type=this.getType();type=type.charAt(0).toUpperCase()+type.substring(1);var name="";if(this.getName)name=" '"+this.getName()+"'";return"[object mozile.rng."+type+name+"]";}
mozile.rng.Node.prototype.getType=function(){if(!this._type)this._type=mozile.dom.getLocalName(this._element);return this._type;}
mozile.rng.Node.prototype.getSchema=function(){return this._schema;}
mozile.rng.Node.prototype.getGrammar=function(){var node=this;while(node){if(node.getType()=="grammar")return node;node=node.getParentNode();}
return null;}
mozile.rng.Node.prototype.getParentNode=function(){if(this._parentNode)return this._parentNode;return null;}
mozile.rng.Node.prototype.getParentElement=function(){var parent=this.getParentNode();while(parent){if(parent.getType()=="element")return parent;parent=parent.getParentNode();}
return null;}
mozile.rng.Node.prototype.getNextSibling=function(){var parent=this.getParentNode();if(!parent)return null;var siblings=parent.getChildNodes();for(var r=0;r<siblings.length;r++){if(siblings[r]==this){if(siblings[r+1])return siblings[r+1];}}
return null;}
mozile.rng.Node.prototype.getChildNodes=function(){if(!this._childNodes)this._childNodes=new Array();return this._childNodes;}
mozile.rng.Node.prototype.getChildNode=function(index){var children=this.getChildNodes();if(children&&children.length&&children.length>0&&index<children.length)
return children[index];else return null;}
mozile.rng.Node.prototype.appendChild=function(child){if(child){this.getChildNodes().push(child);child._parentNode=this;}
return child;}
mozile.rng.Node.prototype.removeChild=function(child){var children=new Array();for(var c=0;c<this.getChildNodes().length;c++){if(this.getChildNodes()[c]==child)continue;children.push(this.getChildNodes()[c]);}
if(children.length==this.getChildNodes().length)
throw Error("mozile.rng.Node.removeChild(): The given node is not child of this node.");else this._childNodes=children;child._parentNode=null;return child;}
mozile.rng.Node.prototype.getDescendants=function(types,deep){if(deep!==false)deep=true;var descendants=new Array();if(!deep&&mozile.rng.checkType(types,this.getType())){descendants.push(this);}
else{var result;for(var c=0;c<this.getChildNodes().length;c++){result=this.getChildNode(c).getDescendants(types,false);descendants=descendants.concat(result);}}
return descendants;}
mozile.rng.Node.prototype.mustContain=function(type){if(!this._mustHaveCache)this._mustHaveCache=new Object();if(this._mustHaveCache[type])return this._mustHaveCache[type];var result=false;for(var c=0;c<this.getChildNodes().length;c++){if(this.getChildNodes()[c].mustHave(type)){result=true;break;}}
this._mustHaveCache[type]=result;return result;}
mozile.rng.Node.prototype.mayContain=function(type){if(!this._mayHaveCache)this._mayHaveCache=new Object();if(this._mayHaveCache[type])return this._mayHaveCache[type];var result=false;for(var c=0;c<this.getChildNodes().length;c++){if(this.getChildNodes()[c].mayHave(type)){result=true;break;}}
this._mayHaveCache[type]=result;return result;}
mozile.rng.Node.prototype.mustHave=function(type){if(this.getType()==type)return true;else return this.mustContain(type);}
mozile.rng.Node.prototype.mayHave=function(type){if(this.getType()==type)return true;else return this.mayContain(type);}
mozile.rng.Node.prototype.reset=function(){for(var i=0;i<this._cached.length;i++){this._cached[i]=undefined;}}
mozile.rng.Node.prototype.resetAll=function(){this.reset();for(var c=0;c<this.getChildNodes().length;c++){this.getChildNodes()[c].reset;}}
mozile.rng.Node.prototype.selfValidate=function(validation){if(!mozile.dom.getFirstChildElement(this._element))
validation.logError(this,this._element,"This RNG element must have at least one child element.");return validation;}
mozile.rng.Node.prototype.selfValidateAll=function(validation){validation=this.selfValidate(validation);validation.logMessage(this,null,"Self validated.");for(var r=0;r<this.getChildNodes().length;r++){if(!validation.isValid)break;validation=this.getChildNodes()[r].selfValidateAll(validation);}
return validation;}
mozile.rng.Node.prototype.validate=function(node,validation){validation.logError(this,node,"The validate() method is not defined yet for "+this+".");return validation;}
mozile.rng.Grammar=function(element,schema){this._element=element;this._schema=schema;this._start=null;this._definitions=new Object;}
mozile.rng.Grammar.prototype=new mozile.rng.Node;mozile.rng.Grammar.prototype.constructor=mozile.rng.Grammar;mozile.rng.Grammar.prototype.getParentGrammar=function(){var parent=this.getParentNode();while(parent){if(parent.getType()=="grammar")return parent;parent=parent.getParentNode();}
return null;}
mozile.rng.Grammar.prototype.appendChild=function(child){if(child&&child.getType){switch(child.getType()){case"start":return this.setStart(child);break;case"define":return this.addDefinition(child);break;case"include":case"div":break;default:throw Error("RNG grammar element cannot have a child of type '"+child.getType()+"'.");break;}
this.getChildNodes().push(child);child._parentNode=this;}
return child;}
mozile.rng.Grammar.prototype.getStart=function(){if(this._start)return this._start;else return null;}
mozile.rng.Grammar.prototype.setStart=function(start){if(start.getType&&start.getType()=="start"){if(this._start)this._start.combine(start);else{this._start=start;this.getChildNodes().push(start);start._parentNode=this;}
return start;}
else return null;}
mozile.rng.Grammar.prototype.getDefinition=function(name){if(this._definitions[name])return this._definitions[name];else return null;}
mozile.rng.Grammar.prototype.addDefinition=function(definition){if(this._definitions[definition.getName()]){definition=this._definitions[definition.getName()].combine(definition);}
else{this._definitions[definition.getName()]=definition;this.getChildNodes().push(definition);definition._parentNode=this;}
return definition;}
mozile.rng.Grammar.prototype.mustHave=function(type){if(this.getType()==type)return true;var result=false;var start=this.getStart();if(start)result=start.mustHave(type);else throw Error("mozile.rng.Grammar.mustHave() requires getStart() to return a mozile.rng.Start object.");return result;}
mozile.rng.Grammar.prototype.mayHave=function(type){if(this.getType()==type)return true;var result=false;var start=this.getStart();if(start)result=start.mayHave(type);else throw Error("mozile.rng.Grammar.mayHave() requires getStart() to return a mozile.rng.Start object.");return result;}
mozile.rng.Grammar.prototype.validate=function(node,validation){var start=this.getStart();if(start)validation=start.validate(node,validation);else validation.logError(this,node,"RNG grammar element must have a start element in order to validate a node, but it does not.");return validation;}
mozile.rng.Start=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Start.prototype=new mozile.rng.Node;mozile.rng.Start.prototype.constructor=mozile.rng.Start;mozile.rng.Start.prototype.combine=mozile.rng.combine;mozile.rng.Start.prototype.selfValidate=function(validation){if(!this._element.parentNode||mozile.dom.getLocalName(this._element.parentNode)!="grammar")
validation.logError(this,this._element,"This RNG element must be the child of a grammar element.");if(!mozile.dom.getFirstChildElement(this._element))
validation.logError(this,this._element,"This RNG element must have at least one child element.");var combine=this._element.getAttribute("combine");if(combine){if(combine!="choice"&&combine!="interleave")
validation.logError(this,this._element,"This RNG 'start' element has an invalid 'combine' attribute value of '"+combine+"'.");}
return validation;}
mozile.rng.Start.prototype.validate=function(node,validation){var RNGChild=this.getChildNode(0);if(RNGChild)validation=RNGChild.validate(node,validation);return validation;}
mozile.rng.Element=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Element.prototype=new mozile.rng.Node;mozile.rng.Element.prototype.constructor=mozile.rng.Element;mozile.rng.Element.prototype._cached=[this._mustHaveCache,this._mayHaveCache,this._descendantElementsCache,this._type,this._name,this._localName,this._prefix,this._namespace];mozile.rng.Element.prototype.getName=mozile.rng.getName;mozile.rng.Element.prototype.getLocalName=mozile.rng.getLocalName;mozile.rng.Element.prototype.getPrefix=mozile.rng.getPrefix;mozile.rng.Element.prototype.getNamespace=function(){if(this._namespace)return this._namespace;if(this._element.getAttribute("ns"))this._namespace=this._element.getAttribute("ns");else if(this.getPrefix())this._namespace=mozile.dom.lookupNamespaceURI(this._element,this.getPrefix());else{var parent=this.getParentNode();while(parent&&parent._element){if(parent._element.getAttribute("ns")){this._namespace=parent._element.getAttribute("ns");break;}
parent=parent.getParentNode();}}
if(!this._namespace)this._namespace=null;return this._namespace;}
mozile.rng.Element.prototype.mustHave=function(type){if(this.getType()==type)return true;else return false;}
mozile.rng.Element.prototype.mayHave=function(type){if(this.getType()==type)return true;else return false;}
mozile.rng.Element.prototype.validate=function(node,validation){if(!node)throw Error("mozile.rng.Element.validate() requires a node.");if(!validation)validation=new mozile.rng.Validation();validation=this._validateElement(node,validation);if(!validation.isValid)return validation;validation.logMessage(this,node,"Validating element...");var result=new mozile.rng.Validation();result.setCurrentParent(node);result=this._validateChildElements(node,result);if(result.isValid)result=this._validateText(node,result);if(result.isValid)result=this._validateAttributes(node,result);if(result.isValid)result.logMessage(this,node,"Element is valid.");else result.logError(this,node,"Element is not valid.");validation.append(result);validation.setCurrentElement(node);return validation;}
mozile.rng.Element.prototype._validateElement=function(node,validation){if(!node)throw Error("mozile.rng.Element._validateElement() requires a node.");if(!validation)throw Error("mozile.rng.Element._validateElement() requires an mozile.rng.Validation object.");if(node.nodeType!=mozile.dom.ELEMENT_NODE)validation.logError(this,node,"Not an element.");if(!validation.isValid)return validation;if(mozile.dom.getLocalName(node)!=this.getLocalName())validation.logError(this,node,"Names do not match. '"+mozile.dom.getLocalName(node)+"' != '"+this.getLocalName()+"'");if(!validation.isValid)return validation;var ns=this.getNamespace();if(ns){if(node.namespaceURI!=ns)validation.logError(this,node,"Namespaces do not match. '"+node.namespaceURI+"' != '"+ns+"'");}
else{if(node.namespaceURI)validation.logError(this,node,"This element has the namespace '"+node.namespaceURI+"'but the RNG element has no namespace.");}
if(!validation.isValid)return validation;return validation;}
mozile.rng.Element.prototype._validateSequence=mozile.rng.validateSequence;mozile.rng.Element.prototype._validateChildElements=function(node,validation){if(!node)throw Error("mozile.rng.Element._validateChildElements() requires a node.");if(!validation)throw Error("mozile.rng.Element._validateChildElements() requires an mozile.rng.Validation object.");var RNGChildren=this.getChildNodes();var child=mozile.dom.getFirstChildElement(node);var r=0;if(!child){for(r=0;r<RNGChildren.length;r++){if(RNGChildren[r].mustHave("element")){validation.logError(this,node,"This element must have child elements, but it does not.");return validation;}}
if(node.firstChild)validation.logMessage(this,node,"Element has no child elements.");}
if(!child){if(node.attributes.length>0)child=node.attributes[0];else{for(r=0;r<RNGChildren.length;r++){if(RNGChildren[r].mustHave("attribute")){validation.logError(this,node,"This element must have attributes, but it does not.");return validation;}}}}
if(!child&&node.firstChild){child=node.firstChild;}
if(!child)validation.logMessage(this,node,"Element has no child nodes or attributes.");else{validation=this._validateSequence(child,validation);if(validation.isValid){var overflow;if(mozile.dom.getFirstChildElement(node)&&!validation.getCurrentElement())
overflow=mozile.dom.getFirstChildElement(node);if(validation.getCurrentElement()&&mozile.dom.getNextSiblingElement(validation.getCurrentElement()))
overflow=mozile.dom.getNextSiblingElement(validation.getCurrentElement());if(overflow){validation.logError(this,node,"There are elements which are not matched by any RNG rules: '"+overflow.nodeName+"'.");}}
if(mozile.dom.getFirstChildElement(node)&&validation.isValid)validation.logMessage(this,node,"All child elements are valid.");}
return validation;}
mozile.rng.Element.prototype._validateText=function(node,validation){if(!node)throw Error("mozile.rng.Element._validateText() requires a node.");if(!validation)throw Error("mozile.rng.Element._validateText() requires an mozile.rng.Validation object.");var child;for(var r=0;r<node.childNodes.length;r++){child=node.childNodes[r];if(child.nodeType!=mozile.dom.TEXT_NODE)continue;if(mozile.dom.isWhitespace(child))continue;if(validation.isEmpty)validation.logError(this,node,"This element contains text, but it is supposed to be empty.");if(!validation.allowText)validation.logError(this,node,"This element contains text but that is not allowed.");if(validation.isValid){validation.logMessage(this,node,"Element has text content.");break;}}
return validation;}
mozile.rng.Element.prototype._validateAttributes=function(node,validation){var attr;for(var r=0;r<node.attributes.length;r++){attr=node.attributes[r];if(mozile.dom.isIgnored(attr))continue;if(validation.isEmpty)validation.logError(this,node,"This element contains attributes, but it is supposed to be empty.");if(validation.isAttributeValid(attr))continue;validation.logError(this,node,"This element contains an invalid attribute: "+attr.nodeName);}
if(validation.isValid&&node.attributes.length>0)validation.logMessage(this,node,"All attributes are valid.");return validation;}
mozile.rng.Attribute=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Attribute.prototype=new mozile.rng.Node;mozile.rng.Attribute.prototype.constructor=mozile.rng.Attribute;mozile.rng.Attribute.prototype._cached=[this._mustHaveCache,this._mayHaveCache,this._descendantElementsCache,this._type,this._name,this._localName,this._prefix,this._namespace];mozile.rng.Attribute.prototype.getName=mozile.rng.getName;mozile.rng.Attribute.prototype.getLocalName=mozile.rng.getLocalName;mozile.rng.Attribute.prototype.getPrefix=mozile.rng.getPrefix;mozile.rng.Attribute.prototype.getNamespace=function(){if(this._namespace)return this._namespace;if(this._element.getAttribute("ns"))this._namespace=this._element.getAttribute("ns");else if(this.getPrefix()){if(this.getPrefix()=="xml")this._namespace=null;else this._namespace=mozile.dom.lookupNamespaceURI(this._element,this.getPrefix());}
if(!this._namespace)this._namespace=null;return this._namespace;}
mozile.rng.Attribute.prototype.selfValidate=function(validation){return validation;}
mozile.rng.Attribute.prototype.mustHave=function(type){if(this.getType()==type)return true;else return false;}
mozile.rng.Attribute.prototype.mayHave=function(type){if(this.getType()==type)return true;else return false;}
mozile.rng.Attribute.prototype.validate=function(node,validation){if(!node)throw Error("mozile.rng.Attribute.validate() requires a node.");if(!validation)throw Error("mozile.rng.Attribute.validate() requires an mozile.rng.Validation object.");var element=validation.getCurrentParent();if(!element)throw Error("mozile.rng.Attribute.validate() requires the Validation object to have a 'current parent' element.");if(element){var attr;var ns=this.getNamespace();if(ns){if(element.getAttributeNodeNS)attr=element.getAttributeNodeNS(ns,this.getLocalName());else{var prefix=mozile.dom.lookupPrefix(element,ns);if(prefix)attr=element.getAttributeNode(prefix+":"+this.getLocalName());}}
else attr=element.getAttributeNode(this.getLocalName());if(attr){validation.addAttribute(attr);validation.logMessage(this,element,"Attribute "+attr.nodeName+" validated.");}
else validation.logError(this,element,"Attribute "+this.getName()+" not found.");}
else validation.logError(this,node,"The node has no parent node.");return validation;}
mozile.rng.Text=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Text.prototype=new mozile.rng.Node;mozile.rng.Text.prototype.constructor=mozile.rng.Text;mozile.rng.Text.prototype.mustHave=function(type){if(this.getType()==type)return true;else return false;}
mozile.rng.Text.prototype.mayHave=function(type){if(this.getType()==type)return true;else return false;}
mozile.rng.Text.prototype.selfValidate=function(validation){if(mozile.dom.getFirstChildElement(this._element))
validation.logError(this,this._element,"This RNG element must not have any child elements.");return validation;}
mozile.rng.Text.prototype.validate=function(node,validation){if(!node)throw Error("mozile.rng.Text.validate() requires a node.");if(!validation)throw Error("mozile.rng.Text.validate() requires an mozile.rng.Validation object.");if(!validation.allowText)validation.logMessage(this,node.parentNode,"This element is allowed to contain text.");validation.allowText=true;return validation;}
mozile.rng.Empty=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Empty.prototype=new mozile.rng.Node;mozile.rng.Empty.prototype.constructor=mozile.rng.Empty;mozile.rng.Empty.prototype.mustHave=function(type){if(this.getType()==type)return true;else return false;}
mozile.rng.Empty.prototype.mayHave=function(type){if(this.getType()==type)return true;else return false;}
mozile.rng.Empty.prototype.selfValidate=function(validation){if(mozile.dom.getFirstChildElement(this._element))
validation.logError(this,this._element,"This RNG element must not have any child elements.");return validation;}
mozile.rng.Empty.prototype.validate=function(node,validation){if(!node)throw Error("mozile.rng.Empty.validate() requires a node.");if(!validation)throw Error("mozile.rng.Empty.validate() requires an mozile.rng.Validation object.");validation.isEmpty=true;validation.logMessage(this,node.parentNode,"This element must be empty.");return validation;}
mozile.rng.Group=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Group.prototype=new mozile.rng.Node;mozile.rng.Group.prototype.constructor=mozile.rng.Group;mozile.rng.Group.prototype.selfValidate=function(validation){if(mozile.dom.getChildElements(this._element).length<2)
validation.logError(this,this._element,"This RNG element must have at least two child elements.");return validation;}
mozile.rng.Group.prototype._validateSequence=mozile.rng.validateSequence;mozile.rng.Group.prototype.validate=function(node,validation){if(!node)throw Error("mozile.rng.Group.validate() requires a node.");if(!validation)throw Error("mozile.rng.Group.validate() requires an mozile.rng.Validation object.");validation=this._validateSequence(node,validation);if(validation.isValid)validation.logMessage(this,validation.getCurrentElement(),"Group is valid.");return validation;}
mozile.rng.Optional=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Optional.prototype=new mozile.rng.Node;mozile.rng.Optional.prototype.constructor=mozile.rng.Optional;mozile.rng.Optional.prototype.mustHave=function(type){if(this.getType()==type)return true;else return false;}
mozile.rng.Optional.prototype._validateSequence=mozile.rng.validateSequence;mozile.rng.Optional.prototype.validate=function(node,validation){if(!node)throw Error("mozile.rng.Optional.validate() requires a node.");if(!validation)throw Error("mozile.rng.Optional.validate() requires an mozile.rng.Validation object.");var result=validation.branch();result=this._validateSequence(node,result);if(result.isValid){validation.count=1;validation.merge(result);validation.logMessage(this,validation.getCurrentElement(),"Option is present.");}
else{if(mozile.rng.debug)validation.append(result,true);validation.logMessage(this,node,"Option is not present.");}
return validation;}
mozile.rng.ZeroOrMore=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.ZeroOrMore.prototype=new mozile.rng.Node;mozile.rng.ZeroOrMore.prototype.constructor=mozile.rng.ZeroOrMore;mozile.rng.ZeroOrMore.prototype.mustHave=function(type){if(this.getType()==type)return true;else return false;}
mozile.rng.ZeroOrMore.prototype.validate=function(node,validation){if(!node)throw Error("mozile.rng.ZeroOrMore.validate() requires a node.");if(!validation)throw Error("mozile.rng.ZeroOrMore.validate() requires an mozile.rng.Validation object.");validation=this._validateMany(node,validation);validation.logMessage(this,validation.getCurrentElement(),"ZeroOrMore matched "+validation.count+" times.");return validation;}
mozile.rng.ZeroOrMore.prototype._validateSequence=mozile.rng.validateSequence;mozile.rng.ZeroOrMore.prototype._validateMany=mozile.rng.validateMany;mozile.rng.OneOrMore=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.OneOrMore.prototype=new mozile.rng.Node;mozile.rng.OneOrMore.prototype.constructor=mozile.rng.OneOrMore;mozile.rng.OneOrMore.prototype.validate=function(node,validation){if(!node)throw Error("mozile.rng.OneOrMore.validate() requires a node.");if(!validation)throw Error("mozile.rng.OneOrMore.validate() requires an mozile.rng.Validation object.");validation=this._validateMany(node,validation);if(validation.count==0)validation.logError(this,validation.getCurrentElement(),"OneOrMore did not match any nodes.");validation.logMessage(this,validation.getCurrentElement(),"OneOrMore matched "+validation.count+" times.");return validation;}
mozile.rng.OneOrMore.prototype._validateSequence=mozile.rng.validateSequence;mozile.rng.OneOrMore.prototype._validateMany=mozile.rng.validateMany;mozile.rng.Choice=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Choice.prototype=new mozile.rng.Node;mozile.rng.Choice.prototype.constructor=mozile.rng.Choice;mozile.rng.Choice.prototype.mustHave=function(type){if(this.getType()==type)return true;if(!this._mustHaveCache)this._mustHaveCache=new Object();if(this._mustHaveCache[type])return this._mustHaveCache[type];var result=true;for(var c=0;c<this.getChildNodes().length;c++){if(!this.getChildNodes()[c].mustHave(type)){result=false;break;}}
this._mustHaveCache[type]=result;return result;}
mozile.rng.Choice.prototype.selfValidate=function(validation){if(mozile.dom.getChildElements(this._element).length<2)
validation.logError(this,this._element,"This RNG element must have at least two child elements.");return validation;}
mozile.rng.Choice.prototype.validate=function(node,validation){if(!node)throw Error("mozile.rng.Choice.validate() requires a node.");if(!validation)throw Error("mozile.rng.Choice.validate() requires an mozile.rng.Validation object.");var RNGChildren=this.getChildNodes();var r=0;for(r=0;r<RNGChildren.length;r++){if(validation.allowText)break;if(RNGChildren[r].getType()=="text")validation=RNGChildren[r].validate(node,validation);}
var result;for(r=0;r<RNGChildren.length;r++){if(RNGChildren[r].getType()=="text"&&node.nodeType==mozile.dom.ELEMENT_NODE)continue;result=validation.branch();result=RNGChildren[r].validate(node,result);if(result.isValid)break;else if(mozile.rng.debug)validation.append(result,true);}
if(result&&result.isValid){validation.merge(result);validation.logMessage(this,validation.getCurrentElement(),"Choice number "+(r+1)+" selected.");}
else validation.logError(this,node,"All choices failed to validate.");return validation;}
mozile.rng.Define=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Define.prototype=new mozile.rng.Node;mozile.rng.Define.prototype.constructor=mozile.rng.Define;mozile.rng.Define.prototype.getName=mozile.rng.getName;mozile.rng.Define.prototype.combine=mozile.rng.combine;mozile.rng.Define.prototype.selfValidate=function(validation){if(!this._element.parentNode)
validation.logError(this,this._element,"This RNG element must be the child of a 'grammar', 'include', or 'div' element.");var parentName=mozile.dom.getLocalName(this._element.parentNode);if(parentName!="grammar"&&parentName!="include"&&parentName!="div")
validation.logError(this,this._element,"This RNG element must be the child of a 'grammar', 'include', or 'div' element.");if(!mozile.dom.getFirstChildElement(this._element))
validation.logError(this,this._element,"This RNG element must have at least one child element.");if(!this.getName())
validation.logError(this,this._element,"This RNG element must have a 'name' attribute.");var combine=this._element.getAttribute("combine");if(combine){if(combine!="choice"&&combine!="interleave")
validation.logError(this,this._element,"This RNG 'define' element has an invalid 'combine' attribute value of '"+combine+"'.");}
return validation;}
mozile.rng.Define.prototype._validateSequence=mozile.rng.validateSequence;mozile.rng.Define.prototype.validate=function(node,validation){if(!node)throw Error("mozile.rng.Define.validate() requires a node.");if(!validation)throw Error("mozile.rng.Define.validate() requires an mozile.rng.Validation object.");validation=this._validateSequence(node,validation);if(validation.isValid)validation.logMessage(this,validation.getCurrentElement(),"Definition is valid.");return validation;}
mozile.rng.Ref=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Ref.prototype=new mozile.rng.Node;mozile.rng.Ref.prototype.constructor=mozile.rng.Ref;mozile.rng.Ref.prototype._cached=[this._type,this._name,this._grammar,this._definition];mozile.rng.Ref.prototype.getName=mozile.rng.getName;mozile.rng.Ref.prototype.getDefinition=function(){if(!this._grammar)this._grammar=this.getGrammar()
if(this._grammar){if(!this._definition)this._definition=this.getGrammar().getDefinition(this.getName());if(this._definition)return this._definition;}
return null;}
mozile.rng.Ref.prototype.getDescendants=function(types,deep){if(!deep&&mozile.rng.checkType(types,this.getType()))return new Array(this);else if(this.getDefinition())return this.getDefinition().getDescendants(types,deep);else return new Array();}
mozile.rng.Ref.prototype.mustHave=function(type){if(this.getType()==type)return true;if(this.getDefinition())return this.getDefinition().mustHave(type);else return false;}
mozile.rng.Ref.prototype.mayHave=function(type){if(this.getType()==type)return true;if(this.getDefinition())return this.getDefinition().mayHave(type);else return false;}
mozile.rng.Ref.prototype.selfValidate=function(validation){if(mozile.dom.getFirstChildElement(this._element))
validation.logError(this,this._element,"This RNG element must not have any child elements.");if(!this.getName())
validation.logError(this,this._element,"This RNG element must have a 'name' attribute.");return validation;}
mozile.rng.Ref.prototype.validate=function(node,validation){if(this.getDefinition()){validation.logMessage(this,node,"Reference followed.");validation=this.getDefinition().validate(node,validation);}
else validation.logError(this,node,"This reference does not have a matching definition.");return validation;}
mozile.rng.Data=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Data.prototype=new mozile.rng.Node;mozile.rng.Data.prototype.constructor=mozile.rng.Data;mozile.rng.Data.prototype.selfValidate=function(validation){if(!this._element.getAttribute("type"))
validation.logError(this,this._element,"This RNG element must have an 'type' attribute.");return validation;}
mozile.rng.Param=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Param.prototype=new mozile.rng.Node;mozile.rng.Param.prototype.constructor=mozile.rng.Param;mozile.rng.Value=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Value.prototype=new mozile.rng.Node;mozile.rng.Value.prototype.constructor=mozile.rng.Value;mozile.rng.Value.prototype.selfValidate=function(validation){for(var r=0;r<this._element.childNodes.length;r++){if(this._element.childNodes[r].nodeType==mozile.dom.TEXT_NODE&&!mozile.dom.isWhitespace(this._element.childNodes[r])){return validation;}}
validation.logError(this,this._element,"This RNG element must contain non-whitespace text.");return validation;}
mozile.rng.Include=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Include.prototype=new mozile.rng.Node;mozile.rng.Include.prototype.constructor=mozile.rng.Include;mozile.rng.Include.prototype.selfValidate=function(validation){if(!this._element.parentNode||mozile.dom.getLocalName(this._element.parentNode)!="grammar")
validation.logError(this,this._element,"This RNG element must be the child of a grammar element.");if(!this._element.getAttribute("href"))
validation.logError(this,this._element,"This RNG element must have an 'href' attribute.");return validation;}
mozile.rng.Include.prototype.include=function(validation){if(!validation)throw Error("mozile.rng.Include.include() requires an mozile.rng.Validation object.");var href=this._element.getAttribute("href");var schema=this.getSchema();var grammar=this.getGrammar();if(!href||!schema||!grammar){validation.logError(this,this._element,"mozile.rng.Include.include() requires an 'href', a schema, and a grammar, but one or more was missing.");return validation;}
var root;if(this.filepath)root=this.filepath;else if(schema.filepath)root=schema.filepath;var URI=mozile.getAbsolutePath(href,root);var filepath=mozile.getDirectory(URI);validation.logMessage(this,this._element,"Including RNG content from '"+URI+"'.");var rngDoc=schema.load(URI);if(!rngDoc||!rngDoc.documentElement||mozile.dom.getLocalName(rngDoc.documentElement)!="grammar"){validation.logError(this,null,"mozile.rng.Include.include() could not load schema at '"+URI+"'.");return validation;}
var children=rngDoc.documentElement.childNodes;for(var c=0;c<children.length;c++){var child=null;switch(mozile.dom.getLocalName(children[c])){case"start":child=schema.parseElement(children[c],validation);break;case"define":for(var i=0;i<this._element.childNodes.length;i++){var includeChild=this._element.childNodes[i];if(includeChild.nodeType!=mozile.dom.ELEMENT_NODE)continue;var includeChildName=includeChild.getAttribute("name");if(mozile.dom.getLocalName(includeChild)=="define"&&includeChildName==children[c].getAttribute("name")){validation.logMessage(this,includeChild,"Overriding defintition of '"+includeChildName+"'.");child=schema.parseElement(includeChild,validation);break;}}
if(!child)child=schema.parseElement(children[c],validation);break;case"include":child=schema.parseElement(children[c],validation);child.filepath=filepath;break;default:child=null;break;}
if(child)grammar.appendChild(child);}
return validation;}
mozile.rng.Interleave=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Interleave.prototype=new mozile.rng.Node;mozile.rng.Interleave.prototype.constructor=mozile.rng.Interleave;mozile.rng.Interleave.prototype.selfValidate=function(validation){if(mozile.dom.getChildElements(this._element).length<2)
validation.logError(this,this._element,"This RNG element must have at least two child elements.");return validation;}
mozile.rng.Interleave.prototype.validate=mozile.rng.validateInterleave;mozile.rng.Div=function(element,schema){this._element=element;this._schema=schema;}
mozile.rng.Div.prototype=new mozile.rng.Node;mozile.rng.Div.prototype.constructor=mozile.rng.Div;mozile.rng.Div.prototype.selfValidate=function(validation){if(!this._element.parentNode||mozile.dom.getLocalName(this._element.parentNode)!="grammar")
validation.logError(this,this._element,"This RNG element must be the child of a 'grammar' element.");return validation;}
mozile.rng.Div.prototype._validateSequence=mozile.rng.validateSequence;mozile.rng.Div.prototype.validate=function(node,validation){if(!node)throw Error("mozile.rng.Div.validate() requires a node.");if(!validation)throw Error("mozile.rng.Div.validate() requires an mozile.rng.Validation object.");validation=this._validateSequence(node,validation);if(validation.isValid)validation.logMessage(this,validation.getCurrentElement(),"Div is valid.");return validation;}
mozile.edit=new Object();mozile.edit.prototype=new mozile.Module;mozile.edit.editable=true;mozile.edit.status=false;mozile.edit.NEXT=1;mozile.edit.PREVIOUS=-1;mozile.edit.marked=new Array();mozile.edit.allCommands=new Object();mozile.edit.allCommands.toString=function(){var keys=new Array();for(var key in this){switch(key){case"toString":case"undefined":break;default:keys.push(key);}}
keys.sort();return keys.join(", ");}
mozile.edit.keyCodes={8:"Backspace",9:"Tab",12:"Clear",13:"Return",14:"Enter",19:"Pause",27:"Escape",32:"Space",33:"Page-Up",34:"Page-Down",35:"End",36:"Home",37:"Left",38:"Up",39:"Right",40:"Down",45:"Insert",46:"Delete",112:"F1",113:"F2",114:"F3",115:"F4",116:"F5",117:"F6",118:"F7",119:"F8",121:"F9",122:"F10",123:"F11",123:"F12"}
mozile.edit.getContainer=function(element){if(!element||!element.nodeType)return null;if(element.nodeType!=mozile.dom.ELEMENT_NODE)element=element.parentNode;if(!element||!element.nodeType)return null;var doc=element.ownerDocument;while(element&&element.nodeType&&element.nodeType==mozile.dom.ELEMENT_NODE){if(mozile.edit.isEditableElement(element))return element;switch(element.getAttribute("contentEditable")){case"true":mozile.editElement(element);return element;case"false":return null;}
element=element.parentNode;}
return null;}
mozile.edit.isEditable=function(node){if(!node)return false;var container=mozile.edit.getContainer(node)
if(container&&container!=node)return true;else return false;}
mozile.edit.isEditableElement=function(element){if(element&&mozile.edit.getMark(element,"editable"))return true;return false;}
mozile.edit.setStatus=function(status){status=Boolean(status);if(mozile.edit.status!=status){mozile.edit.status=status;if(mozile.useDesignMode==true&&typeof(document.documentElement.contentEditable)=="undefined"){document.designMode=(status)?"on":"off";}}
return mozile.edit.status;}
mozile.edit.enable=function(){mozile.edit.editable=true;mozile.edit.start();if(mozile.gui)mozile.gui.show();var list=mozile.edit.getMarked("editingDisabled",true);for(var i=0;i<list.length;i++){mozile.edit.setMark(list[i],"editingDisabled",undefined);list[i].setAttribute("contentEditable","true");}
return mozile.edit.editable;}
mozile.edit.disable=function(){mozile.edit.editable=false;mozile.edit.stop();if(mozile.gui)mozile.gui.hide();var list=mozile.dom.getElements("contentEditable","true");for(var i=0;i<list.length;i++){mozile.edit.setMark(list[i],"editingDisabled",true);list[i].setAttribute("contentEditable","false");}
return mozile.edit.editable;}
mozile.edit.start=function(){return mozile.edit.setStatus(true);}
mozile.edit.stop=function(){return mozile.edit.setStatus(false);}
mozile.edit.setMark=function(element,key,value){if(!element||element.nodeType==undefined)return null;if(element.nodeType!=mozile.dom.ELEMENT_NODE)return null;if(!key||typeof(key)!="string")return null;try{if(element.mozile==undefined||typeof(element.mozile)!="object"){element.mozile=new Object();mozile.edit.marked.push(element);}
element.mozile[key]=value;return value;}catch(e){return null;}}
mozile.edit.getMark=function(element,key){if(!element||element.nodeType==undefined)return undefined;if(element.nodeType!=mozile.dom.ELEMENT_NODE)return undefined;if(!key||typeof(key)!="string")return undefined;if(element.mozile==undefined||!element.mozile)return undefined;if(element.mozile[key]==undefined)return undefined;return element.mozile[key];}
mozile.edit.getMarked=function(key,value){var list=new Array();if(!key||typeof(key)!="string")return list;var element;for(var i=0;i<mozile.edit.marked.length;i++){element=mozile.edit.marked[i];if(!element.mozile||element.mozile[key]==undefined)continue;if(value===undefined)list.push(element);else if(element.mozile[key]==value)list.push(element);}
return list;}
mozile.edit.lookupRNG=function(node){if(!node)return null;var element=node;if(node.nodeType!=mozile.dom.ELEMENT_NODE)element=node.parentNode;if(!mozile.schema)return null;var name=mozile.dom.getLocalName(element);if(name&&mozile.dom.isHTML(node))name=name.toLowerCase();var matches=mozile.schema.getNodes("element",name);if(matches.length>0)return matches[0];else return null;}
mozile.edit.parseMES=function(container,node){if(node.nodeType!=mozile.dom.ELEMENT_NODE)return;var command,define;for(var i=0;i<node.childNodes.length;i++){var child=node.childNodes[i];switch(mozile.dom.getNamespaceURI(child)){case mozile.xml.ns.mes:switch(mozile.dom.getLocalName(child)){case"ref":define=mozile.edit.followMESRef(child);if(define)mozile.edit.parseMES(container,define);break;case"command":command=mozile.edit.generateCommand(child);if(command)container.addCommand(command);break;case"group":command=mozile.edit.generateCommand(child);if(command){container.addCommand(command);if(command._commands.length==0)
mozile.edit.parseMES(command,child);}
break;}
break;case mozile.xml.ns.rng:if(child.nodeName=="ref"){var name=child.getAttribute("name");if(!name)continue;define=mozile.schema._root.getDefinition(name);mozile.edit.parseMES(container,define._element);}
break;}}}
mozile.edit.followMESRef=function(element){var define=mozile.edit.getMark(element,"define");if(define&&define.nodeType&&define.nodeType==mozile.dom.ELEMENT_NODE)
return define;var name=element.getAttribute("name");if(!name)return null;var node=element;while(node){if(mozile.dom.getNamespaceURI(node)==mozile.xml.ns.rng&&mozile.dom.getLocalName(node)=="grammar")break;else node=node.parentNode;}
if(!node)return null;define=null;var child;for(var i=0;i<node.childNodes.length;i++){child=node.childNodes[i];if(mozile.dom.getNamespaceURI(child)==mozile.xml.ns.mes&&mozile.dom.getLocalName(child)=="define"&&child.getAttribute("name")==name){define=child;break;}}
if(define){mozile.edit.setMark(element,"define",define);return define;}
else return null;}
mozile.edit.generateCommands=function(schema){var elements=schema.getNodes("element");var name,uniqueName;var j=0;for(var i=0;i<elements.length;i++){if(elements[i].commands==undefined){name=elements[i].getName()+"_commands";uniqueName=name;if(mozile.edit.getCommand(uniqueName)){j=0;while(true){uniqueName=name+"_"+j;if(!mozile.edit.getCommand(uniqueName))break;j++;}}
elements[i].commands=new mozile.edit.CommandGroup(uniqueName);}
elements[i].commands.addCommand(mozile.edit.navigateLeftRight);if(elements[i].mayContain("text")){elements[i].commands.addCommand(mozile.edit.insertText);elements[i].commands.addCommand(mozile.edit.removeText);}
if(mozile.edit.remove)elements[i].commands.addCommand(mozile.edit.remove);mozile.edit.parseMES(elements[i].commands,elements[i]._element);}}
mozile.edit.generateCommand=function(node){var name=node.getAttribute("name");if(!name)return null;if(mozile.edit.allCommands[name])return mozile.edit.allCommands[name];var command;if(mozile.dom.getLocalName(node)=="command"){var className=node.getAttribute("class");if(className&&mozile.edit[className]){eval("command = new mozile.edit."+className+"(name)");}
else command=new mozile.edit.Command(name);var child=node.firstChild;while(child){if(child.nodeType==mozile.dom.ELEMENT_NODE){switch(mozile.dom.getLocalName(child)){case"element":var element=mozile.dom.getFirstChildElement(child);if(child.getAttribute("import")=="true")
element=mozile.dom.importNode(element,true);if(element)command.element=element;break;case"script":command.script=child;break;}}
child=child.nextSibling;}}
else if(mozile.dom.getLocalName(node)=="group"){command=new mozile.edit.CommandGroup(name);}
else return null;command.node=node;var properties=["priority","label","image","tooltip","accel","makesChanges","watchesChanges","element","text","remove","nested","direction","target","collapse","copyAttributes","className","styleName","styleValue"];for(var i=0;i<properties.length;i++){var property=properties[i];if(node.getAttribute(property)){var value=node.getAttribute(property);if(value.toLowerCase()=="true")value=true;else if(value.toLowerCase()=="false")value=false;command[property]=value;}}
if(command.accel){command.accels=mozile.edit.splitAccelerators(command.accel);}
if(command.target&&!command.direction){command.direction=null;}
if(command.script){child=command.script.firstChild;while(child){if(child.nodeType==mozile.dom.TEXT_NODE||child.nodeType==mozile.dom.CDATA_SECTION_NODE){command.evaluate(child.data);}
child=child.nextSibling;}}
return command;}
mozile.edit.checkAccelerators=function(event,accelerators){if(!event)return false;if(typeof(accelerators)!="object"||!accelerators.length)return false;for(var i=0;i<accelerators.length;i++){if(mozile.edit.checkAccelerator(event,accelerators[i]))return true;}
return false;}
mozile.edit.checkAccelerator=function(event,accelerator){if(!event)return false;if(typeof(accelerator)!="string")return false;if(mozile.browser.isIE){if(event.type!="keydown"){if(event.type!="keypress")return false;if(event.keyCode&&!mozile.edit.keyCodes[event.keyCode])return false;}}
else if(event.type!="keypress")return false;if(event.accel==undefined)event.accel=mozile.edit.generateAccelerator(event);if(event.accel.toLowerCase()==accelerator.toLowerCase())return true;else return false;}
mozile.edit.generateAccelerator=function(event){if(!event)return"";var accel="";if(event.metaKey)accel=accel+"Meta-";if(event.ctrlKey)accel=accel+"Control-";if(event.altKey)accel=accel+"Alt-";if(event.shiftKey)accel=accel+"Shift-";if(event.keyCode&&mozile.edit.convertKeyCode(event.keyCode)){accel=accel+mozile.edit.convertKeyCode(event.keyCode);}
else if(event.charCode==32)accel=accel+"Space";else accel=accel+String.fromCharCode(event.charCode).toUpperCase();var command="Control";if(mozile.os.isMac)command="Meta";accel=accel.replace(command,"Command");return accel;}
mozile.edit.splitAccelerators=function(accelerators){var accels=new Array();var split=accelerators.split(/\s/);var accel;for(var i=0;i<split.length;i++){accel=split[i];accel=accel.replace(/\s+/g,"");if(accel)accels.push(accel);}
return accels;}
mozile.edit.parseAccelerator=function(accelerator){accelerator=accelerator.replace(/\s.*/,"");var accel={command:false,meta:false,ctrl:false,alt:false,shift:false,charCode:0,character:"",abbr:""}
if(accelerator.indexOf("Command")>-1)accel.command=true;if(accelerator.indexOf("Meta")>-1)accel.meta=true;if(accelerator.indexOf("Control")>-1)accel.ctrl=true;if(accelerator.indexOf("Alt")>-1)accel.alt=true;if(accelerator.indexOf("Shift")>-1)accel.shift=true;accel.character=accelerator.substring(accelerator.lastIndexOf("-")+1);if(mozile.os.isMac){if(accel.ctrl)accel.abbr+="\u2303";if(accel.alt)accel.abbr+="\u2325";if(accel.shift)accel.abbr+="\u21E7";if(accel.command)accel.abbr+="\u2318";accel.abbr+=accel.character;}
else{if(accel.command)accel.abbr+="Ctrl+";if(accel.alt)accel.abbr+="Alt+";if(accel.shift)accel.abbr+="Shift+";accel.abbr+=accel.character;}
return accel;}
mozile.edit.convertKeyCode=function(keyCode){if(mozile.edit.keyCodes[keyCode])return mozile.edit.keyCodes[keyCode];else return null;}
mozile.edit.getCommand=function(name){if(mozile.edit.allCommands[name])return mozile.edit.allCommands[name];else return null;}
mozile.execCommand=function(name,a1,a2,a3,a4,a5,a6,a7,a8,a9,a10){if(!name)return null;var command=mozile.edit.getCommand(name);if(!command)return null;var selection=mozile.dom.selection.get();selection.restore();var state=command.request(null,true,a1,a2,a3,a4,a5,a6,a7,a8,a9,a10);if(!state)return state;mozile.edit.done(state);if(state.changesMade){var event=new Object();mozile.edit._getNode(event);if(mozile.gui)mozile.gui.update(event,state.changesMade);mozile.notifyChange(event.node);}
return state;}
mozile.edit.extendRNG=function(){mozile.rng.Element.prototype.create=function(parent){var node=mozile.dom.createElement(this.getName());if(parent){parent.appendChild(node);return parent;}
else return node;}}
if(mozile.rng)mozile.edit.extendRNG();mozile.edit.State=function(command,selection){this.command=command;this.selection=null;if(selection!==false){if(!selection)selection=mozile.dom.selection.get();this.selection={before:selection.store()};}
this.reversible=true;this.cancel=true;this.changesMade=command.makesChanges;this.executed=false;}
mozile.edit.State.prototype.toString=function(){return"[object mozile.edit.State]";}
mozile.edit.State.prototype.storeNode=function(input){if(!input)return null;if(typeof(input)=="string"){if(input.indexOf("/")!=0)return null;return input;}
else{var xpath=mozile.xpath.getXPath(input);if(xpath)return xpath;else return null;}
return null;}
mozile.edit.Command=function(name){this.name=name;this.group=false;this.makesChanges="node";this.watchesChanges="node";mozile.edit.allCommands[this.name]=this;}
mozile.edit.Command.prototype.toString=function(){return"[object mozile.edit.Command '"+this.name+"']";}
mozile.edit.Command.prototype.evaluate=function(code){eval(code);}
mozile.edit.Command.prototype.respond=function(change){if(!change||typeof(change)!="string")return false;if(change=="none")return false;switch(this.watchesChanges){case"none":return false;case"state":if(change=="state")return true;case"text":if(change=="text")return true;case"node":if(change=="node")return true;}
return false;}
mozile.edit.Command.prototype.isAvailable=function(event){return true;}
mozile.edit.Command.prototype.isActive=function(event){return false;}
mozile.edit.Command.prototype.test=function(event){if(event){if(this.accels)return mozile.edit.checkAccelerators(event,this.accels);else if(this.accel){this.accels=mozile.edit.splitAccelerators(this.accel);return mozile.edit.checkAccelerator(event,this.accel);}
else return false;}
return true;}
mozile.edit.Command.prototype.prepare=function(event){var state=new mozile.edit.State(this);if(this.prompt){if(!this.prompt(event,state))return null;}
return state;}
mozile.edit.Command.prototype.trigger=function(event){if(this.test(event)){return this.execute(this.prepare(event),true);}
return null;}
mozile.edit.Command.prototype.request=function(state,fresh,a1,a2,a3,a4,a5,a6,a7,a8,a9,a10){var test=this.test(null,a1,a2,a3,a4,a5,a6,a7,a8,a9,a10);if(!test)return null;var newState=this.prepare(null,a1,a2,a3,a4,a5,a6,a7,a8,a9,a10);if(!newState)return null;newState=this.execute(newState,fresh);if(!newState||!newState.executed)return null;if(state&&typeof(state)=="object"){if(!state.actions)state.actions=new Array();state.actions.push(newState);}
return newState;}
mozile.edit.Command.prototype.execute=function(state,fresh){mozile.debug.inform("mozile.edit.Command.execute","Command '"+this.name+"' executed with state "+state);state.executed=true;return state;}
mozile.edit.Command.prototype.unexecute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.after);if(state.actions){for(var i=state.actions.length-1;i>=0;i--){state.actions[i]=state.actions[i].command.unexecute(state.actions[i],fresh);if(state.actions[i].executed)mozile.debug.inform(this.name+".unexecute","Child command "+i+" failed to unexecute.");}}
selection.restore(state.selection.before);state.executed=false;return state;}
mozile.edit.CommandGroup=function(name){this.name=name;this.group=true;this.makesChanges="none";this.watchesChanges="none";this._commands=new Array();this._priority=new Array();mozile.edit.allCommands[this.name]=this;}
mozile.edit.CommandGroup.prototype=new mozile.edit.Command;mozile.edit.CommandGroup.prototype.constructor=mozile.edit.CommandGroup;mozile.edit.CommandGroup.prototype.toString=function(){return"[object mozile.edit.CommandGroup '"+this.name+"']";}
mozile.edit.CommandGroup.prototype.addCommand=function(command){if(!command)return null;if(!this._commands)this._commands=new Array();if(!this._priority)this._priority=new Array();for(var i=0;i<this._commands.length;i++){if(this._commands[i]==command)return null;}
this._commands.push(command);this._priority.push(command);this._priority.sort(this.compareCommands);return command;}
mozile.edit.CommandGroup.prototype.removeCommands=function(){this._commands=new Array();this._priority=new Array();}
mozile.edit.CommandGroup.prototype.compareCommands=function(command1,command2){if(command1.priority==undefined||Number(command1.priority)==NaN)
command1.priority=0;if(command2.priority==undefined||Number(command2.priority)==NaN)
command2.priority=0;return command2.priority-command1.priority;}
mozile.edit.CommandGroup.prototype.trigger=function(event){if(!this._priority)return null;var state;for(var i=0;i<this._priority.length;i++){state=this._priority[i].trigger(event);if(state)return state;}
return null;}
mozile.edit.commands=new mozile.edit.CommandGroup("commands");mozile.edit.defaults=new mozile.edit.CommandGroup("defaults");mozile.edit._undoStack=new Array();mozile.edit._undoIndex=-1;mozile.edit.currentState=null;mozile.edit.dumpUndoStack=function(){var entries=new Array("Undo Stack [ "+mozile.edit._undoIndex+" / "+mozile.edit._undoStack.length+" ]");for(var i=0;i<mozile.edit._undoStack.length;i++){var picked="  ";if(i==mozile.edit._undoIndex)picked="> "
entries.push(picked+i+". "+mozile.edit._undoStack[i].command.name);}
return entries.join("\n");}
mozile.edit.done=function(state){if(!state||!state.reversible)return;mozile.edit._undoStack=mozile.edit._undoStack.slice(0,mozile.edit._undoIndex+1);mozile.edit._undoStack.push(state);mozile.edit._undoIndex=mozile.edit._undoStack.length-1;mozile.edit.setCurrentState();}
mozile.edit.setCurrentState=function(){mozile.edit.currentState=mozile.edit._undoStack[mozile.edit._undoIndex];}
mozile.edit.save=new mozile.edit.Command("Save");mozile.edit.save.accel="Command-S";mozile.edit.save.tooltip="Save changes";mozile.edit.save.image="silk/page_save";mozile.edit.save.makesChanges="none";mozile.edit.save.watchesChanges="state";mozile.edit.commands.addCommand(mozile.edit.save);mozile.edit.save.isAvailable=function(event){if(!mozile.save)return false;if(mozile.save.isSaved())return false;else return true;}
mozile.edit.save.execute=function(state,fresh){mozile.save.save();state.reversible=false;state.executed=true;return state;}
mozile.edit.source=new mozile.edit.Command("Source");mozile.edit.source.tooltip="View source";mozile.edit.source.image="silk/html";mozile.edit.source.makesChanges="none";mozile.edit.source.watchesChanges="none";mozile.edit.commands.addCommand(mozile.edit.source);mozile.edit.source.execute=function(state,fresh){if(mozile.save&&mozile.gui){var content=mozile.save.getContent(document);content=mozile.save.cleanMarkup(content);mozile.gui.display("<h3>Page Source</h3>\n<pre>"+content+"</pre>");}
state.reversible=false;state.executed=true;return state;}
mozile.edit.debug=new mozile.edit.Command("Debug");mozile.edit.debug.accel="Command-D";mozile.edit.debug.tooltip="Show debugging messages";mozile.edit.debug.image="silk/bug";mozile.edit.debug.makesChanges="none";mozile.edit.debug.watchesChanges="none";mozile.edit.commands.addCommand(mozile.edit.debug);mozile.edit.debug.execute=function(state,fresh){mozile.debug.show();state.reversible=false;state.executed=true;return state;}
mozile.edit.undo=new mozile.edit.Command("Undo");mozile.edit.undo.accel="Command-Z";mozile.edit.undo.tooltip="Undo the last action";mozile.edit.undo.image="silk/arrow_undo";mozile.edit.undo.makesChanges="node";mozile.edit.undo.watchesChanges="state";mozile.edit.commands.addCommand(mozile.edit.undo);mozile.edit.undo.test=function(event){if(mozile.edit._undoIndex<0)return false;if(event){return mozile.edit.checkAccelerator(event,this.accel);}
return true;}
mozile.edit.undo.isAvailable=function(event){if(mozile.edit._undoIndex<0)return false;else return true;}
mozile.edit.undo.prepare=function(event,repeated){var state=new mozile.edit.State(this,false);state.repeated=false;if(repeated)state.repeated=repeated;if(event)state.repeated=event.repeat;state.reversible=false;return state;}
mozile.edit.undo.execute=function(state,fresh){var undoState=mozile.edit._undoStack[mozile.edit._undoIndex];if(undoState){undoState.command.unexecute(undoState,false);mozile.edit._undoIndex--;mozile.edit.setCurrentState();state.changesMade=undoState.changesMade;}
state.executed=true;return state;}
mozile.edit.redo=new mozile.edit.Command("Redo");mozile.edit.redo.accel="Command-Shift-Z";mozile.edit.redo.tooltip="Redo the last action";mozile.edit.redo.image="silk/arrow_redo";mozile.edit.redo.makesChanges="node";mozile.edit.redo.watchesChanges="state";mozile.edit.commands.addCommand(mozile.edit.redo);mozile.edit.redo.test=function(event){if(mozile.edit._undoIndex+1>=mozile.edit._undoStack.length)return false;if(event){return mozile.edit.checkAccelerator(event,this.accel);}
return true;}
mozile.edit.redo.isAvailable=function(event){if(mozile.edit._undoIndex+1>=mozile.edit._undoStack.length)return false;else return true;}
mozile.edit.redo.prepare=function(event,repeated){var state=new mozile.edit.State(this,false);state.repeated=false;if(repeated)state.repeated=repeated;if(event)state.repeated=event.repeat;state.reversible=false;return state;}
mozile.edit.redo.execute=function(state,fresh){var redoState=mozile.edit._undoStack[mozile.edit._undoIndex+1];if(redoState){mozile.edit._undoIndex++;redoState.command.execute(redoState,false);mozile.edit.setCurrentState();state.changesMade=redoState.changesMade;}
state.executed=true;return state;}
mozile.edit.clipboard=null;mozile.edit.updateClipboard=function(){}
mozile.edit.copy=new mozile.edit.Command("Copy");mozile.edit.copy.accel="Command-C";mozile.edit.copy.tooltip="Copy selection to clipboard";mozile.edit.copy.image="silk/page_copy";mozile.edit.commands.addCommand(mozile.edit.copy);mozile.edit.copy.prepare=function(event){var state=new mozile.edit.State(this,false);state.reversible=false;state.cancel=false;return state;}
mozile.edit.copy.execute=function(state,fresh){var selection=mozile.dom.selection.get();var range=selection.getRangeAt(0);if(range.commonAncestorContainer.nodeType==mozile.dom.TEXT_NODE||!mozile.edit.rich){mozile.edit.clipboard=range.toString();}
else mozile.edit.clipboard=range.cloneContents();state.executed=true;return state;}
mozile.edit.cut=new mozile.edit.Command("Cut");mozile.edit.cut.accel="Command-X";mozile.edit.cut.tooltip="Cut selection to clipboard";mozile.edit.cut.image="silk/cut";mozile.edit.commands.addCommand(mozile.edit.cut);mozile.edit.cut.test=function(event){if(event){if(!event.editable)return false;if(!mozile.edit.checkAccelerator(event,this.accel))return false;if(!mozile.edit.rich){if(event.node)return false;if(event.node.nodeType!=mozile.dom.TEXT_NODE)return false;}}
return true;}
mozile.edit.cut.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);var range=selection.getRangeAt(0);state.actions=new Array();if(range.commonAncestorContainer.nodeType==mozile.dom.TEXT_NODE||!mozile.edit.rich){mozile.edit.clipboard=range.toString();mozile.edit.removeText.request(state,fresh);}
else{mozile.edit.clipboard=range.cloneContents();mozile.edit.remove.request(state,fresh);}
state.selection.after=selection.store();state.executed=true;return state;}
mozile.edit.paste=new mozile.edit.Command("Paste");mozile.edit.paste.accel="Command-V";mozile.edit.paste.tooltip="Paste the clipboard contents";mozile.edit.paste.image="silk/page_paste";mozile.edit.commands.addCommand(mozile.edit.paste);mozile.edit.paste.test=function(event){if(!mozile.edit.clipboard)return false;if(event){if(!event.editable)return false;if(!mozile.edit.checkAccelerator(event,this.accel))return false;if(!mozile.edit.rich){if(typeof(mozile.edit.clipboard)!="string")return false;if(!event.node)return false;if(event.node.nodeType!=mozile.dom.TEXT_NODE)return false;}}
return true;}
mozile.edit.paste.prepare=function(event){var state=new mozile.edit.State(this);if(typeof(mozile.edit.clipboard)=="string"){state.content=mozile.edit.clipboard;}
else state.content=mozile.edit.clipboard.cloneNode(true);state.reversible=true;return state;}
mozile.edit.paste.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);var range=selection.getRangeAt(0);state.actions=new Array();if(!selection.isCollapsed){if(mozile.edit.remove)mozile.edit.remove.request(state,fresh);else mozile.edit.removeText.request(state,fresh);}
if(typeof(state.content)=="string"){mozile.edit.insertText.request(state,fresh,mozile.edit.NEXT,state.content);}
else{var previousNode=null;var nextNode=null;if(selection.focusNode.nodeType==mozile.dom.TEXT_NODE){var newState=mozile.edit.splitNode.request(state,fresh,selection.focusNode,selection.focusOffset);previousNode=newState.oldContainer;nextNode=newState.newContainer;}
else{previousNode=selection.focusNode.childNodes[selection.focusOffset-1];nextNode=selection.focusNode.childNodes[selection.focusOffset];}
var moveNode,firstNode,lastNode;for(var i=state.content.childNodes.length-1;i>=0;i--){moveNode=state.content.childNodes[i].cloneNode(true);mozile.edit.insertNode.request(state,fresh,null,previousNode,moveNode);if(i==state.content.childNodes.length-1)lastNode=moveNode;if(i==0)firstNode=moveNode;}
var IP=mozile.edit.getInsertionPoint(firstNode,mozile.edit.NEXT);if(IP){IP.select();IP=mozile.edit.getInsertionPoint(lastNode,mozile.edit.PREVIOUS);if(IP)IP.extend();}
mozile.edit._normalize(state,fresh,lastNode,nextNode);mozile.edit._normalize(state,fresh,previousNode,firstNode);}
state.selection.after=selection.store();state.executed=true;return state;}
mozile.edit.test=new mozile.edit.Command("Test");mozile.edit.test.accel="Escape";mozile.edit.commands.addCommand(mozile.edit.test);mozile.edit.test.execute=function(state,fresh){mozile.require("mozile.util");var output=new Array();output.push("Debugging Information:");output.push("Undo: "+mozile.edit._undoIndex+" / "+mozile.edit._undoStack.length);var selection=mozile.dom.selection.get();output.push("Selection:\n"+mozile.util.dumpValues(selection.store()));var element=selection.focusNode;if(element.nodeType!=mozile.dom.ELEMENT_NODE)element=element.parentNode;var rng=mozile.edit.lookupRNG(element);if(rng){if(rng.getName())output.push("RNG: "+rng+" "+rng.getName());else output.push("RNG: "+rng);output.push("Text? "+rng.mayContain("text"));}
else output.push("No matching RNG object.");alert(output.join("\n"));state.reversible=false;state.executed=true;return state;}
mozile.edit.tweak=new mozile.edit.Command("Tweak");mozile.edit.tweak.accel="Command-E";mozile.edit.commands.addCommand(mozile.edit.tweak);mozile.edit.tweak.execute=function(state,fresh){if(mozile.browser.isIE){var selection=mozile.dom.selection.get();var range=selection.getRangeAt(0);range._range.move("character",1);selection.removeAllRanges();selection.addRange(range);}
state.reversible=false;state.executed=true;return state;}
mozile.require("mozile.edit.InsertionPoint");mozile.edit.navigateLeftRight=new mozile.edit.Command("NavigateLeftRight");mozile.edit.navigateLeftRight.priority=15;mozile.edit.navigateLeftRight.accel="Left Right";mozile.edit.navigateLeftRight.accels=mozile.edit.splitAccelerators(mozile.edit.navigateLeftRight.accel);mozile.edit.navigateLeftRight.makesChanges="none";mozile.edit.navigateLeftRight.watchesChanges="none";mozile.edit.navigateLeftRight.prepare=function(event,direction,extend){var state=new mozile.edit.State(this,false);state.direction=mozile.edit.NEXT;if(direction)state.direction=direction;else if(event&&event.keyCode==37)state.direction=mozile.edit.PREVIOUS;state.extend=false;if(extend)state.extend=extend;else if(event)state.extend=event.shiftKey;state.reversible=false;return state;}
mozile.edit.navigateLeftRight.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(selection.isCollapsed||state.extend){var IP=selection.getInsertionPoint();IP.seek(state.direction,document.documentElement);if(state.extend)IP.extend();else IP.select();}
else{if(state.direction==mozile.edit.NEXT)selection.collapseToEnd();else selection.collapseToStart();}
state.executed=true;return state;}
mozile.edit.Navigate=function(name){this.name=name;this.group=false;this.remove=true;this.makesChanges="none";this.watchesChanges="none";this.target="text";this.direction="next";this.collapse=null;mozile.edit.allCommands[this.name]=this;}
mozile.edit.Navigate.prototype=new mozile.edit.Command;mozile.edit.Navigate.prototype.constructor=mozile.edit.Navigate;mozile.edit.Navigate.prototype.prepare=function(event){var state=new mozile.edit.State(this);var target=mozile.edit._getTarget(event,this.target,this.direction);state.target=state.storeNode(target);if(this.prompt){if(!this.prompt(event,state))return null;}
state.reversible=false;return state;}
mozile.edit.Navigate.prototype.execute=function(state,fresh){var selection=mozile.dom.selection.get();var target=mozile.xpath.getNode(state.target);var direction=mozile.edit.NEXT;if(this.direction=="previous")direction=mozile.edit.PREVIOUS;var IP=mozile.edit.getInsertionPoint(target,direction);if(IP){IP.select();IP=mozile.edit.getInsertionPoint(target,-1*direction);if(IP)IP.extend();if(this.collapse=="start")selection.collapseToStart();else if(this.collapse=="end")selection.collapseToEnd();selection.scroll();}
state.executed=true;return state;}
mozile.edit.insertText=new mozile.edit.Command("InsertText");mozile.edit.insertText.priority=10;mozile.edit.insertText.makesChanges="text";mozile.edit.insertText.watchesChanges="none";mozile.edit.insertText.test=function(event,direction,content,node){if(event){if(event.type!="keypress")return false;if(event.ctrlKey||event.metaKey)return false;if(!mozile.os.isMac&&event.altKey)return false;if(!node&&event.charCode==32&&!mozile.alternateSpace){var range=event.range;if(!range)range=mozile.dom.selection.get().getRangeAt(0);if(range.startContainer.nodeType==mozile.dom.TEXT_NODE){if(range.startContainer.data.charAt(range.startOffset-1)==" "){return false;}}}
if(event.charCode&&event.charCode>=32){if(mozile.browser.isSafari&&event.charCode>=63232&&event.charCode<=63235)return false;return true;}
return false;}
else{if(typeof(content)!="string")return false;}
return true;}
mozile.edit.insertText.prepare=function(event,direction,content,node){var state=new mozile.edit.State(this);state.direction=mozile.edit.NEXT;if(direction)state.direction=direction;state.content=" ";if(content)state.content=content;else if(event)state.content=String.fromCharCode(event.charCode);state.collapse=false;if(event)state.collapse=true;state.node=state.storeNode(node);state.remove=false;var selection=null;if(event&&event.selection)selection=event.selection;else selection=mozile.dom.selection.get();if(mozile.alternateSpace&&!state.node&&state.content==" "){var range=selection.getRangeAt(0);var alt=mozile.alternateSpace;var text=range.startContainer;var offset=range.startOffset;var nextChar=null;if(range.endContainer.nodeType==mozile.dom.TEXT_NODE)
nextChar=range.endContainer.data.charAt(range.endOffset);var previousChar=null;var previousAlt=false;if(text.nodeType==mozile.dom.TEXT_NODE){previousChar=text.data.charAt(offset-1);var data=text.data.substring(0,offset);if(offset&&data.lastIndexOf(alt)+alt.length==offset){previousAlt=true;previousChar=text.data.charAt(offset-alt.length-1);}}
if(previousAlt){if(previousChar&&previousChar!=" "){state.remove=true;state.content=" "+alt;}
else if(!nextChar||nextChar==" ")state.content=alt;}
else if(!nextChar||nextChar==" ")state.content=alt;else if(!previousChar||previousChar==" ")state.content=alt;}
return state;}
mozile.edit.insertText.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);state.emptyToken=false;state.actions=new Array();if(!state.node&&!selection.isCollapsed){if(mozile.edit.remove)mozile.edit.remove.request(state,fresh,state.direction,null,true);else mozile.edit.removeText.request(state,fresh,state.direction);}
if(state.node){var node=mozile.xpath.getNode(state.node);state.changedNode=node;state.oldData=node.data;node.data=state.content;if(state.direction==mozile.edit.NEXT){selection.collapse(node,0);selection.extend(node,node.data.length);}
else{selection.collapse(node,node.data.length);selection.extend(node,0);}
state.selection.after=selection.store();}
else if(mozile.edit.isEmptyToken(selection.focusNode)){state.emptyToken=true;selection.focusNode.data=state.content;if(!state.collapse&&state.content.length>0){selection.collapse(selection.focusNode,0);selection.extend(selection.focusNode,state.content.length);}
else selection.collapse(selection.focusNode,state.content.length);state.selection.after=selection.store();}
else if(selection.focusNode.nodeType!=mozile.dom.TEXT_NODE){state.newNode=document.createTextNode(state.content);if(selection.focusOffset==0)mozile.dom.prependChild(state.newNode,selection.focusNode);else selection.focusNode.insertBefore(state.newNode,selection.focusNode.childNodes[selection.focusOffset]);if(!state.collapse&&state.newNode.data.length>0){selection.collapse(state.newNode,0);selection.extend(state.newNode,state.newNode.data.length);}
else selection.collapse(state.newNode,state.newNode.data.length);state.selection.after=selection.store();}
else{var text=selection.focusNode;var offset=selection.focusOffset;if(state.remove){mozile.edit.removeText.request(state,fresh,-1*state.direction,mozile.alternateSpace);offset-=mozile.alternateSpace.length;}
text.insertData(offset,state.content);var newOffset=offset+(state.direction*state.content.length);if(!state.collapse&&offset!=newOffset){selection.collapse(text,offset);selection.extend(text,newOffset);}
else selection.collapse(text,newOffset);if(state.actions.length==0)
state.selection.after=selection.store(state.selection.before,newOffset);else state.selection.after=selection.store();}
state.executed=true;return state;}
mozile.edit.insertText.unexecute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.after);if(state.changedNode)state.changedNode.data=state.oldData;else if(state.emptyToken)selection.focusNode.data=mozile.emptyToken;else if(state.newNode)state.newNode.parentNode.removeChild(state.newNode);else selection.focusNode.deleteData(selection.focusOffset-state.content.length,state.content.length);for(var i=state.actions.length-1;i>=0;i--){state.actions[i]=state.actions[i].command.unexecute(state.actions[i],fresh);if(state.actions[i].executed)throw("Error: mozile.edit.inertText.unexecute Child command unexecute failed at action "+i+".");}
selection.restore(state.selection.before);state.executed=false;return state;}
mozile.edit.removeText=new mozile.edit.Command("RemoveText");mozile.edit.removeText.priority=10;mozile.edit.removeText.makesChanges="text";mozile.edit.removeText.watchesChanges="none";mozile.edit.removeText.test=function(event,direction,content){var dir;if(event){if(mozile.edit.checkAccelerator(event,"Backspace"))
dir=mozile.edit.PREVIOUS;else if(mozile.edit.checkAccelerator(event,"Delete"))
dir=mozile.edit.NEXT;if(!dir)return false;}
if(!dir)dir=mozile.edit.PREVIOUS;if(direction==mozile.edit.NEXT)dir=direction;if(!event)event={type:"fake"};var node=mozile.edit._getNode(event);if(!node||node.nodeType!=mozile.dom.TEXT_NODE)return false;if(event.type!="fake"){var selection=event.selection;if(selection.isCollapsed){var IP=selection.getInsertionPoint(true);if(!IP)return false;if(!IP.seek(dir))return false;if(!IP||IP.getNode()!=node)return false;}
return true;}
else return true;}
mozile.edit.removeText.prepare=function(event,direction,content){var state=new mozile.edit.State(this);state.direction=mozile.edit.PREVIOUS;if(direction)state.direction=direction;else if(event&&mozile.edit.convertKeyCode(event.keyCode)=="Delete")
state.direction=mozile.edit.NEXT;state.content=null;if(content)state.content=content;return state;}
mozile.edit.removeText.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);if(!state.direction)state.direction=mozile.edit.PREVIOUS;if(selection.isCollapsed){var firstOffset=selection.focusOffset;var secondOffset=selection.focusOffset;if(!state.content){var IP=selection.getInsertionPoint();IP.seek(state.direction);if(state.direction==mozile.edit.PREVIOUS)firstOffset=IP.getOffset();else secondOffset=IP.getOffset();state.content=selection.focusNode.data.substring(firstOffset,secondOffset);}
else{if(state.direction==mozile.edit.PREVIOUS)
firstOffset-=state.content.length;else secondOffset+=state.content.length;}
if(firstOffset<0)firstOffset=0;if(firstOffset+state.content.length<=selection.focusNode.data.length){selection.focusNode.deleteData(firstOffset,state.content.length);selection.collapse(selection.focusNode,firstOffset);state.selection.after=selection.store(state.selection.before,firstOffset);}
else mozile.debug.debug("mozile.edit.removeText.execute","Content length too great. firstOffset="+firstOffset+" content='"+state.content+"' data='"+selection.focusNode.data+"'");}
else{var range=selection.getRangeAt(0);if(mozile.browser.isIE&&range.startContainer!=range.endContainer){if(range.endOffset==0)range.setEnd(range.startContainer,range.startContainer.data.length);else range.setStart(range.endContainer,0);}
state.content=range.startContainer.data.substring(range.startOffset,range.endOffset);range.startContainer.deleteData(range.startOffset,range.endOffset-range.startOffset);selection.collapse(range.startContainer,range.startOffset);state.selection.after=selection.store(state.selection.before,range.startOffset);}
state.executed=true;return state;}
mozile.edit.removeText.unexecute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.after);if(!selection||!selection.focusNode)throw("Error: mozile.edit.removeText.unexecute no selection.focusNode");selection.focusNode.insertData(selection.focusOffset,state.content);selection.restore(state.selection.before);state.executed=false;return state;}
mozile.edit.isBlock=function(node){if(!node)return false;if(node.nodeType!=mozile.dom.ELEMENT_NODE)return false;var display=mozile.dom.getStyle(node,"display");switch(display){case"block":case"list-item":return true;}
return false;}
mozile.edit.getParentBlock=function(node){while(node){if(mozile.edit.isBlock(node))return node;else node=node.parentNode;}
return null;}
mozile.edit.mayContainText=function(node){if(node&&node.nodeType==mozile.dom.TEXT_NODE)node=node.parentNode;if(node&&node.nodeType==mozile.dom.ELEMENT_NODE){var rng=mozile.edit.lookupRNG(node);if(rng)return rng.mayContain("text");else{if(mozile.edit.getMark(node,"mayContainText")==true)return true;mozile.debug.debug("mozile.edit.mayContainText","No RNG Element for element named '"+node.nodeName+"'.");for(var i=0;i<node.childNodes.length;i++){if(mozile.edit.isTextEditable(node.childNodes[i]))
return mozile.edit.setMark(node,"mayContainText",true);}
return false;}}
return false;}
mozile.edit.isTextEditable=function(node){if(node.nodeType!=mozile.dom.TEXT_NODE)return false;if(!node.data)return false;if(mozile.edit.isEmptyToken(node))return true;if(mozile.dom.isWhitespace(node))return false;return true;}
mozile.edit.isChildless=function(node){if(node.nodeType==mozile.dom.COMMENT_NODE)return true;if(node.nodeType!=mozile.dom.ELEMENT_NODE)return false;var rng=mozile.edit.lookupRNG(node);if(rng){if(rng.mayContain("element"))return false;else return true;}
else return false;}
mozile.edit.createEmptyToken=function(){return document.createTextNode(mozile.emptyToken);}
mozile.edit.isEmptyToken=function(node){if(node&&node.nodeType==mozile.dom.TEXT_NODE&&node.data==mozile.emptyToken)return true;else return false;}
mozile.edit.containsEmptyToken=function(node,offset){if(!node||node.nodeType!=mozile.dom.TEXT_NODE)return false;if(offset==undefined||Number(offset)){if(node.data.indexOf(mozile.emptyToken)>-1)return true;else return false;}
else{var data=node.data.substring(offset);if(data.indexOf(mozile.emptyToken)==0)return true;else return false;}}
mozile.edit.isEmpty=function(node){switch(node.nodeType){case mozile.dom.TEXT_NODE:if(node.data.match(/\S/))return false;if(mozile.edit.isEmptyToken(node))return false;return true;case mozile.dom.ELEMENT_NODE:var children=node.childNodes;var i=0;for(i=0;i<children.length;i++){if(children[i].nodeType==mozile.dom.TEXT_NODE&&!mozile.edit.isEmpty(children[i]))
return false;}
for(i=0;i<children.length;i++){if(children[i].nodeType==mozile.dom.ELEMENT_NODE&&!mozile.edit.isEmpty(children[i]))
return false;}
return true;default:return true;}}
mozile.edit._getElementName=function(command){var elementName;if(typeof(command.element)=="string")elementName=command.element;else if(command.element&&command.element.cloneNode)
elementName=mozile.dom.getLocalName(command.element);elementName=elementName.toLowerCase();return elementName;}
mozile.edit._getNode=function(event){var node;if(event&&event.node)node=event.node;if(!node){var selection;if(event&&event.selection)selection=event.selection;if(!selection)selection=mozile.dom.selection.get();if(!selection)return false;var range;if(event&&event.range)range=event.range;if(!range)range=selection.getRangeAt(0);if(!range)return false;node=range.commonAncestorContainer;if(event){event.selection=selection;event.range=range;event.node=node;}}
if(!node)return null;else return node;}
mozile.edit._getTarget=function(event,target,direction,allowInvisible){if(!direction)direction="ancestor";if(direction!="ancestor"&&direction!="descendant"&&direction!="next"&&direction!="previous"){mozile.debug.debug("mozile.edit._getTarget","Invalid direction '"+direction+"'.");return null;}
if(event&&!event.targetCache)event.targetCache=new Object();var cacheKey=target.replace(" ","_")+"__"+direction;if(event&&event.targetCache[cacheKey])return event.targetCache[cacheKey];var node=mozile.edit._getNode(event);if(!node)return null;var test,result;if(typeof(target)=="function"){result=target(event,null);}
else if(typeof(target)=="string"){if(target.toLowerCase()=="any"){test=function(node){if(node)return true;else return false;}}
else if(target.toLowerCase()=="text"){test=function(node){if(node.nodeType==mozile.dom.TEXT_NODE)return true;else return false;}}
else if(target.toLowerCase()=="element"){test=function(node){if(node.nodeType==mozile.dom.ELEMENT_NODE)return true;else return false;}}
else if(target.toLowerCase()=="block"){test=function(node){if(mozile.edit.isBlock(node))return true;else return false;}}
else if(target.toLowerCase().indexOf("localname")==0){var name=target.substring(10);name=name.toLowerCase();test=function(node){var localName=mozile.dom.getLocalName(node);if(localName&&localName.toLowerCase()==name)return true;else return false;}}
else return null;}
else return null;var treeWalker;if(test&&!result){if(direction!="ancestor"&&!treeWalker){var root=document.documentElement;if(direction=="descendant"){root=node;if(root.nodeType!=mozile.dom.ELEMENT_NODE)root=root.parentNode;direction="next";}
treeWalker=document.createTreeWalker(root,mozile.dom.NodeFilter.SHOW_ALL,null,false);treeWalker.currentNode=node;}
var startNode=node;while(node){if(direction=="next")node=treeWalker.nextNode();else if(direction=="previous"){node=treeWalker.previousNode();if(mozile.dom.isAncestorOf(node,startNode))continue;}
if(node&&test(node)&&mozile.edit.isEditable(node)&&(allowInvisible||mozile.dom.isVisible(node))&&mozile.edit.getInsertionPoint(node,mozile.edit.NEXT)){result=node;break;}
if(direction=="ancestor")node=node.parentNode;}}
if(result){if(event)event.targetCache[cacheKey]=result;return result;}
else return null;}
mozile.enableEditing(false);mozile.edit.InsertionPoint=function(node,offset){this._node=node;this._offset=offset;}
mozile.edit.InsertionPoint.prototype._matchLeadingWS=/^(\s*)/;mozile.edit.InsertionPoint.prototype._matchTrailingWS=/(\s*)$/;mozile.edit.InsertionPoint.prototype._matchNonWS=/\S/;mozile.edit.InsertionPoint.prototype.getNode=function(){return this._node;}
mozile.edit.InsertionPoint.prototype.getOffset=function(){if(this._offset<0)this._offset=0;return this._offset;}
mozile.edit.InsertionPoint.prototype.toString=function(){return"Insertion Point: "+mozile.xpath.getXPath(this._node)+" "+this._offset;}
mozile.edit.InsertionPoint.prototype.select=function(){try{var selection=mozile.dom.selection.get();selection.collapse(this.getNode(),this.getOffset());}catch(e){mozile.debug.debug("mozile.edit.InsertionPoint.prototype.select","Bad collapse for IP "+mozile.xpath.getXPath(this.getNode())+" "+this.getOffset()+"\n"+mozile.dumpError(e));}}
mozile.edit.InsertionPoint.prototype.extend=function(){try{var selection=mozile.dom.selection.get();selection.extend(this.getNode(),this.getOffset());}catch(e){mozile.debug.debug("mozile.edit.InsertionPoint.prototype.extend","Bad extend for IP "+mozile.xpath.getXPath(this.getNode())+" "+this.getOffset()+"\n"+mozile.dumpError(e));}}
mozile.edit.InsertionPoint.prototype.next=function(){this.seek(mozile.edit.NEXT);}
mozile.edit.InsertionPoint.prototype.previous=function(){this.seek(mozile.edit.PREVIOUS);}
mozile.edit.InsertionPoint.prototype.seek=function(direction,container){var node=this.getNode();var offset=this.getOffset();if(!node||typeof(offset)=="undefined")return false;if(node.nodeType!=mozile.dom.TEXT_NODE||!mozile.edit.mayContainText(node)||(direction==mozile.edit.PREVIOUS&&offset==0)||(direction==mozile.edit.NEXT&&offset==node.data.length)||(direction==mozile.edit.NEXT&&mozile.edit.isEmptyToken(node))){return this.seekNode(direction,container);}
else offset=offset+direction;if(!node||typeof(offset)=="undefined")return false;if(mozile.edit.isEmptyToken(node)){this._offset=0;return true;}
var content=node.data;var substring,result,altSpaceIndex;if(direction==mozile.edit.NEXT){substring=content.substring(this.getOffset());result=substring.match(this._matchLeadingWS);if(mozile.alternateSpace)
altSpaceIndex=substring.indexOf(mozile.alternateSpace);}
else{substring=content.substring(0,this.getOffset());result=substring.match(this._matchTrailingWS);if(mozile.alternateSpace){altSpaceIndex=substring.length;altSpaceIndex-=substring.lastIndexOf(mozile.alternateSpace)+1;}}
var wsLength=result[0].length;if(Number(altSpaceIndex)!=NaN&&altSpaceIndex>-1&&altSpaceIndex<wsLength){wsLength=altSpaceIndex;}
var moveBy=0;if(wsLength<2)moveBy=direction;else if(mozile.dom.getStyle(node.parentNode,"white-space").toLowerCase()=="pre")moveBy=direction;else if(wsLength<substring.length)moveBy=wsLength*direction;else if(wsLength==substring.length)
return this.seekNode(direction,container);else throw Error("Unhandled case in InsertionPoint.seek()");this._node=node;this._offset=this.getOffset()+moveBy;return true;}
mozile.edit.InsertionPoint.prototype.seekNode=function(direction,extraStep,container){if(extraStep!==false)extraStep=true;var node=this.getNode();if(!node)return false;var offset=this.getOffset();if(direction==mozile.edit.NEXT&&offset>0)offset--;if(node.nodeType==mozile.dom.ELEMENT_NODE&&node.childNodes[offset])
node=node.childNodes[offset];if(!container)mozile.edit.getContainer(node);if(!container)container=document.documentElement;var treeWalker=document.createTreeWalker(container,mozile.dom.NodeFilter.SHOW_ALL,null,false);treeWalker.currentNode=node;var startNode=node;var IP,tempNode,lastNode,nextNode;while(node){lastNode=node;if(direction==mozile.edit.NEXT)node=treeWalker.nextNode();else{tempNode=node;node=treeWalker.previousNode();while(node&&node.firstChild==tempNode){tempNode=node;node=treeWalker.previousNode();}}
if(!node)break;IP=mozile.edit.getInsertionPoint(node,direction);if(IP){this._node=IP.getNode();this._offset=IP.getOffset();if(mozile.edit.isEmptyToken(this._node))this._offset=0;else if(extraStep&&mozile.edit.mayContainText(lastNode)&&mozile.edit.getParentBlock(node)==mozile.edit.getParentBlock(startNode))
this._offset=this._offset+direction;else if(extraStep&&lastNode.nodeType==mozile.dom.COMMENT_NODE&&node==IP.getNode())
this._offset=this._offset+direction;return true;}
nextNode=node;while(nextNode){if(direction==mozile.edit.NEXT)nextNode=nextNode.nextSibling;else nextNode=nextNode.previousSibling;if(nextNode&&nextNode.nodeType==mozile.dom.COMMENT_NODE)continue;else break;}
if(nextNode){IP=mozile.edit.getInsertionPoint(nextNode,direction);if(IP)continue;}
if(node.nodeType==mozile.dom.ELEMENT_NODE&&mozile.edit.mayContainText(node.parentNode)){this._node=node.parentNode;this._offset=mozile.dom.getIndex(node);if(direction==mozile.edit.NEXT&&this._offset<this._node.childNodes.length)
this._offset++;while(this._node.childNodes[this._offset-1]&&this._node.childNodes[this._offset-1].nodeType==mozile.dom.COMMENT_NODE)
this._offset--;return true;}}
return false;}
mozile.dom.Selection.prototype.getInsertionPoint=function(force){if(!this.focusNode||this.focusOffset==null)return null;else return new mozile.edit.InsertionPoint(this.focusNode,this.focusOffset,force);}
if(mozile.dom.InternetExplorerSelection){mozile.dom.InternetExplorerSelection.prototype.getInsertionPoint=mozile.dom.Selection.prototype.getInsertionPoint;}
mozile.edit.getInsertionPoint=function(node,direction,force){if(!node)return false;var offset,IP;if(mozile.edit.mayContainText(node)||force){if(node.nodeType==mozile.dom.TEXT_NODE){if(direction==mozile.edit.NEXT)offset=0;else if(mozile.edit.isEmptyToken(node))offset=0;else offset=node.data.length;return new mozile.edit.InsertionPoint(node,offset);}
if(direction==mozile.edit.NEXT)IP=mozile.edit.getInsertionPoint(node.firstChild,direction);else IP=mozile.edit.getInsertionPoint(node.lastChild,direction);if(IP)return IP;if(direction==mozile.edit.NEXT)offset=0;else offset=node.childNodes.length;return new mozile.edit.InsertionPoint(node,offset);}
return null;}
mozile.edit.rich=true;mozile.edit.insertNode=new mozile.edit.Command("InsertNode");mozile.edit.insertNode.test=function(event,parentNode,previousSibling,content){if(event){return false;}
if(!parentNode&&!previousSibling)return false;if(!content)return false;return true;}
mozile.edit.insertNode.prepare=function(event,parentNode,previousSibling,content){var state=new mozile.edit.State(this,false);state.location={parentNode:null,previousSibling:null};state.location.parentNode=state.storeNode(parentNode);state.location.previousSibling=state.storeNode(previousSibling);state.content=null;if(content)state.content=content;return state;}
mozile.edit.insertNode.execute=function(state,fresh){if(!state.content)throw("Error [mozile.edit.insertNode.execute]: No content provided.");var location={previousSibling:null,parentNode:null};if(state.location.previousSibling){location.previousSibling=mozile.xpath.getNode(state.location.previousSibling);}
else if(state.location.parentNode){location.parentNode=mozile.xpath.getNode(state.location.parentNode);}
else throw("Error [mozile.edit.insertNode.execute]: No previous sibling or parentNode provided.");if(location.previousSibling)mozile.dom.insertAfter(state.content,location.previousSibling);else if(location.parentNode)mozile.dom.prependChild(state.content,location.parentNode);state.executed=true;return state;}
mozile.edit.insertNode.unexecute=function(state,fresh){if(state.content.parentNode){state.content.parentNode.removeChild(state.content);}
else mozile.debug.debug("mozile.edit.insertNode.unexecute","No parent for state.content "+state.content);state.executed=false;return state;}
mozile.edit.removeNode=new mozile.edit.Command("RemoveNode");mozile.edit.removeNode.test=function(event,content){if(event){return false;}
if(!content)return false;if(!content.parentNode)return false;return true;}
mozile.edit.removeNode.prepare=function(event,content){var state=new mozile.edit.State(this,false);state.content=null;if(content)state.content=content;else if(event)state.content=mozile.edit._getNode(event);return state;}
mozile.edit.removeNode.execute=function(state,fresh){var target=state.content;var parentNode=target.parentNode;if(!parentNode)throw("Error [mozile.edit.removeNode.execute]: No parent node for node '"+target+"'.");var previousSibling=target.previousSibling;if(previousSibling&&!state.previousSibling)
state.previousSibling=mozile.xpath.getXPath(previousSibling);else if(!state.parentNode)
state.parentNode=mozile.xpath.getXPath(parentNode);parentNode.removeChild(target);state.executed=true;return state;}
mozile.edit.removeNode.unexecute=function(state,fresh){if(state.previousSibling){var previousSibling=mozile.xpath.getNode(state.previousSibling);if(!previousSibling)throw("Error [mozile.edit.removeNode.unexecute]: Could not find previousSibling '"+state.previousSibling+"'.");mozile.dom.insertAfter(state.content,previousSibling);}
else if(state.parentNode){var parentNode=mozile.xpath.getNode(state.parentNode);mozile.dom.prependChild(state.content,parentNode);}
else mozile.debug.inform("mozile.edit.removeNode.unexecute","No parent or previousSibling.");state.executed=false;return state;}
mozile.edit.remove=new mozile.edit.Command("Remove");mozile.edit.remove.test=function(event,direction,content,preserve){if(event){if(!mozile.edit.checkAccelerators(event,["Backspace","Delete"]))
return false;}
var selection;if(event&&event.selection)selection=event.selection;else selection=mozile.dom.selection.get();if(selection.isCollapsed){var dir=mozile.edit.PREVIOUS;if(direction==mozile.edit.NEXT)dir=direction;var IP=selection.getInsertionPoint(true);if(!IP)return false;return IP.seek(dir);}
return true;}
mozile.edit.remove.prepare=function(event,direction,content,preserve){var state=new mozile.edit.State(this);state.direction=mozile.edit.PREVIOUS;if(direction)state.direction=direction;else if(event&&mozile.edit.convertKeyCode(event.keyCode)=="Delete")
state.direction=mozile.edit.NEXT;state.content=" ";if(content)state.content=content;state.preserve=false;if(preserve===true)state.preserve=true;return state;}
mozile.edit.remove.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);state.actions=new Array();var node,newState,IP;if(selection.isCollapsed){node=selection.focusNode;IP=selection.getInsertionPoint(true);if(IP)IP.seek(state.direction);if(node.nodeType==mozile.dom.TEXT_NODE&&IP&&IP.getNode()==node)
mozile.edit.removeText.request(state,fresh,state.direction);else if(IP)IP.extend();}
if(!selection.isCollapsed){node=selection.getRangeAt(0).commonAncestorContainer;if(node.nodeType==mozile.dom.TEXT_NODE)
mozile.edit.removeText.request(state,fresh,state.direction);else mozile.edit._removeRange(state,fresh,state.direction);}
if(state.preserve){mozile.edit._ensureNonEmpty(state,fresh,selection.focusNode);}
else{mozile.edit._removeEmpty(state,fresh,selection.focusNode,mozile.edit.getParentBlock(selection.focusNode));}
state.selection.after=selection.store();state.executed=true;return state;}
mozile.edit.moveNode=new mozile.edit.Command("MoveNode");mozile.edit.moveNode.test=function(event,destinationParentNode,destinationPreviousSibling,target){if(event){return false;}
if(!destinationParentNode&&!destinationPreviousSibling)return false;if(!target)return false;return true;}
mozile.edit.moveNode.prepare=function(event,destinationParentNode,destinationPreviousSibling,target){var state=new mozile.edit.State(this);state.destination={parentNode:null,previousSibling:null};state.destination.parentNode=state.storeNode(destinationParentNode);state.destination.previousSibling=state.storeNode(destinationPreviousSibling);state.target=state.storeNode(target);return state;}
mozile.edit.moveNode.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);var anchorNode=selection.anchorNode;var anchorOffset=selection.anchorOffset;var focusNode=selection.focusNode;var focusOffset=selection.focusOffset;var target=mozile.xpath.getNode(state.target);if(!target)throw("Error: mozile.edit.moveNode.execute No target node.");mozile.edit.removeNode.request(state,fresh,target);mozile.edit.insertNode.request(state,fresh,state.destination.parentNode,state.destination.previousSibling,target);selection.collapse(anchorNode,anchorOffset);if(focusNode!=anchorNode||focusOffset!=anchorOffset){selection.extend(focusNode,focusOffset);}
state.selection.after=selection.store();state.executed=true;return state;}
mozile.edit.mergeNodes=new mozile.edit.Command("MergeNodes");mozile.edit.mergeNodes.test=function(event,from,to){if(event){return false;}
if(!from)return false;if(!to)return false;return true;}
mozile.edit.mergeNodes.prepare=function(event,from,to){var state=new mozile.edit.State(this);state.from=state.storeNode(from);state.to=state.storeNode(to);return state;}
mozile.edit.mergeNodes.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);state.actions=new Array();var fromNode=mozile.xpath.getNode(state.from);var toNode=mozile.xpath.getNode(state.to);var anchorNode=selection.anchorNode;var anchorOffset=selection.anchorOffset;var focusNode=selection.focusNode;var focusOffset=selection.focusOffset;mozile.edit._removeEmptyTokens(state,fresh,toNode);mozile.edit._removeEmptyTokens(state,fresh,fromNode);var firstNode,secondNode;if(fromNode.nodeType==mozile.dom.TEXT_NODE&&toNode.nodeType==mozile.dom.TEXT_NODE){if(fromNode.nextSibling==toNode){firstNode=fromNode;secondNode=toNode;}
else if(toNode.nextSibling==fromNode){firstNode=toNode;secondNode=fromNode;}
if(firstNode&&secondNode){var offset=firstNode.data.length;if(anchorNode==secondNode){anchorNode=firstNode;anchorOffset+=offset;}
if(focusNode==secondNode){focusNode=firstNode;focusOffset+=offset;}
mozile.edit.insertText.request(state,fresh,null,firstNode.data+secondNode.data,firstNode);mozile.edit.removeNode.request(state,fresh,secondNode);selection.collapse(anchorNode,anchorOffset);if(focusNode!=anchorNode||focusOffset!=anchorOffset){selection.extend(focusNode,focusOffset);}
state.selection.after=selection.store();state.executed=true;return state;}
else throw("Error [mozile.edit.mergeNodes.execute]: Cannot merge text non-adjacent nodes: "+state.from+" "+state.to);}
firstNode=toNode.lastChild;secondNode=fromNode.firstChild
while(fromNode.firstChild){mozile.edit.moveNode.request(state,fresh,toNode,toNode.lastChild,fromNode.firstChild);}
mozile.edit.removeNode.request(state,fresh,fromNode);var IP;if(mozile.dom.isAncestorOf(document.documentElement,anchorNode)){selection.collapse(anchorNode,anchorOffset);}
else{IP=mozile.edit.getInsertionPoint(toNode,mozile.edit.NEXT);if(IP)IP.select();}
if(mozile.dom.isAncestorOf(document.documentElement,focusNode)){if(focusNode!=selection.anchorNode||focusOffset!=selection.anchorOffset){selection.extend(focusNode,focusOffset);}}
else{IP=mozile.edit.getInsertionPoint(toNode,mozile.edit.PREVIOUS);if(IP)IP.extend();}
if(firstNode&&firstNode.nodeType==mozile.dom.TEXT_NODE&&secondNode&&secondNode.nodeType==mozile.dom.TEXT_NODE){this.request(state,fresh,firstNode,secondNode);}
state.selection.after=selection.store();state.executed=true;return state;}
mozile.edit.splitNode=new mozile.edit.Command("SplitNode");mozile.edit.splitNode.test=function(event,target,offset,after){if(event){return false;}
if(!target)return false;if(!target.nodeType)return false;if(target.nodeType!=mozile.dom.TEXT_NODE&&target.nodeType!=mozile.dom.ELEMENT_NODE)
return false;return true;}
mozile.edit.splitNode.prepare=function(event,target,offset,after){var state=new mozile.edit.State(this);target=state.storeNode(target);state.target=target;state.offset=null;if(offset)state.offset=offset;state.after=false;if(after===true)state.after=true;return state;}
mozile.edit.splitNode.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);state.actions=new Array();var target=mozile.xpath.getNode(state.target);var oldContainer,newContainer;var anchorNode=selection.anchorNode;var anchorOffset=selection.anchorOffset;var focusNode=selection.focusNode;var focusOffset=selection.focusOffset;if(target.nodeType==mozile.dom.TEXT_NODE&&state.offset!=undefined){state.splitNode=target;oldContainer=target;newContainer=target.splitText(state.offset);if(anchorNode==target&&anchorOffset>=state.offset){anchorNode=newContainer;anchorOffset-=state.offset;}
if(focusNode==target&&focusOffset>=state.offset){focusNode=newContainer;focusOffset-=state.offset;}}
else if(target.nodeType==mozile.dom.TEXT_NODE||target.nodeType==mozile.dom.ELEMENT_NODE){var i=0;if(state.after)i++;var node=target;while(node){i++;node=node.previousSibling;}
oldContainer=target.parentNode;newContainer=oldContainer.cloneNode(false);mozile.edit.insertNode.request(state,fresh,null,oldContainer,newContainer);var newContainerPath=mozile.xpath.getXPath(newContainer);while(oldContainer.childNodes.length>=i){mozile.edit.moveNode.request(state,fresh,newContainerPath,null,oldContainer.lastChild);}
if(mozile.edit.isBlock(oldContainer))
mozile.edit._ensureNonEmpty(state,fresh,oldContainer);if(mozile.edit.isBlock(newContainer))
mozile.edit._ensureNonEmpty(state,fresh,newContainer);}
selection.collapse(anchorNode,anchorOffset);if(focusNode!=selection.anchorNode||focusOffset!=selection.anchorOffset){selection.extend(focusNode,focusOffset);}
state.oldContainer=oldContainer;state.newContainer=newContainer;state.selection.after=selection.store();state.executed=true;return state;}
mozile.edit.splitNode.unexecute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.after);for(var i=state.actions.length-1;i>=0;i--){state.actions[i]=state.actions[i].command.unexecute(state.actions[i],fresh);if(state.actions[i].executed)throw("Error: mozile.edit.splitNode.unexecute Child command unexecute failed at action "+i+".");}
if(state.splitNode){state.splitNode.appendData(state.newContainer.data);if(state.newContainer.parentNode){state.newContainer.parentNode.removeChild(state.newContainer);}}
selection.restore(state.selection.before);state.executed=false;return state;}
mozile.edit.splitNodes=new mozile.edit.Command("SplitNodes");mozile.edit.splitNodes.test=function(event,target,offset,limitNode,shallow){if(event){return false;}
return true;}
mozile.edit.splitNodes.prepare=function(event,target,offset,limitNode,shallow){var state=new mozile.edit.State(this);if(!target){var selection=mozile.dom.selection.get();var range=selection.getRangeAt(0);if(range.startContainer.nodeType==mozile.dom.TEXT_NODE)
target=range.startContainer;else target=range.startContainer.childNodes[range.startOffset];}
state.target=state.storeNode(target);state.offset=null;if(offset!=undefined&&offset!=null)state.offset=offset;else if(event)state.offset="focusOffset";var limit=null;if(limitNode&&mozile.dom.isAncestorOf(limitNode,target)){limit=limitNode;}
else limit=mozile.edit.getParentBlock(target).parentNode;state.limitNode=state.storeNode(limit);state.shallow=false;if(shallow===true)state.shallow=true;return state;}
mozile.edit.splitNodes.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);state.actions=new Array();var newState;var node=mozile.xpath.getNode(state.target);var offset=state.offset;if(offset=="focusOffset")offset=selection.focusOffset;var limitNode=mozile.xpath.getNode(state.limitNode);var after=false;if(offset==null||offset==0){offset=null;while(node){if(node==limitNode)break;if(!node.parentNode)break;if(node.parentNode==limitNode)break;if(node!=node.parentNode.firstChild)break;if(state.shallow){if(!node.parentNode.parentNode)break;if(node.parentNode.parentNode==limitNode)break;}
node=node.parentNode;}}
else if(node.data&&offset==node.data.length){offset=null;if(node.nextSibling)node=node.nextSibling;while(node){if(node==limitNode)break;if(!node.parentNode)break;if(node.parentNode==limitNode)break;if(node!=node.parentNode.lastChild)break;if(!node.parentNode.nextSibling)break;if(state.shallow){if(!node.parentNode.parentNode)break;if(node.parentNode.parentNode==limitNode)break;}
node=node.parentNode.nextSibling;}
if(node==node.parentNode.lastChild)after=true;}
while(node){if(node==limitNode)break;if(offset==null&&node.parentNode==limitNode)break;if(!node.parentNode)break;newState=mozile.edit.splitNode.request(state,fresh,node,offset,after);if(newState&&newState.newContainer){node=newState.newContainer;offset=null;}}
if(newState){if(newState.oldContainer)state.oldContainer=newState.oldContainer;if(newState.newContainer)state.newContainer=newState.newContainer;}
else{state.oldContainer=node.previousSibling;state.newContainer=node;}
state.selection.after=selection.store();state.executed=true;return state;}
mozile.edit.Split=function(name){this.name=name;this.group=false;this.remove=true;this.makesChanges="node";this.watchesChanges="node";this.target="block";this.direction="ancestor";mozile.edit.allCommands[this.name]=this;}
mozile.edit.Split.prototype=new mozile.edit.Command;mozile.edit.Split.prototype.constructor=mozile.edit.Split;mozile.edit.Split.prototype.prepare=function(event){var state=new mozile.edit.State(this);var target=mozile.edit._getTarget(event,this.target,this.direction);state.limit=state.storeNode(target.parentNode);if(this.prompt){if(!this.prompt(event,state))return null;}
return state;}
mozile.edit.Split.prototype.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);state.actions=new Array();if(!selection.isCollapsed)mozile.edit.remove.request(state,fresh);var limit=mozile.xpath.getNode(state.limit);var newState=mozile.edit.splitNodes.request(state,fresh,selection.focusNode,selection.focusOffset,limit,true);var IP=mozile.edit.getInsertionPoint(newState.newContainer,mozile.edit.NEXT);if(IP)IP.select();state.newContainer=newState.newContainer;state.oldContainer=newState.oldContainer;state.selection.after=selection.store();state.executed=true;return state;}
mozile.edit.Insert=function(name){this.name=name;this.group=false;this.remove=true;this.makesChanges="node";this.watchesChanges="node";mozile.edit.allCommands[this.name]=this;}
mozile.edit.Insert.prototype=new mozile.edit.Command;mozile.edit.Insert.prototype.constructor=mozile.edit.Insert;mozile.edit.Insert.prototype.prepare=function(event){var state=new mozile.edit.State(this);state.element=null;if(typeof(this.element)=="string"){state.element=mozile.dom.createElement(this.element);if(this.className){mozile.dom.setClass(state.element,this.className);}
if(this.styleName){mozile.dom.setStyle(state.element,this.styleName,this.styleValue);}}
else if(this.element&&this.element.cloneNode){state.element=this.element.cloneNode(true);}
state.text=null;if(state.element==null&&this.text){state.text=this.text;}
if(this.prompt){if(!this.prompt(event,state))return null;}
return state;}
mozile.edit.Insert.prototype.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);state.actions=new Array();var newState;var previousNode;var text;if(this.remove&&!selection.isCollapsed){mozile.edit.remove.request(state,fresh,mozile.edit.NEXT);selection=mozile.dom.selection.get();}
if(state.text){mozile.edit.insertText.request(state,fresh,null,state.text);state.selection.after=selection.store();state.executed=true;return state;}
if(selection.isCollapsed){if(selection.focusNode.nodeType==mozile.dom.TEXT_NODE){newState=mozile.edit.splitNode.request(state,fresh,selection.focusNode,selection.focusOffset);previousNode=newState.oldContainer;}
else previousNode=selection.focusNode[selection.focusOffset];mozile.edit.insertNode.request(state,fresh,null,previousNode,state.element);if(!this.remove){if(mozile.edit.isBlock(state.element))
text=mozile.edit.createEmptyToken();else text=document.createTextNode("");state.element.appendChild(text);}}
else{var range=selection.getRangeAt(0);var container=range.commonAncestorContainer;if(container.nodeType==mozile.dom.TEXT_NODE)container=container.parentNode;var startContainer=range.startContainer;var startOffset=range.startOffset;newState=mozile.edit.splitNodes.request(state,fresh,range.endContainer,range.endOffset,container);var nextNode=newState.newContainer;newState=mozile.edit.splitNodes.request(state,fresh,startContainer,startOffset,container);previousNode=newState.oldContainer;mozile.edit.insertNode.request(state,fresh,null,previousNode,state.element);var current=state.element.nextSibling;while(current){if(current==nextNode)break;var target=current;current=current.nextSibling;mozile.edit.moveNode.request(state,fresh,state.element,state.element.lastChild,target);}}
if(text)selection.collapse(text,0);else{var IP=mozile.edit.getInsertionPoint(state.element,mozile.edit.NEXT);if(IP){if(this.remove){IP.seekNode(mozile.edit.NEXT,false);if(IP)IP.select();}
else{IP.select();IP=mozile.edit.getInsertionPoint(state.element,mozile.edit.PREVIOUS);if(IP)IP.extend();}}}
state.selection.after=selection.store();state.executed=true;return state;}
mozile.edit.Wrap=function(name){this.name=name;this.group=false;this.makesChanges="node";this.watchesChanges="node";this.nested=false;mozile.edit.allCommands[this.name]=this;}
mozile.edit.Wrap.prototype=new mozile.edit.Command;mozile.edit.Wrap.prototype.constructor=mozile.edit.Wrap;mozile.edit.Wrap.prototype._isWrapper=function(node){if(!node)return false;var targetName=mozile.dom.getLocalName(node);if(!targetName)return false;targetName=targetName.toLowerCase();var wrapperName=mozile.edit._getElementName(this);if(!wrapperName)return false;wrapperName=wrapperName.toLowerCase();if(targetName==wrapperName){if(this.className){if(mozile.dom.hasClass(node,this.className))return true;}
if(this.styleName){var styleName=mozile.dom.convertStyleName(this.styleName);if(node.style&&node.style[styleName]&&node.style[styleName]==this.styleValue)return true;}
else return true;}
return false;}
mozile.edit.Wrap.prototype._getWrapper=function(node,outerWrapper){if(!node)return false;var wrapper=null;while(node){if(this._isWrapper(node))wrapper=node;if(wrapper&&!outerWrapper)break;node=node.parentNode;}
return wrapper;}
mozile.edit.Wrap.prototype.isActive=function(event){if(this.prompt)return false;if(event&&event.node&&this._getWrapper(event.node))return true;else return false;}
mozile.edit.Wrap.prototype.prepare=function(event){var state=new mozile.edit.State(this);state.wrapper=null;if(typeof(this.element)=="string"){state.wrapper=mozile.dom.createElement(this.element);if(this.className){mozile.dom.setClass(state.wrapper,this.className);}
if(this.styleName){mozile.dom.setStyle(state.wrapper,this.styleName,this.styleValue);}}
else if(this.element&&this.element.cloneNode){state.wrapper=this.element.cloneNode(true);}
if(this.prompt){if(!this.prompt(event,state))return null;}
return state;}
mozile.edit.Wrap.prototype.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);var range=selection.getRangeAt(0);state.actions=new Array();var wrapper=state.wrapper;state.wrappers=new Array();var nextNode,previousNode,textNode,newState,IP;if(range.collapsed){var outerWrapper=this._getWrapper(range.commonAncestorContainer,true);if(outerWrapper&&!this.nested){if(selection.focusNode==outerWrapper.lastChild&&selection.focusOffset==selection.focusNode.data.length){IP=mozile.edit.getInsertionPoint(outerWrapper,mozile.edit.PREVIOUS,true);if(IP)IP.seekNode(mozile.edit.NEXT,false);if(IP)IP.select();mozile.edit._removeEmpty(state,fresh,outerWrapper.lastChild,outerWrapper.parentNode);}
else{newState=mozile.edit.splitNodes.request(state,fresh,range.startContainer,range.startOffset,outerWrapper.parentNode);previousNode=newState.oldContainer;textNode=document.createTextNode("");mozile.edit.insertNode.request(state,fresh,previousNode.parentNode,previousNode,textNode);selection.collapse(textNode,0);}}
else{if(range.startContainer.nodeType==mozile.dom.TEXT_NODE){if(range.startOffset<range.startContainer.data.length){newState=mozile.edit.splitNodes.request(state,fresh,range.startContainer,range.startOffset,range.startContainer.parentNode);previousNode=newState.oldContainer;}
else previousNode=range.startContainer;}
else if(range.startOffset>0){previousNode=range.startContainer.childNodes[range.startOffset-1];}
mozile.edit.insertNode.request(state,fresh,previousNode.parentNode,previousNode,wrapper);wrapper.appendChild(document.createTextNode(""));selection.collapse(wrapper.firstChild,0);}}
else{var container=range.commonAncestorContainer;var startWrapper,endWrapper;if(!this.nested){startWrapper=this._getWrapper(range.startContainer,true);endWrapper=this._getWrapper(range.endContainer,true);}
if(startWrapper&&endWrapper)
container=mozile.dom.getCommonAncestor(startWrapper,endWrapper);else if(startWrapper)
container=mozile.dom.getCommonAncestor(startWrapper,container);else if(endWrapper)
container=mozile.dom.getCommonAncestor(endWrapper,container);container=container.parentNode;var node,offset,startNode,endNode;node=range.endContainer;offset=range.endOffset;var endContainer=node.parentNode;if(endWrapper)endContainer=endWrapper.parentNode;newState=mozile.edit.splitNodes.request(state,fresh,node,offset,endContainer);endNode=newState.oldContainer;nextNode=newState.newContainer;node=range.startContainer;offset=range.startOffset;var startContainer=node.parentNode;if(startWrapper)startContainer=startWrapper.parentNode;newState=mozile.edit.splitNodes.request(state,fresh,node,offset,startContainer);previousNode=newState.oldContainer;startNode=newState.newContainer;if(endNode==node)endNode=startNode;var treeWalker=document.createTreeWalker(container,mozile.dom.NodeFilter.SHOW_ALL,null,false);var allNodesWrapped=false;if(!this.nested){allNodesWrapped=true;treeWalker.currentNode=startNode;var current=treeWalker.currentNode;var oldWrapper;while(current){if(current==nextNode)break;oldWrapper=this._getWrapper(current);if(oldWrapper){if(oldWrapper.nextSibling){current=oldWrapper.nextSibling;if(current==nextNode)current=null;else if(current.nodeType==mozile.dom.TEXT_NODE){allNodesWrapped=false;current=current.nextSibling;}}
mozile.edit._unwrapNode(state,fresh,oldWrapper);}
else{allNodesWrapped=false;current=treeWalker.nextNode();}}}
if(!allNodesWrapped||startNode==nextNode){if(previousNode){treeWalker.currentNode=previousNode;treeWalker.nextSibling();}
else if(startNode==nextNode){treeWalker.currentNode=startNode;nextNode=container;}
current=treeWalker.currentNode;var target,lastParent;while(current){if(current==nextNode)break;if(mozile.dom.isAncestorOf(wrapper,current,container))break;if(mozile.dom.isAncestorOf(current,wrapper,container))
current=treeWalker.nextNode();else if(mozile.dom.isAncestorOf(current,nextNode,container))
current=treeWalker.nextNode();else{target=current;current=treeWalker.nextSibling();if(!current)current=treeWalker.nextNode();if(target.parentNode&&target.parentNode!=lastParent){wrapper=state.wrapper.cloneNode(true);state.wrappers.push(wrapper);mozile.edit.insertNode.request(state,fresh,null,target,wrapper);lastParent=target.parentNode;}
mozile.edit.moveNode.request(state,fresh,wrapper,wrapper.lastChild,target);}}}
selection=mozile.dom.selection.get();range=selection.getRangeAt(0);container=range.commonAncestorContainer;if(container.nodeType!=mozile.dom.TEXT_NODE){IP=mozile.edit.getInsertionPoint(previousNode,mozile.edit.PREVIOUS,true);if(IP){IP.seekNode(mozile.edit.NEXT,false);IP.select();IP=mozile.edit.getInsertionPoint(nextNode,mozile.edit.NEXT,true);if(IP){IP.seekNode(mozile.edit.PREVIOUS,false);IP.extend();}}}
if(this._isWrapper(previousNode)&&this._isWrapper(previousNode.nextSibling)){mozile.edit.mergeNodes.request(state,fresh,previousNode.nextSibling,previousNode);}
if(this._isWrapper(nextNode)&&this._isWrapper(nextNode.previousSibling)){mozile.edit.mergeNodes.request(state,fresh,nextNode,nextNode.previousSibling);}}
state.selection.after=selection.store();state.executed=true;return state;}
mozile.edit.Unwrap=function(name){this.name=name;this.group=false;this.makesChanges="node";this.watchesChanges="node";this.target="element";this.direction="ancestor";mozile.edit.allCommands[this.name]=this;}
mozile.edit.Unwrap.prototype=new mozile.edit.Command;mozile.edit.Unwrap.prototype.constructor=mozile.edit.Unwrap;mozile.edit.Unwrap.prototype.isAvailable=function(event){var target=mozile.edit._getTarget(event,this.target,this.direction);if(target)return true;return false;}
mozile.edit.Unwrap.prototype.test=function(event,targetNode){if(event){if(this.accel){if(mozile.edit.checkAccelerators(event,this.accels)){}
if(mozile.edit.checkAccelerator(event,this.accel)){}
else return false;}
else return false;}
if(!this.target)return false;var node=targetNode;if(!node)node=mozile.edit._getTarget(event,this.target,this.direction);if(!node)return false;return true;}
mozile.edit.Unwrap.prototype.prepare=function(event,target){var state=new mozile.edit.State(this);if(!target)target=mozile.edit._getTarget(event,this.target,this.direction);state.target=state.storeNode(target);if(this.prompt){if(!this.prompt(event,state))return null;}
return state;}
mozile.edit.Unwrap.prototype.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);state.actions=new Array();var target=mozile.xpath.getNode(state.target);mozile.edit._unwrapNode(state,fresh,target);state.selection.after=selection.store();state.executed=true;return state;}
mozile.edit.Replace=function(name){this.name=name;this.group=false;this.makesChanges="node";this.watchesChanges="node";this.target="element";this.direction="ancestor";this.copyAttributes=true;mozile.edit.allCommands[this.name]=this;}
mozile.edit.Replace.prototype=new mozile.edit.Command;mozile.edit.Replace.prototype.constructor=mozile.edit.Replace;mozile.edit.Replace.prototype.isAvailable=function(event){var target=mozile.edit._getTarget(event,this.target,this.direction);if(target)return true;return false;}
mozile.edit.Replace.prototype.isActive=function(event){if(this.prompt)return false;if(!this.elementName)this.elementName=mozile.edit._getElementName(this);if(!this.elementName)return false;var target=mozile.edit._getTarget(event,this.target,this.direction);if(target){var targetName=mozile.dom.getLocalName(target).toLowerCase();if(targetName&&targetName==this.elementName){if(this.className){if(mozile.dom.hasClass(target,this.className))return true;else return false;}
return true;}}
return false;}
mozile.edit.Replace.prototype.test=function(event){if(event){if(this.accel){if(mozile.edit.checkAccelerators(event,this.accels)){}
if(mozile.edit.checkAccelerator(event,this.accel)){}
else return false;}
else return false;}
if(!this.element)return false;if(!this.target)return false;var node=mozile.edit._getTarget(event,this.target,this.direction);if(!node)return false;return true;}
mozile.edit.Replace.prototype.prepare=function(event){var state=new mozile.edit.State(this);state.element=null;if(typeof(this.element)=="string"){state.element=mozile.dom.createElement(this.element);if(this.className){mozile.dom.setClass(state.element,this.className);}
if(this.styleName){mozile.dom.setStyle(state.element,this.styleName,this.styleValue);}}
else if(this.element&&this.element.cloneNode){state.element=this.element.cloneNode(true);}
var target=mozile.edit._getTarget(event,this.target,this.direction);state.target=state.storeNode(target);if(this.copyAttributes){for(var i=0;i<target.attributes.length;i++){var attr=target.attributes[i];state.element.setAttribute(attr.nodeName,attr.nodeValue);}
if(target.className)state.element.className=target.className;if(target.mozile){state.element.mozile={};for(var key in target.mozile){state.element.mozile[key]=target.mozile[key];}}}
if(this.prompt){if(!this.prompt(event,state))return null;}
return state;}
mozile.edit.Replace.prototype.execute=function(state,fresh){var selection=mozile.dom.selection.get();if(!fresh)selection.restore(state.selection.before);var range=selection.getRangeAt(0);state.actions=new Array();var target=mozile.xpath.getNode(state.target);var focusNode=mozile.xpath.getXPath(selection.focusNode,state.target);var focusOffset=selection.focusOffset;mozile.edit.insertNode.request(state,fresh,null,target,state.element);while(target.firstChild){mozile.edit.moveNode.request(state,fresh,state.element,state.element.lastChild,target.firstChild);}
mozile.edit.removeNode.request(state,fresh,target);var newFocus=mozile.xpath.getNode(focusNode,state.element);if(newFocus)selection.collapse(newFocus,focusOffset);state.selection.after=selection.store();state.executed=true;return state;}
mozile.edit.Style=function(name){this.name=name;this.group=false;this.makesChanges="node";this.watchesChanges="node";this.target="element";this.direction="ancestor";this.styleName=null;this.styleValue=null;mozile.edit.allCommands[this.name]=this;}
mozile.edit.Style.prototype=new mozile.edit.Command;mozile.edit.Style.prototype.constructor=mozile.edit.Style;mozile.edit.Style.prototype.isAvailable=function(event){var target=mozile.edit._getTarget(event,this.target,this.direction);if(target)return true;return false;}
mozile.edit.Style.prototype.isActive=function(event){if(this.prompt)return false;var styleName=mozile.dom.convertStyleName(this.styleName);if(!styleName)return false;var target=mozile.edit._getTarget(event,this.target,this.direction);if(target&&target.style&&target.style[styleName]&&target.style[styleName]==this.styleValue)return true;return false;}
mozile.edit.Style.prototype.test=function(event){if(event){if(this.accel){if(mozile.edit.checkAccelerators(event,this.accels)){}
if(mozile.edit.checkAccelerator(event,this.accel)){}
else return false;}
else return false;}
if(!this.styleName)return false;if(!this.styleValue)return false;if(!this.target)return false;var node=mozile.edit._getTarget(event,this.target,this.direction);if(!node)return false;if(!node.style)return false;var state={targetNode:node};state.styleName=mozile.dom.convertStyleName(this.styleName);if(typeof(this.styleValue)=="function"){var result=this.styleValue(event,state);if(result===null)return false;}
return true;}
mozile.edit.Style.prototype.prepare=function(event){var state=new mozile.edit.State(this);var target=mozile.edit._getTarget(event,this.target,this.direction);state.targetNode=target;state.target=state.storeNode(target);state.styleName=mozile.dom.convertStyleName(this.styleName);state.styleValue=null;if(typeof(this.styleValue)=="function")
state.styleValue=this.styleValue(event,state);else if(typeof(this.styleValue)=="string")
state.styleValue=this.styleValue;state.oldValue=null;if(this.prompt){if(!this.prompt(event,state))return null;}
return state;}
mozile.edit.Style.prototype.execute=function(state,fresh){var target=mozile.xpath.getNode(state.target);state.oldValue=target.style[state.styleName];target.style[state.styleName]=state.styleValue;state.executed=true;return state;}
mozile.edit.Style.prototype.unexecute=function(state,fresh){var target=mozile.xpath.getNode(state.target);target.style[state.styleName]=state.oldValue;state.executed=false;return state;}
mozile.edit._mergeNodes=function(state,fresh,firstNode,secondNode){var firstBlock=mozile.edit.getParentBlock(firstNode);var secondBlock=mozile.edit.getParentBlock(secondNode);if(!firstBlock||!secondBlock)return false;if(firstBlock==secondBlock){return mozile.edit._normalize(state,fresh,firstNode,secondNode);}
var lastChild=firstBlock;var firstChild=secondBlock;var newState;while(lastChild.nodeType==mozile.dom.ELEMENT_NODE&&firstChild.nodeType==mozile.dom.ELEMENT_NODE&&lastChild.nodeName==firstChild.nodeName){var from=firstChild;var to=lastChild;lastChild=lastChild.lastChild;firstChild=firstChild.firstChild;mozile.edit.mergeNodes.request(state,fresh,from,to);}
mozile.edit._normalize(state,fresh,lastChild,firstChild);if(lastChild==firstBlock)return false;else return true;}
mozile.edit._normalize=function(state,fresh,firstNode,secondNode){if(!firstNode||!firstNode.parentNode||firstNode.nodeType!=mozile.dom.TEXT_NODE)
return false;if(!secondNode||!secondNode.parentNode||secondNode.nodeType!=mozile.dom.TEXT_NODE)
return false;if(firstNode.nextSibling!=secondNode)return false;return mozile.edit.mergeNodes.request(state,fresh,firstNode,secondNode);}
mozile.edit._removeEmpty=function(state,fresh,target,limitNode){var selection=mozile.dom.selection.get();if(!target||!target.parentNode)return null;var parent=target.parentNode;var parentBlock=mozile.edit.getParentBlock(target);if(typeof(state.direction)=="undefined")
state.direction=mozile.edit.PREVIOUS;var IP=mozile.edit.getInsertionPoint(target,-1*state.direction,true);if(!IP)return null;var result=IP.seekNode(-1*state.direction,false);if(!result)IP.seekNode(state.direction,false);if(!IP)return null;if((target.nodeType==mozile.dom.TEXT_NODE&&target.data.length==0)||(target.nodeType==mozile.dom.ELEMENT_NODE&&target!=mozile.edit.getContainer(target)&&target.childNodes.length==0)){if(target==limitNode){var content=mozile.edit.createEmptyToken();mozile.edit.insertNode.request(state,fresh,target,null,content);selection.collapse(content,0);return null;}
mozile.edit.removeNode.request(state,fresh,target);if(state.direction==mozile.edit.PREVIOUS){if(parentBlock==target||parentBlock==mozile.edit.getParentBlock(IP.getNode())){IP.select();}
else{IP=mozile.edit.getInsertionPoint(parentBlock,state.direction);if(IP)IP.select();else mozile.debug.debug("mozile.edit._removeEmpty","Nowhere to move the insertion point.");}}
else IP.select();if(IP){var firstNode,secondNode;if(state.direction==mozile.edit.PREVIOUS){secondNode=IP.getNode();IP.seekNode(state.direction,false);firstNode=IP.getNode();}
else{firstNode=IP.getNode();IP.seekNode(state.direction,false);secondNode=IP.getNode();}
result=mozile.edit._normalize(state,fresh,firstNode,secondNode);}}
else{return null;}
if(target==limitNode)return null;else return mozile.edit._removeEmpty(state,fresh,parent,limitNode);}
mozile.edit._removeEmptyTokens=function(state,fresh,target){var node=target.firstChild;while(node){if(mozile.edit.isEmptyToken(node)){var content=node;node=node.nextSibling;mozile.edit.removeNode.request(state,fresh,content);}
else node=node.nextSibling;}
return state;}
mozile.edit._ensureNonEmpty=function(state,fresh,target){if(!state||!target)return false;if(!target.nodeType)return false;if(!mozile.edit.isEmpty(target))return true;switch(target.nodeType){case mozile.dom.TEXT_NODE:mozile.edit.insertText.request(state,fresh,null,mozile.emptyToken,target);return true;case mozile.dom.ELEMENT_NODE:var rng=mozile.edit.lookupRNG(target);if((rng&&rng.mayContain("text"))||!rng){for(var i=0;i<target.childNodes.length;i++){if(target.childNodes[i].nodeType==mozile.dom.TEXT_NODE){return mozile.edit._ensureNonEmpty(state,fresh,target.childNodes[i]);}}
var emptyToken=mozile.edit.createEmptyToken();mozile.edit.insertNode.request(state,fresh,target,target.lastChild,emptyToken);return true;}
else if(target.firstChild){var child=target.firstChild;var result;while(child){result=mozile.edit._ensureNonEmpty(state,fresh,child);if(result)return true;child=child.nextSibling;}}
return false;default:return false;}}
mozile.edit._removeRange=function(state,fresh,direction){var selection=mozile.dom.selection.get();var range=selection.getRangeAt(0);var container=range.commonAncestorContainer;if(!direction)direction=mozile.edit.PREVIOUS;var startNode=range.startContainer;if(startNode.nodeType==mozile.dom.ELEMENT_NODE)
startNode=startNode.childNodes[range.startOffset];var endNode=range.endContainer;if(endNode.nodeType==mozile.dom.ELEMENT_NODE)
endNode=endNode.childNodes[range.endOffset];var treeWalker=document.createTreeWalker(container,mozile.dom.NodeFilter.SHOW_ALL,null,false);treeWalker.currentNode=startNode;var current=treeWalker.nextNode();while(current){if(!current.parentNode)break;if(current==endNode)break;if(mozile.dom.isAncestorOf(current,startNode,container))
current=treeWalker.nextNode();else if(mozile.dom.isAncestorOf(current,endNode,container))
current=treeWalker.nextNode();else{var target=current;current=treeWalker.nextSibling();if(!current)current=treeWalker.nextNode();mozile.edit.removeNode.request(state,fresh,target);}}
var data;if(startNode.nodeType==mozile.dom.TEXT_NODE){data=startNode.data.substring(0,range.startOffset);mozile.edit.insertText.request(state,fresh,null,data,startNode);}
else mozile.edit.removeNode.request(state,fresh,startNode);if(endNode.nodeType==mozile.dom.TEXT_NODE){data=endNode.data.substring(range.endOffset);mozile.edit.insertText.request(state,fresh,null,data,endNode);}
if(direction==mozile.edit.NEXT){if(endNode&&endNode.nodeType==mozile.dom.TEXT_NODE)
selection.collapse(endNode,0);else if(startNode&&startNode.nodeType==mozile.dom.TEXT_NODE)
selection.collapse(startNode,startNode.data.length);else mozile.debug.debug("mozile.edit._removeRange","Nowhere to collapse.");}
else{if(startNode&&startNode.nodeType==mozile.dom.TEXT_NODE)
selection.collapse(startNode,startNode.data.length);else if(endNode&&endNode.nodeType==mozile.dom.TEXT_NODE)
selection.collapse(endNode,0);else mozile.debug.debug("mozile.edit._removeRange","Nowhere to collapse.");}
if(startNode&&endNode&&startNode.parentNode&&endNode.parentNode){var result=mozile.edit._mergeNodes(state,fresh,startNode,endNode);if(!result&&direction==mozile.edit.NEXT&&mozile.edit.getParentBlock(startNode)!=mozile.edit.getParentBlock(endNode)){selection.collapse(endNode,0);}}
return state;}
mozile.edit._unwrapNode=function(state,fresh,target){var previousNode=target.previousSibling;var nextNode=target.nextSibling;var lastChild;while(target.lastChild){lastChild=target.lastChild;mozile.edit.moveNode.request(state,fresh,null,target,target.lastChild);}
mozile.edit.removeNode.request(state,fresh,target);mozile.edit._normalize(state,fresh,nextNode.previousSibling,nextNode);mozile.edit._normalize(state,fresh,previousNode,previousNode.nextSibling);return lastChild;}
mozile.enableEditing(true);mozile.event=new Object();mozile.event.prototype=new mozile.Module;mozile.event.mousedown=false;mozile.event.normalize=function(event){if(!event)return event;if(typeof(event.target)=="undefined")event.target=event.srcElement;if(typeof(event.charCode)=="undefined")event.charCode=event.keyCode;return event;}
mozile.event.addListener=function(doc,type,listener,useCapture){if(!listener)listener=mozile.event.handle;if(doc.addEventListener)doc.addEventListener(type,listener,useCapture);else if(doc.attachEvent)doc.attachEvent("on"+type,listener);else mozile.debug.inform("mozile.event.addListener","No known event method available");}
mozile.event.listen=function(){var events=["mousedown","mousemove","mouseup","click","dblclick","keydown","keyup","keypress"];for(var i=0;i<events.length;i++){mozile.event.addListener(document,events[i]);}}
mozile.event.dispatch=function(element,type,keyCode,charCode,ctrlKey,altKey,shiftKey,metaKey){if(element.fireEvent)element.fireEvent("on"+type);else if(element.dispatchEvent){var event;if(type.indexOf("click")>-1||type.indexOf("mouse")>-1){event=document.createEvent("MouseEvent");event.initMouseEvent(type,true,true,window,1,0,0,0,0,false,false,false,false,1,null);}
else if(type.indexOf("key")>-1){if(!ctrlKey)ctrlKey=false;if(!altKey)altKey=false;if(!shiftKey)shiftKey=false;if(!metaKey)metaKey=false;event=document.createEvent("KeyEvent");event.initKeyEvent(type,true,true,document.defaultView,ctrlKey,altKey,shiftKey,metaKey,keyCode,charCode);}
else{event=document.createEvent("Events");event.initEvent(type,true,true);}
element.dispatchEvent(event);}}
mozile.event.cancel=function(event){if(!event)return;if(event.stopPropagation)event.stopPropagation();event.cancelBubble=true;if(event.preventDefault)event.preventDefault();}
mozile.event.handle=function(event){try{switch(event.type){case"mousemove":return mozile.event.handleMouseMove(event);case"mouseup":case"click":case"dblclick":mozile.event.mousedown=false;}
event=mozile.event.normalize(event);mozile.event.findTarget(event);if(!event.node)return true;event.container=mozile.edit.getContainer(event.node);event.editable=Boolean(event.container);var state=mozile.edit.commands.trigger(event);if(state)return mozile.event.handled(event,state);if(mozile.gui)mozile.gui.update(event);if(!mozile.edit.editable)return true;var priorStatus=mozile.edit.status;mozile.edit.setStatus(event.editable);if(!event.editable)return true;mozile.event.storeSelection(event);if(!priorStatus)mozile.event.fixFocus(event);var node=event.node;while(event.node){if(!event.rng)event.rng=mozile.edit.lookupRNG(event.node);if(event.rng&&event.rng.commands){state=event.rng.commands.trigger(event);if(state)return mozile.event.handled(event,state);}
if(event.node==event.container)break;else event.node=event.node.parentNode;}
event.node=node;state=mozile.edit.defaults.trigger(event);if(state)return mozile.event.handled(event,state);if(!mozile.event.cancelKeyEvent(event))return false;if(!mozile.event.cancelHyperlink(event))return false;}catch(e){mozile.debug.inform("mozile.event.handle",mozile.dumpError(e));}
return true;}
mozile.notifyChange=function(target){var event=document.createEvent("Events");event.initEvent("change",true,true);target.dispatchEvent(event);}
mozile.event.handled=function(event,state){mozile.edit.done(state);if(state.changesMade){if(mozile.gui)mozile.gui.update(event,state.changesMade);mozile.notifyChange(event.node);}
if(state.cancel){mozile.event.cancel(event);return false;}
else return true;}
mozile.event.handleMouseMove=function(event){if(!mozile.browser.isMozilla)return true;if(!mozile.event.mousedown)return true;var selection=mozile.dom.selection.get();if(!selection)return true;if(selection.focusNode!=event.rangeParent||selection.focusOffset!=event.rangeOffset){selection.extend(event.rangeParent,event.rangeOffset);}
return true;}
mozile.event.findTarget=function(event){event.selection=mozile.dom.selection.get();if(!event.selection||event.selection.rangeCount<1)return;event.range=event.selection.getRangeAt(0);if(!event.range)return;event.node=event.range.commonAncestorContainer;}
mozile.event.storeSelection=function(event){mozile.dom.selection.last={anchorNode:event.selection.anchorNode,anchorOffset:event.selection.anchorOffset,focusNode:event.selection.focusNode,focusOffset:event.selection.focusOffset,isCollapsed:event.selection.isCollapsed};}
mozile.event.fixFocus=function(event){if(!mozile.browser.isMozilla)return;if(!mozile.useDesignMode)return;if(event.type!="mousedown")return;var newEvent=document.createEvent("MouseEvent");newEvent.initMouseEvent("mousedown",true,true,event.view,1,event.screenX,event.screenY,event.clientX,event.clientY,false,false,false,false,1,event.relatedTarget);event.target.dispatchEvent(newEvent);}
mozile.event.cancelKeyEvent=function(event){if(!event||!event.keyCode)return true;switch(event.keyCode){case 8:case 9:case 46:mozile.event.cancel(event);return false;}
return true;}
mozile.event.cancelHyperlink=function(event){if(!mozile.browser.isMozilla)return true;if(mozile.useDesignMode)return true;switch(event.type){case"mousedown":case"click":case"dblclick":case"mouseup":break;default:return true;}
var node=event.explicitOriginalTarget;var container=mozile.edit.getContainer(node);if(container){while(node){if(node.localName&&node.localName.toLowerCase()=="a"){if(event.selection&&event.rangeParent&&event.rangeOffset!=undefined){if(event.type=="mousedown"){event.selection.collapse(event.rangeParent,event.rangeOffset);mozile.event.mousedown=true;}
else mozile.event.mousedown=false;if(event.type=="dblclick"){mozile.event.selectWord(event.rangeParent,event.rangeOffset);}}
mozile.event.cancel(event);return false;}
if(node==container)break;node=node.parentNode;}}
return true;}
mozile.event.selectWord=function(node,offset){if(!node||offset==undefined)return;var selection=mozile.dom.selection.get();if(node.nodeType!=mozile.dom.TEXT_NODE){selection.collapse(node,offset);}
else{var match=/\s/;var data=node.data;if(offset==data.length)return;var startOffset=offset-2;while(startOffset>=0){if(data.charAt(startOffset).match(match)){startOffset++;break;}
else startOffset--;}
if(startOffset<0)startOffset=0;var endOffset=offset+1;while(endOffset<=data.length){if(data.charAt(endOffset).match(match))break;else endOffset++;}
if(endOffset>data.length)endOffset=data.length;selection.collapse(node,startOffset);selection.extend(node,endOffset);}}
mozile.event.listen();mozile.save=new Object();mozile.save.prototype=new mozile.Module;mozile.save.method=null;mozile.save.target=document;mozile.save.format=null;mozile.save.savedState=null;mozile.save.warn=true;window.onbeforeunload=function(){if(!mozile.save.warn)return undefined;if(mozile.save.isSaved())return undefined;return"There are unsaved changes in this document. Changes will be lost if you navigate away from this page.";}
mozile.save.isSaved=function(){if(!mozile.edit)return true;if(!mozile.edit.currentState)return true;if(mozile.edit.currentState!=mozile.save.savedState)return false;return true;}
mozile.save.save=function(){if(!mozile.save.method)return false;var content=mozile.save.getContent(mozile.save.target,mozile.save.format);var result=mozile.save.method.save(content);if(result&&mozile.edit)mozile.save.savedState=mozile.edit.currentState;return result;}
mozile.save.saveAs=function(){if(!mozile.save.method)return false;var content=mozile.save.getContent(mozile.save.target,mozile.save.format);var result=mozile.save.method.saveAs(content);if(result&&mozile.edit)mozile.save.savedState=mozile.edit.currentState;return result;}
mozile.save.getContent=function(target,format){var content="";if(!target)target=document;if(target.nodeType==9){target=target.documentElement;content+=mozile.save.getXMLDeclaration();content+=mozile.save.getDoctypeDeclaration();content+=mozile.save.getProcessingInstructions();}
target=target.cloneNode(true);target=mozile.save.cleanDOM(target);content+=mozile.xml.serialize(target);if(format){if(format.toLowerCase()=="uppercase")
content=mozile.save.toUpperCase(content);else if(format.toLowerCase()=="lowercase")
content=mozile.save.toLowerCase(content);}
content=mozile.save.cleanContent(content);return content;}
mozile.save.getXMLDeclaration=function(){var xmlDeclaration="";if(document.xmlVersion){xmlDeclaration='<?xml version="'+document.xmlVersion+'" encoding="'+document.xmlEncoding+'"?>\n'}
return xmlDeclaration;}
mozile.save.getDoctypeDeclaration=function(){var doctypeDeclaration="";if(document.doctype){doctypeDeclaration=mozile.xml.serialize(document.doctype)+"\n";}
return doctypeDeclaration;}
mozile.save.getProcessingInstructions=function(){var PIString="";if(window.XPathEvaluator){var evaluator=new XPathEvaluator();var PIList=evaluator.evaluate("/processing-instruction()",document,null,XPathResult.ANY_TYPE,null);var PI=PIList.iterateNext();while(PI){PIString+="<?"+PI.target+" "+PI.data+"?>\n";PI=PIList.iterateNext();}}
return PIString;}
mozile.save.cleanDOM=function(target){if(document.createTreeWalker&&mozile.dom.NodeFilter){var treeWalker=document.createTreeWalker(target,mozile.dom.NodeFilter.SHOW_ALL,null,false);treeWalker.currentNode=target;var current=treeWalker.currentNode;var remove=new Array();while(current){if(current.getAttribute&&current.getAttribute("class")=="mozileLink")remove.push(current);if(current.className&&current.className=="mozileLink")remove.push(current);if(current.getAttribute&&current.getAttribute("class")=="mozileGUI")remove.push(current);if(current.className&&current.className=="mozileGUI")remove.push(current);current=treeWalker.nextNode();}
while(remove.length){if(remove[0].parentNode)remove[0].parentNode.removeChild(remove[0]);remove.shift();}}
else mozile.debug.inform("mozile.save.cleanDOM","Could not clean target because no TreeWalker is available.");return target;}
mozile.save.cleanContent=function(content){return content;}
mozile.save.cleanMarkup=function(content){content=content.replace(/</g,"&lt;");content=content.replace(/>/g,"&gt;");return content;}
mozile.save._tagPattern=/<(\/*)(\w*)/g;;mozile.save.toUpperCase=function(content){return content.replace(mozile.save._tagPattern,function(word){return word.toUpperCase();});}
mozile.save.toLowerCase=function(content){return content.replace(mozile.save._tagPattern,function(word){return word.toLowerCase();});}
mozile.save.Method=function(name){this.name=name;}
mozile.save.Method.prototype.save=function(content){return false;}
mozile.save.Method.prototype.saveAs=function(content){return this.save(content);}
mozile.save.display=new mozile.save.Method("Display Source");mozile.save.display.save=function(content){content=mozile.save.cleanMarkup(content);if(mozile.gui){mozile.gui.display('<h3>Mozile Source</h3>\n<pre>'+content+'</pre>');}
else alert("Mozile Source\n\n"+content);return true;}
mozile.save.method=mozile.save.display;mozile.save.post=new mozile.save.Method("POST");mozile.save.post.async=true;mozile.save.post.showResponse=false;mozile.save.post.uri="";mozile.save.post.user=null;mozile.save.post.password=null;mozile.save.post.contentType="text/html";if(document.contentType)mozile.save.post.contentType=document.contentType;mozile.save.post.characterSet="UTF-8";if(document.characterSet)mozile.save.post.characterSet=document.characterSet;mozile.save.post.save=function(content){if(!this.uri){if(mozile.debug)mozile.debug.inform("mozile.save.post.save","No URI to save to.");return false;}
var CR='\x0D';var LF='\x0A';content=CR+LF+content+CR+LF;if(this.XHR)this.XHR.abort();this.XHR=null;var XHR;try{if(window.XMLHttpRequest){XHR=new XMLHttpRequest();}
else if(window.ActiveXObject){XHR=new ActiveXObject('Microsoft.XMLHTTP');}}catch(e){if(mozile.debug)mozile.debug.inform("mozile.save.post.save","File save failed for '"+this.uri+"' with error message:\n"+e);return false;}
if(XHR){XHR.open("POST",this.uri,this.async,this.user,this.password);XHR.setRequestHeader('Content-Type',this.contentType+"; "+this.characterSet);if(mozile.browser.mozile&&mozile.browser.mozileVersion<1.8)
XHR.setRequestHeader('Content-Length',content.length);XHR.setRequestHeader('Content-Location',this.uri);XHR.onreadystatechange=this.onreadystatechange;XHR.send(content);this.XHR=XHR;if(!this.async){this.onreadystatechange();}
return true;}
if(mozile.debug)mozile.debug.inform("mozile.save.post.save","No XMLHttpRequest available when trying to save to '"+this.uri+"'.");return false;}
mozile.save.post.onreadystatechange=function(){var XHR=mozile.save.post.XHR;if(!XHR)return;if(XHR.readyState!=4)return;if(XHR.status==0||XHR.status==200){if(mozile.save.post.showResponse)
mozile.gui.display('<h3>Save Operation Response</h3>\n\n'+XHR.responseText);}
else{if(mozile.save.post.showResponse)
mozile.gui.display('<h3>Save Operation Error</h3>\n\n'+XHR.responseText);else if(mozile.debug)mozile.debug.inform("mozile.save.post.save","File save failed with status '"+XHR.status+"' and message:\n"+XHR.responseText);}}
mozile.save.post.saveAs=function(content){var uri=prompt("Save to what URI?",this.uri);if(!uri)return false;this.uri=uri;return this.save(content);}
mozile.save.tidy=new Object();mozile.save.tidy.prototype=new mozile.Module;mozile.save.tidy.spaces=true;mozile.save.tidy.newline="\n";mozile.save.tidy.tab="  ";mozile.save.tidy.isXML=function(node){if(node.namespaceURI&&node.namespaceURI!=mozile.xml.ns.xhtml)
return true;else return false;}
mozile.save.tidy.countAncestors=function(node){var i=-1;while(node&&node!=document){i++;node=node.parentNode;}
return i;}
mozile.save.tidy.spaceBefore=function(node){var output="";if(!this.spaces)return output;switch(node.nodeName.toLowerCase()){case"html":return"";case"head":case"body":return this.newline;case"textarea":output=this.newline;for(var i=1;i<this.countAncestors(node);i++)output+=this.tab;break;}
if(node.parentNode&&node.parentNode.nodeName.toLowerCase()=="head"){return this.newline+this.tab;}
if(mozile.edit.isBlock(node)||mozile.save.tidy.isXML(node)){output=this.newline;for(var i=1;i<this.countAncestors(node);i++)output+=this.tab;}
return output;}
mozile.save.tidy.spaceBetween=function(node){var output="";if(!this.spaces)return output;if(mozile.edit.isBlock(node)||mozile.save.tidy.isXML(node))
return this.newline;switch(node.nodeName.toLowerCase()){case"textarea":output=this.newline;break;}
return output;}
mozile.save.tidy.spaceAfter=function(node){var output="";if(!this.spaces)return output;switch(node.nodeName.toLowerCase()){case"head":case"body":case"script":case"style":case"ul":case"ol":case"dl":output=this.spaceBefore(node);break;case"html":output=this.newline+output;}
if(mozile.save.tidy.isXML(node)&&mozile.dom.getFirstChildElement(node))
output=this.spaceBefore(node);return output;}
mozile.save.tidy.formatNode=function(node){var output="";var indent="";switch(node.nodeType){case mozile.dom.ELEMENT_NODE:var name=this.formatName(node.nodeName);if(node.nodeName.toLowerCase()=="script")return"";output=this.spaceBefore(node);output+="<"+name;var i;if(node.attributes.length){var attr=new Array();for(i=node.attributes.length-1;i>=0;i--){var a=node.attributes[i];if(!a.nodeValue)continue;if(!a.specified)continue;if(a.nodeName.toLowerCase()=="contenteditable"&&a.nodeValue=="inherit")continue;attr.push(this.formatNode(a));}
if(attr.length)output+=" "+attr.join(" ");}
if(node.childNodes.length){output+=">";var children=new Array();for(i=0;i<node.childNodes.length;i++){children.push(this.formatNode(node.childNodes[i]));}
output+=children.join("");output+=this.spaceAfter(node);output+="</"+name+">";}
else output+="/>";break;case mozile.dom.ATTRIBUTE_NODE:output=node.nodeName+'="'+node.nodeValue+'"';break;case mozile.dom.TEXT_NODE:if(node.data.match(/\w/))output=mozile.save.cleanMarkup(node.data);else{if(node!=node.parentNode.firstChild&&node!=node.parentNode.lastChild){output=this.spaceBetween(node.nextSibling);}}
break;case mozile.dom.CDATA_SECTION_NODE:if(node.parentNode&&node.parentNode.nodeName.toLowerCase()=="style")
output="/*<![CDATA[*/"+node.data+"/*]]>*/";else if(node.parentNode&&node.parentNode.nodeName.toLowerCase()=="script")
output="//<![CDATA[\n"+node.data+"\n//]]>";else output="<![CDATA["+node.data+"]]>";break;case mozile.dom.ENTITY_REFERENCE_NODE:output="UNHANDLED";break;case mozile.dom.ENTITY_NODE:output="UNHANDLED";break;case mozile.dom.PROCESSING_INSTRUCTION_NODE:output="<?"+node.target+" "+node.data+"?>";break;case mozile.dom.COMMENT_NODE:output="<!--"+node.data+"-->";break;case mozile.dom.DOCUMENT_NODE:output=this.formatNode(node.documentElement);break;case mozile.dom.DOCUMENT_TYPE_NODE:output=mozile.xml.serialize(node);break;case mozile.dom.DOCUMENT_FRAGMENT_NODE:output="";break;case mozile.dom.NOTATION_NODE:output="UNHANDLED";break;default:output="UNHANDLED";}
return output;}
mozile.save.tidy.formatName=function(name){if(!mozile.save||!mozile.save.format)return name;if(typeof(mozile.save.format)!="string")return name;if(mozile.save.format.toLowerCase()=="lowercase")return name.toLowerCase();if(mozile.save.format.toLowerCase()=="uppercase")return name.toUpperCase();return name;}
mozile.save.extract=new Object();mozile.save.extract.prototype=new mozile.Module;mozile.save.extract.extract=function(node,container){mozile.require("mozile.dom");if(!node)return null;var text,i;if(node.nodeType==mozile.dom.ELEMENT_NODE){var newContainer=container;var xml=node.getAttribute("xml");if(xml){var instructions=mozile.save.extract.parseInstructions(xml);newContainer=instructions[0].execute(node,container);for(i=1;i<instructions.length;i++){instructions[i].execute(node,newContainer);}
if(instructions[0].getType()=="Set Attribute")return container;}
for(i=0;i<node.childNodes.length;i++){mozile.save.extract.extract(node.childNodes[i],newContainer);}}
return container;}
mozile.save.extract.parseInstructions=function(string){var instructions=new Array();var instruction=new mozile.save.extract.Instruction;var mode="target";var c;for(var i=0;i<string.length;i++){c=string.charAt(i);if(c=="="){i++;c=string.charAt(i);if(c=="'"){mode="value";continue;}
else mode="select";}
if(c=="'"&&mode=="value"){mode="target";continue;}
else if(c.match(/\s/)&&mode!="value"){instructions.push(instruction);instruction=new mozile.save.extract.Instruction;mode="target";continue;}
instruction[mode]+=c;if(c=="\\"&&string.charAt(i+1)){i++;instruction[mode]+=string.charAt(i);}}
instructions.push(instruction);return instructions;}
mozile.save.extract.Instruction=function(){this.target="";this.select="";this.value="";}
mozile.save.extract.Instruction.prototype.toString=function(){return["Instruction",this.target,this.select,this.value].join(" :: ");}
mozile.save.extract.Instruction.prototype.getType=function(){if(!this.target)return null;if(this.value)return"Assign Value";if(this.select)return"Map Selection";if(this.target.charAt(0)=="@")return"Set Attribute";else return"Create Element";}
mozile.save.extract.Instruction.prototype.execute=function(element,container){try{var useContainer=container;var target=this.target;var attribute,text;var value=this.value;if(this.select&&this.select.charAt(0)=="@"){attribute=this.select.substring(1);value=element.getAttribute(attribute);if(!value&&element[attribute]!=undefined)
value=element[attribute];}
if(this.target.indexOf("/")!=-1){var ancestors=this.target.split("/");target=ancestors.pop();for(var i=ancestors.length-1;i>=0;i--){if(ancestors[i]==".."){useContainer=useContainer.parentNode;continue;}
if(!useContainer){alert("Aaagh");mozile.debug.debug("mozile.save.extract.Instruction.prototype.execute","No node matching target: "+this.target);return null;}}}
if(target.charAt(0)=="@"){if(!this.value&&(!this.select||this.select=="*"||this.select=="text()")){value=mozile.dom.getText(element);}}
else if(target!=".."){var newContainer=mozile.dom.createElementNS(container.namespaceURI,target);useContainer.appendChild(newContainer);useContainer=newContainer;container=newContainer;}
if(value){if(target.charAt(0)=="@"){useContainer.setAttribute(target.substring(1),value);}
else{text=useContainer.ownerDocument.createTextNode(value);useContainer.appendChild(text);}}
if(target.charAt(0)!="@"){if(this.select=="*"){for(var i=0;i<element.childNodes.length;i++){useContainer.appendChild(element.childNodes[i].cloneNode(true));}}
if(this.select=="text()"){for(var i=0;i<element.childNodes.length;i++){if(element.childNodes[i].nodeType!=mozile.dom.TEXT_NODE)continue;if(mozile.dom.isWhitespace(element.childNodes[i]))continue;useContainer.appendChild(element.childNodes[i].cloneNode(true));}}}}catch(e){alert(mozile.dumpError(e)+"\n"+element+" "+container+"\n"+element.getAttribute("xml")+"\n"+this.toString()+"\n"+target+" "+value);}
return container;}
mozile.gui=new Object();mozile.gui.prototype=new mozile.Module;mozile.gui.factory=null;mozile.gui.create=function(){if(!mozile.gui.factory)throw("Error [mozile.gui.create]: No GUI factory selected.");else return mozile.gui.factory.create();}
mozile.gui.destroy=function(){if(!mozile.gui.factory)throw("Error [mozile.gui.destroy]: No GUI factory selected.");else return mozile.gui.factory.destroy();}
mozile.gui.update=function(event,change){if(!mozile.gui.factory)throw("Error [mozile.gui.update]: No GUI factory selected.");else return mozile.gui.factory.update(event,change);}
mozile.gui.display=function(content){if(!mozile.gui.factory)throw("Error [mozile.gui.display]: No GUI factory selected.");else return mozile.gui.factory.display(content);}
mozile.gui.show=function(){if(!mozile.gui.factory)throw("Error [mozile.gui.show]: No GUI factory selected.");else return mozile.gui.factory.show();}
mozile.gui.hide=function(){if(!mozile.gui.factory)throw("Error [mozile.gui.hide]: No GUI factory selected.");else return mozile.gui.factory.hide();}
mozile.gui.Factory=function(name){this.name=name;}
mozile.gui.Factory.prototype.create=function(){alert("Creating GUI.");}
mozile.gui.Factory.prototype.destroy=function(){alert("Destroying GUI.");}
mozile.gui.Factory.prototype.update=function(event){alert("Updating GUI based on "+event);}
mozile.gui.Factory.prototype.display=function(content){alert("Displaying content:\n"+content);}
mozile.gui.htmlToolbar=new mozile.gui.Factory("HTMLToolbarFactory");mozile.gui.factory=mozile.gui.htmlToolbar;mozile.gui.htmlToolbar.visible=false;mozile.gui.htmlToolbar.toolbar=null;mozile.gui.htmlToolbar.interval=null;mozile.gui.htmlToolbar.checkmark="\u2713";mozile.gui.htmlToolbar.ieColor="#C2C2C2";mozile.gui.htmlToolbar.ieActiveColor="#C1D2EE";mozile.gui.htmlToolbar.ieMenuWidth="170px";mozile.gui.htmlToolbar.ieBorder="";mozile.gui.htmlToolbar.ieActiveBorder="1px solid #316AC5";mozile.gui.htmlToolbar.iePadding="1px";mozile.gui.htmlToolbar.ieActivePadding="0px";mozile.gui.htmlToolbar.about=new mozile.edit.Command("About");mozile.gui.htmlToolbar.about.label="About Mozile";mozile.gui.htmlToolbar.about.tooltip="More information about Mozile";mozile.gui.htmlToolbar.about.image="silk/information";mozile.gui.htmlToolbar.about.execute=function(state,fresh){alert(["About Mozile "+mozile.version,mozile.homepage,mozile.about,mozile.copyright+" "+mozile.license,"Contributors: "+mozile.credits,"Acknowledgements: "+mozile.acknowledgements].join("\n"));state.reversible=false;state.executed=true;return state;}
mozile.gui.htmlToolbar.help=new mozile.edit.Command("Help");mozile.gui.htmlToolbar.help.label="Help";mozile.gui.htmlToolbar.help.tooltip="Get help on using Mozile";mozile.gui.htmlToolbar.help.image="silk/help";mozile.gui.htmlToolbar.help.execute=function(state,fresh){var help=mozile.getAbsolutePath(mozile.help,mozile.root);window.open(help,"MozileHelp","");state.reversible=false;state.executed=true;return state;}
mozile.gui.htmlToolbar.mainMenu=new mozile.edit.CommandGroup("Main Menu");mozile.gui.htmlToolbar.mainMenu.image="Mozile-16";mozile.gui.htmlToolbar.mainMenu.addCommand(mozile.gui.htmlToolbar.about);mozile.gui.htmlToolbar.mainMenu.addCommand(mozile.gui.htmlToolbar.help);mozile.gui.htmlToolbar._commands=new Array(mozile.gui.htmlToolbar.mainMenu);mozile.gui.htmlToolbar.createElement=function(name){if(mozile.defaultNS){mozile.require("mozile.xml");return mozile.dom.createElementNS(mozile.xml.ns.xhtml,name);}
else return mozile.dom.createElement(name);}
mozile.gui.htmlToolbar.createImage=function(name){if(!name||typeof(name)!="string")return null;var filetype=".png";if(name.indexOf(".")>-1)filetype="";var img;var rootPath=mozile.root.substring(0,mozile.root.lastIndexOf('/'));var src=[rootPath,"images",name+filetype].join(mozile.filesep);if(false&&mozile.browser.isIE&&(filetype==".png"||name.toLowerCase().substring(name.length-3,name.length)=="png")){img=mozile.gui.htmlToolbar.createElement("span");img.style.cssText="width: 16px; height: 16px; display:inline-block; "
+"filter:progid:DXImageTransform.Microsoft.AlphaImageLoader"
+"(src=\'"+src+"\', sizingMethod='image');";}
else{img=mozile.gui.htmlToolbar.createElement("img");img.setAttribute("src",src);}
return img;}
mozile.gui.htmlToolbar.create=function(){if(this.toolbar)return;var rootPath=mozile.root.substring(0,mozile.root.lastIndexOf('/'));var href=[rootPath,"src","gui","htmlToolbar.css"].join(mozile.filesep);mozile.dom.addStyleSheet(href,"text/css");this.toolbar=mozile.gui.htmlToolbar.createElement("div");this.toolbar.setAttribute("id","mozileToolbar");mozile.dom.setClass(this.toolbar,"mozileGUI");mozile.protectElement(this.toolbar);var body=mozile.dom.getBody();body.appendChild(this.toolbar);if(mozile.browser.isIE){mozile.dom.setStyle(this.toolbar,"background-color",this.ieColor);}
this.updateButtons();this.reposition();}
mozile.gui.htmlToolbar.destroy=function(){this.hide();delete this.toolbar;var command;for(var name in mozile.edit.allCommands){command=mozile.edit.allCommands[name];if(command.button!=undefined)delete command.button;if(command.menuItem!=undefined)delete command.menuItem;if(command.menu!=undefined)delete command.menu;}}
mozile.gui.htmlToolbar.reposition=function(){if(!mozile.gui.htmlToolbar.toolbar)mozile.gui.htmlToolbar.create();if(mozile.edit.editable)mozile.gui.htmlToolbar.show();else mozile.gui.htmlToolbar.hide();if(!mozile.browser.isIE){mozile.dom.setStyle(mozile.gui.htmlToolbar.toolbar,"position","fixed");mozile.dom.setStyle(mozile.gui.htmlToolbar.toolbar,"top","-1px");}
else{var top=document.documentElement.scrollTop;if(top)mozile.dom.setStyle(mozile.gui.htmlToolbar.toolbar,"top",top+"px");}
if(document.documentElement.clientWidth){var left=(document.documentElement.clientWidth-mozile.gui.htmlToolbar.toolbar.offsetWidth)/2
if(left)mozile.dom.setStyle(mozile.gui.htmlToolbar.toolbar,"left",left+"px");}}
mozile.gui.htmlToolbar.update=function(event,change){if(!event)return false;if(mozile.edit.editable)mozile.gui.htmlToolbar.show();else mozile.gui.htmlToolbar.hide();if(!change){if(event.type.indexOf("key")>-1&&event.type!="keyup")return false;}
this.reposition();if(!event||!event.node){mozile.gui.htmlToolbar.closeMenus();return false;}
if(!change)change="none";if(change=="node"||event.node!=this.lastNode){change="node";if(mozile.dom.isAncestorOf(this.toolbar,event.node))return false;else mozile.gui.htmlToolbar.closeMenus();this.lastNode=event.node;if(!event.rng)event.rng=mozile.edit.lookupRNG(event.node);this.updateButtons(event.rng,event);}
this.updateCommands(event,change);this.reposition();return true;}
mozile.gui.htmlToolbar.updateButtons=function(rng,event){mozile.dom.removeChildNodes(this.toolbar);this.toolbar.commands=new Array();var i=0;for(i=0;i<mozile.gui.htmlToolbar._commands.length;i++){this.updateButton(mozile.gui.htmlToolbar._commands[i]);}
this.toolbar.appendChild(document.createTextNode("|"));for(i=0;i<mozile.edit.commands._commands.length;i++){this.updateButton(mozile.edit.commands._commands[i]);}
if(rng&&rng.commands._commands&&rng.commands._commands.length){this.toolbar.appendChild(document.createTextNode("|"));for(i=0;i<rng.commands._commands.length;i++){this.updateButton(rng.commands._commands[i],event);}}}
mozile.gui.htmlToolbar.updateButton=function(command,event){if(command.button){this.toolbar.appendChild(command.button.element);}
else if(command.group||command.image){var button=new mozile.gui.htmlToolbar.Button(command);this.toolbar.appendChild(button.element);this.toolbar.commands.push(command);}
if(command.button){this.toolbar.commands.push(command);}}
mozile.gui.htmlToolbar.updateCommands=function(event,change){if(!change||typeof(change)!="string")return;if(change=="none")return;for(var i=0;i<this.toolbar.commands.length;i++){var command=this.toolbar.commands[i];if(command.respond(change)){mozile.gui.htmlToolbar.updateCommand(command,event);}}}
mozile.gui.htmlToolbar.updateCommand=function(command,event){if(command.button){command.button.isAvailable(event);command.button.isActive(event);}}
mozile.gui.htmlToolbar.closeMenus=function(){if(!this.toolbar.commands)return;for(var i=0;i<this.toolbar.commands.length;i++){var button=this.toolbar.commands[i].button;if(button.menu&&button.menu.opened)button.menu.close();}}
mozile.gui.htmlToolbar.display=function(content){var win=window.open("","MozileDisplay","");win.document.write(content);}
mozile.gui.htmlToolbar.show=function(){if(!mozile.gui.htmlToolbar.toolbar)mozile.gui.htmlToolbar.create();if(!mozile.gui.htmlToolbar.visible){mozile.dom.setStyle(this.toolbar,"display","block");mozile.gui.htmlToolbar.visible=true;if(mozile.browser.isIE||!mozile.useDesignMode){mozile.gui.htmlToolbar.interval=window.setInterval("mozile.gui.htmlToolbar.reposition()",mozile.updateInterval);}}
return mozile.gui.htmlToolbar.visible;}
mozile.gui.htmlToolbar.hide=function(){if(mozile.gui.htmlToolbar.toolbar&&mozile.gui.htmlToolbar.visible){mozile.dom.setStyle(this.toolbar,"display","none");mozile.gui.htmlToolbar.visible=false;window.clearInterval(mozile.gui.htmlToolbar.interval);}
return mozile.gui.htmlToolbar.visible;}
mozile.gui.htmlToolbar.Button=function(command){if(!command)return;this.command=command;this.type="Button";this.parent=null;this.element=mozile.gui.htmlToolbar.createElement("span");this.element.setAttribute("class","mozileButton");var press=function(event){var c=mozile.edit.getCommand(command.name);if(c)c.button.press(event);}
if(mozile.browser.isIE)this.element.onmouseup=press;else this.element.onclick=press;if(command.tooltip)this.element.setAttribute("title",command.tooltip);this.image=mozile.gui.htmlToolbar.createImage(command.image);this.element.appendChild(this.image);if(command.group){this.menu=new mozile.gui.htmlToolbar.Menu(command);this.menu.parent=this;this.element.appendChild(this.menu.element);var img=mozile.gui.htmlToolbar.createImage("arrow-down.gif");this.element.appendChild(img);}
command.button=this;}
mozile.gui.htmlToolbar.Button.prototype.press=function(event){mozile.event.normalize(event);var opened=false;if(this.menu&&this.menu.opened)opened=true;if(!this.parent)mozile.gui.htmlToolbar.closeMenus();if(this.menu){if(opened)this.menu.close();else this.menu.open(event);}
else{mozile.gui.htmlToolbar.closeMenus();var state=mozile.execCommand(this.command.name);}
mozile.event.cancel(event);}
mozile.gui.htmlToolbar.Button.prototype.getPosition=function(){var position={x:mozile.dom.getX(this.element),y:mozile.dom.getY(this.element),width:this.element.offsetWidth,height:this.element.offsetHeight};return position;}
mozile.gui.htmlToolbar.Button.prototype.isAvailable=function(event){var available=this.command.isAvailable(event);if(available==this.available)return available;this.element.setAttribute("available",available);if(mozile.browser.isIE&&this.image){if(available)this.image.style.cssText="";else this.image.style.cssText="filter:alpha(opacity=50)";}
this.available=available;return available;}
mozile.gui.htmlToolbar.Button.prototype.isActive=function(event){var active=this.command.isActive(event);if(active==this.active)return active;this.element.setAttribute("active",active);if(mozile.browser.isIE&&this.image){if(active){mozile.dom.setStyle(this.image,"border",mozile.gui.htmlToolbar.ieActiveBorder);mozile.dom.setStyle(this.image,"padding",mozile.gui.htmlToolbar.ieActivePadding);}
else{mozile.dom.setStyle(this.image,"border",mozile.gui.htmlToolbar.ieBorder);mozile.dom.setStyle(this.image,"padding",mozile.gui.htmlToolbar.iePadding);}}
this.active=active;return active;}
mozile.gui.htmlToolbar.MenuItem=function(command){if(!command)return;this.command=command;this.type="MenuItem";this.parent=null;this.element=mozile.gui.htmlToolbar.createElement("tr");this.element.setAttribute("class","mozileMenuItem");var press=function(event){var c=mozile.edit.getCommand(command.name);if(c)c.menuItem.press(event);}
if(mozile.browser.isIE)this.element.onmouseup=press;else this.element.onclick=press;if(command.tooltip)this.element.setAttribute("title",command.tooltip);this.cells={};this.cells.active=mozile.gui.htmlToolbar.createElement("td");this.cells.icon=mozile.gui.htmlToolbar.createElement("td");this.cells.label=mozile.gui.htmlToolbar.createElement("td");this.cells.accel=mozile.gui.htmlToolbar.createElement("td");if(mozile.browser.isIE){this.cells.active.className="mozileActive";this.cells.icon.className="mozileIcon";this.cells.label.className="mozileLabel";this.cells.accel.className="mozileAccel";}
else{this.cells.active.setAttribute("class","mozileActive");this.cells.icon.setAttribute("class","mozileIcon");this.cells.label.setAttribute("class","mozileLabel");this.cells.accel.setAttribute("class","mozileAccel");}
this.element.appendChild(this.cells.active);this.element.appendChild(this.cells.icon);this.element.appendChild(this.cells.label);this.element.appendChild(this.cells.accel);if(command.image){var img=mozile.gui.htmlToolbar.createImage(command.image);this.cells.icon.appendChild(img);}
var name=command.name;if(command.label)name=command.label;this.cells.label.appendChild(document.createTextNode(name));if(command.group){this.menu=new mozile.gui.htmlToolbar.Menu(command);this.menu.parent=this;this.element.appendChild(this.menu.element);img=mozile.gui.htmlToolbar.createImage("menu-arrow.gif");this.cells.accel.appendChild(img);}
else if(command.accel){var accel=mozile.edit.parseAccelerator(command.accel);var span=mozile.gui.htmlToolbar.createElement("span");span.appendChild(document.createTextNode(accel.abbr));this.cells.accel.appendChild(span);}
command.menuItem=this;}
mozile.gui.htmlToolbar.MenuItem.prototype=new mozile.gui.htmlToolbar.Button;mozile.gui.htmlToolbar.MenuItem.prototype.constructor=mozile.gui.htmlToolbar.MenuItem;mozile.gui.htmlToolbar.MenuItem.prototype.isActive=function(event){var active=this.command.isActive(event);if(active==this.active)return active;mozile.dom.removeChildNodes(this.cells.active);if(active){this.cells.active.appendChild(mozile.gui.htmlToolbar.createImage("silk/tick"));}
this.active=active;return active;}
mozile.gui.htmlToolbar.Menu=function(command){if(!command)return;this.command=command;this.type="Menu";this.parent=null;this.element=mozile.gui.htmlToolbar.createElement("table");this.element.setAttribute("class","mozileMenu");this.element.setAttribute("cellspacing","0px");mozile.dom.setStyle(this.element,"display","none");var tbody=mozile.gui.htmlToolbar.createElement("tbody");this.element.appendChild(tbody);if(mozile.browser.isIE){mozile.dom.setStyle(this.element,"background-color",mozile.gui.htmlToolbar.ieColor);}
this.menuItems=new Array();for(var i=0;i<command._commands.length;i++){var menuItem=new mozile.gui.htmlToolbar.MenuItem(command._commands[i]);if(menuItem){this.menuItems.push(menuItem);menuItem.parent=this;tbody.appendChild(menuItem.element);}}
this.opened=false;command.menu=this;}
mozile.gui.htmlToolbar.Menu.prototype=new mozile.gui.htmlToolbar.Button;mozile.gui.htmlToolbar.Menu.prototype.constructor=mozile.gui.htmlToolbar.Menu;mozile.gui.htmlToolbar.Menu.prototype.open=function(event){try{this.reposition();mozile.dom.setStyle(this.element,"display","block");this.opened=true;for(var i=0;i<this.menuItems.length;i++){this.menuItems[i].isAvailable(event);this.menuItems[i].isActive(event);}}catch(e){alert(mozile.dumpError(e));}}
mozile.gui.htmlToolbar.Menu.prototype.close=function(){mozile.dom.setStyle(this.element,"display","none");this.opened=false;for(var i=0;i<this.menuItems.length;i++){if(this.menuItems[i].menu&&this.menuItems[i].menu.opened){this.menuItems[i].menu.close();}}}
mozile.gui.htmlToolbar.Menu.prototype.reposition=function(){if(!this.parent)return;var left=0;var top=0;var position=this.parent.getPosition();if(mozile.browser.isIE){mozile.dom.setStyle(this.element,"position","absolute");mozile.dom.setStyle(this.element,"width",mozile.gui.htmlToolbar.ieMenuWidth);mozile.dom.setStyle(this.element,"border",mozile.gui.htmlToolbar.ieBorder);left=this.parent.element.offsetLeft;top=this.parent.element.offsetTop;}
else{var width=this.element.clientWidth;mozile.dom.setStyle(this.element,"position","fixed");mozile.debug.debug("",position.x+" + "+width+" = "+(position.x+width)+" ? "+document.documentElement.clientWidth);if(position.x+width>document.documentElement.clientWidth){left=document.documentElement.clientWidth-width;}
else left=position.x;top=position.y;}
if(this.parent.type=="Button"){top+=position.height;}
else{left+=position.width;}
mozile.dom.setStyle(this.element,"left",left+"px");mozile.dom.setStyle(this.element,"top",top+"px");}

/**** Final Configuration ****/
// Load all deferred modules.
mozile.deferRequirements = false;
mozile.loadDeferred();
