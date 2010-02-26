var ACTUALSCRIPT = null;

function showInformation(script){
	setActualScript(script);
	var url = document.location.href;
	if(url.indexOf("execution-uri-overview") != -1){
		statusMessage.add("load-execution-uri", "loading execution uri");
		$("#tx-list").load("./get-execution-uri?script="+script+"&mode=naked", function(){
			statusMessage.remove("load-execution-uri");
			showExecutionUriTabActions();
		});
	} else if(url.indexOf("script-overview") != -1){
		statusMessage.add("get-scripts", "loading scripts");
		$("#tx-list").load("./get-script?script="+script+"&mode=naked", function() {
			statusMessage.remove("get-scripts");
		});
	}
}

function countNamedElements(name) {
	var cnt = 0;
	var o = document.getElementsByName(name);
	for(var i = 0 ; i < o.length; i++){
        cnt++;
	}

	return cnt;
}

function showExecutionUriTabActions() {
	$('#tx-contextual-buttons').empty();
	if(countNamedElements('executionUriCheckBox') > 0) {
		$('#tx-contextual-buttons').append('<ol><li><a href="javascript:deleteExecutionUris()" class="tx-button tx-button-remove">Delete</a></li>'+
		'<li><a href="javascript:showAddExecutionUriForm();" class="tx-button tx-button-create">Add</a></li></ol>');
	} else {
		$('#tx-contextual-buttons').append('<ol><li><a href="javascript:showAddExecutionUriForm();" class="tx-button tx-button-create">Add</a></li></ol>');
	}
}

function setActualScript(script){
	ACTUALSCRIPT = script;
}

function getQueryParam(name) {

	var query = window.location.search.substring(1);
	var parms = query.split('&');
	for (var i=0; i<parms.length; i++) {
		var pos = parms[i].indexOf('=');
		if (pos > 0) {
			var key = parms[i].substring(0,pos);
			if(key == name) {
				setActualScript(parms[i].substring(pos+1));
				break;
			}
		}
	}
}

function updateScript(){
	$("#updateform").submit();
}

function deleteScript(){
	var options = new AjaxOptions("delete-script", "deleting script", function(obj) {
					$("#tx-list").empty();
					$("#tx-result").load("./script-list?mode=naked");
				});
	options.type = "POST"
	options.url = "delete";
	options.data = "script=" + encodeURIComponent(ACTUALSCRIPT);
	$.ajax(options);
}

function installScript(){
	$("#installform").submit();
}

function executeScript(){
	var options = new AjaxOptions("execute-script", "executing script", function(obj) {
					$('#scriptConsole').empty();
					$('#scriptConsole').append('<h4>Script Output</h4><pre>'
						+obj+'</pre>');
					$('#scriptConsole').slideDown('slow');
				});
	options.url = "execute";
	options.data = "script=" + encodeURIComponent(ACTUALSCRIPT);
	$.ajax(options);
}

function showAddExecutionUriForm(){
	$('#addExecutionUriForm').empty();
	$('#addExecutionUriForm').append('<form id="addExecutionUri"><input type="hidden" id="scriptUri" name="scriptUri" value="'+ACTUALSCRIPT+'"></input>'
    +'<label for="executionUri">Execution URI</label>  <input type="text" name="executionUri"></input> '
    +'<input type="button" value="add" onclick="javascript:addExecutionUri()"></input>  </form>');
	$('#addExecutionUriForm').slideDown('slow');
}

function deleteExecutionUris(){
	$(':checked').each(
		function() {
			var options = new AjaxOptions("delete-execution-uri", "deleting execution uri", function(obj) {
						$("#tx-list").load("./get-execution-uri?script="+ACTUALSCRIPT+"&mode=naked", function(){
						showExecutionUriTabActions();
					});
						});
			options.type = "POST"
			options.url = "delete-executionUri";
			options.data = "executionUri="+$(this).val()+"&scriptUri="+encodeURIComponent(ACTUALSCRIPT);
			$.ajax(options);
		}
	);
}

function addExecutionUri(){
	var serialized = $('#addExecutionUri').serialize();
	var options = new AjaxOptions("add-execution-uri", "saving execution uri", function(obj) {
				$('#addExecutionUriForm').slideUp('slow');
				$("#tx-list").load("./get-execution-uri?script="+$('#scriptUri').val()+"&mode=naked", function(){
					showExecutionUriTabActions();
				});
			});
	options.type = "POST"
	options.url = "add-execution-uri";
	options.data = serialized;
	$.ajax(options);
}

function fileChoiceSelected() {
	if(document.getElementById('fileButton').checked) {
		$('#choiceCell').empty();
		$('#choiceCell').append('<input type="radio" name="fileChoice" id="fileButton" value="file" checked="checked" onclick="fileChoiceSelected()" /> Upload a File '+
						'<input type="radio" name="fileChoice" id="textButton" value="text" onclick="fileChoiceSelected()"/> Enter Script');
		$('#nameRow').remove();
		$('#fileCell').empty();
		$('#fileCellLabel').empty();
		$('#fileCellLabel').append('<label for="scriptFile">Script File:</label>');
		$('#fileCell').append('<input type="file" name="scriptFile" />');
	} else if(document.getElementById('textButton').checked) {
		$('#choiceCell').empty();
		$('#choiceCell').append('<input type="radio" name="fileChoice" id="fileButton" value="file" onclick="fileChoiceSelected()" /> Upload a File '+
						'<input type="radio" name="fileChoice" id="textButton" value="text" checked="checked" onclick="fileChoiceSelected()"/> Enter Script');
		$('#fileRow').before('<tr id="nameRow"><td><label for="scriptName">Script Name:</label></td><td><input type="text" name="scriptName" /></td></tr>');
		$('#fileCellLabel').empty();
		$('#fileCellLabel').append('<label for="scriptFile">Script:</label>');
		$('#fileCell').empty();
		$('#fileCell').append('<textarea name="scriptCode" id="scriptCode" rows="10" style="font-size:12px; width:70%;"></textarea>');
	}
}